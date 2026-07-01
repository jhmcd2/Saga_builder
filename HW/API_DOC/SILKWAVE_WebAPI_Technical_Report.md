# SILKWAVE Web/Admin API — Technical Report

*Classification of source: UNCLASSIFIED (per source file headers). Based on code review of `WebServer.java`, `NetworkManager.java`, associated servlets, and `SILKWAVE_README.md`.*

---

## 1. Overview

The SILKWAVE hub exposes an embedded Jetty HTTP(S) server (`gov.ic.silkwave.web.WebServer`) that hosts a set of servlets for administration, health checking, name-service inspection, resource export, and (optionally) a CASPORT v3 REST simulation endpoint. The server is a singleton, started once via `WebServer.getInstance()` and only comes up if HTTP or HTTPS is explicitly enabled in configuration.

Underneath the web layer, `NetworkManager` is the core message-routing hub that the web server's servlets query indirectly (via `ServiceLocator`) for live operational data (queue depths, connection counts, routing state, etc.). The web server itself connects to the internal messaging bus as a client (`ConnectionManager`) under the identity `service.webserver.client.user` / `service.webserver.client.password`.

---

## 2. Ports

| Port (default) | Property | Protocol | Purpose |
|---|---|---|---|
| **8501** | `service.webserver.http.port` | HTTP | Main web/admin UI and REST-style servlets (plaintext) |
| **8601** | `service.webserver.https.port` | HTTPS | Main web/admin UI and REST-style servlets (TLS) |

**Important operational detail:** the web server runs in **one mode at a time**, not both simultaneously. In `WebServer`'s constructor:

```java
if (httpsEnabled) {
    ... build HTTPS connector ...
    server.setConnectors(new Connector[]{https});
} else if (httpEnabled) {
    ... build HTTP connector + security handler ...
    server.setConnectors(new Connector[]{http});
}
```

If `service.webserver.https.enabled=true`, the HTTPS branch runs and the HTTP branch (including all of the FormAuthenticator/`ConstraintSecurityHandler` setup described in §4) is **skipped entirely** — only a single connector is ever attached to the `Server`.

Other ports referenced elsewhere in the platform (not part of this web API, listed for context, from `SILKWAVE_README.md`):

| Port | Purpose |
|---|---|
| 61616 / 61617 | Core SILKWAVE messaging bus (TCP / SSL) |
| 61622 / 61623 | STOMP (SSL / TCP), disabled by default |
| 9552 | UDP stream port |
| 8443 / 8444 | File transfer (HTTP / HTTPS) |

### 2.1 Bind address

If `service.webserver.localonly=true`, both the HTTP and HTTPS connectors bind to `127.0.0.1` only (checked independently for each connector), restricting the web API to localhost regardless of which port is active.

### 2.2 Notable config inconsistency

In the HTTPS branch, `HttpConfiguration.setSecurePort(...)` is populated from `service.webserver.http.port` (default `"8601"`) rather than `service.webserver.https.port`:

```java
https_config.setSecurePort(
    Integer.parseInt(Constants.props.getProperty("service.webserver.http.port", "8601")));
...
https.setPort(
    Integer.parseInt(Constants.props.getProperty("service.webserver.https.port", "8601")));
```

The actual listening port (`https.setPort`) correctly reads `service.webserver.https.port`, so this only affects the internally-advertised "secure port" metadata used in redirects/URLs — but if an operator sets `service.webserver.http.port` and `service.webserver.https.port` to different values, this metadata will be wrong.

---

## 3. Enabling flags

| Property | Default | Effect |
|---|---|---|
| `service.webserver.http.enabled` | `false` | Enables the plaintext HTTP connector |
| `service.webserver.https.enabled` | `false` | Enables the TLS connector (takes priority over HTTP if both are true) |
| `service.webserver.casportsim.enabled` | `false` | Mounts the CASPORT v3 simulation servlet at `/rest/v3/*` |
| `service.webserver.localonly` | `false` | Binds connector(s) to `127.0.0.1` |
| `service.webserver.content.location` | `WebContent` | Static resource base for the `DefaultServlet` (login pages, JS/CSS assets, etc.) |

If neither HTTP nor HTTPS is enabled, `WebServer` logs a warning and never starts (`server.start()` is never called).

---

## 4. Authentication and access control

The web context uses Jetty's `ConstraintSecurityHandler` with **Form authentication** (`FormAuthenticator`, login page `/login.html`, error page `/loginerror.html`).

- Default constraint: **all paths (`/*`) require authentication**, with roles `user`, `admin`, or `moderator`.
- Login credentials source: a realm file (`service.webserver.http.realm.file`) if configured, otherwise a single hardcoded user/pass pair from `service.webserver.user` / `service.webserver.pass` (defaults: `admin` / `silkwave`).
- TRACE and TRACK HTTP methods are explicitly disabled hub-wide (`disableTrackTrace`) to harden against security scanners.

### 4.1 Paths explicitly whitelisted (no authentication required)

| Path | Purpose |
|---|---|
| `/jquery-ui/*` | Static asset |
| `/jquery/*` | Static asset |
| `/css/login.css`, `/css/login-error.css` | Login page styling |
| `/js/login.js`, `/js/login-error.js` | Login page scripting |
| `/health` | Liveness probe (see §6) |
| `/resources-export/*` | Resource export API (see §6) |

### 4.2 Security gaps / things worth flagging

1. **This entire `ConstraintSecurityHandler` block only exists inside the HTTP branch.** If the server runs in HTTPS mode (`service.webserver.https.enabled=true`), `context.setSecurityHandler(...)` is never called on the root context — meaning **no authentication is enforced at all** over HTTPS. Everything mounted on the root context (all `*AdminServlet`s, `/health`, `/resources-export/*`, static content) is reachable unauthenticated when running in HTTPS mode.
2. **`/resources-export` (exact path, no trailing slash) is not whitelisted** — only `/resources-export/*` is. The servlet is registered under both path specs, so hitting the bare `/resources-export` in HTTP mode would fall under the default `/*` constraint and require login, while `/resources-export/` (with slash) or any sub-path would not.
3. **The CASPORT v3 simulator (`/rest/v3/*`) runs in a separate `ServletContextHandler`** (`casportSimContext`) that never receives a `securityHandler` in either HTTP or HTTPS mode. It is unauthenticated in all configurations. This is presumably acceptable given it is explicitly a *simulator* for testing group-membership checks, but it should be confirmed disabled (`service.webserver.casportsim.enabled=false`) outside test/dev environments.

---

## 5. Servlet map

Registered on the root context (`/`, `ServletContextHandler.SESSIONS`):

| Path | Servlet | Notes |
|---|---|---|
| `/SilkwaveAdminServlet` | `SilkwaveAdminServlet` | Hub status/stats dashboard backend (see §6) |
| `/DiscoveryServiceAdminServlet` | `DiscoveryServiceAdminServlet` | Not included in reviewed source set |
| `/NetworkStatusAdminServlet` | `NetworkStatusAdminServlet` | Not included in reviewed source set |
| `/RegistrationAdminServlet` | `RegistrationAdminServlet` | Not included in reviewed source set |
| `/RoutingAdminServlet` | `RoutingAdminServlet` | Not included in reviewed source set |
| `/SecurityAdminServlet` | `SecurityAdminServlet` | Not included in reviewed source set |
| `/NamingAdminServlet` | `NamingAdminServlet` | Not included in reviewed source set |
| `/NamingServiceAdminServlet` | `NamingServiceAdminServlet` | Name-server route query/admin UI backend (see §6) |
| `/StreamAdminServlet/*` | `StreamAdminServlet` | Not included in reviewed source set |
| `/FileAdminServlet/*` | `FileAdminServlet` | Not included in reviewed source set |
| `/health` | `HealthServlet` | k8s liveness probe (see §6) |
| `/resources-export/*`, `/resources-export` | `ResourcesExportServlet` | JSON export of NameServer resource maps (see §6) |
| `/` | `DefaultServlet` | Static content root (`WebContent`, or `service.webserver.content.location`) |

Registered on a separate context (`/rest/v3`), only when `service.webserver.casportsim.enabled=true`:

| Path | Servlet | Notes |
|---|---|---|
| `/rest/v3/*` | `CasportV3SimServlet` | Simulates a CASPORT v3 group-membership REST API (see §6) |

Several admin servlets referenced above (`DiscoveryServiceAdminServlet`, `NetworkStatusAdminServlet`, `RegistrationAdminServlet`, `RoutingAdminServlet`, `SecurityAdminServlet`, `NamingAdminServlet`, `StreamAdminServlet`, `FileAdminServlet`) were not part of the files reviewed for this report; their behavior is inferred only from naming and package (`gov.ic.silkwave.*`) conventions and is **not independently verified**.

---

## 6. Endpoint details (servlets reviewed in full)

### 6.1 `GET/POST /health` — `HealthServlet`

Intended for **Kubernetes liveness probes**. Returns `text/plain`.

- `200 good` — healthy
- `500 bad` — unhealthy

Health determination (`isHealthy()`, synchronized) connects to the internal message bus as `HealthTask` and pings, in order:

1. `localns:ww` (always)
2. `localns:silkwave.security` (only if `service.security.state != off`)
3. Four AppGeo endpoints, gated by `service.webserver.health.appgeo.enabled` (default `true`):
   `localns:oio.appgeo:<hubId>.status`, `.topic.status`, `.command`, `.registration`
4. `localns:isrfabric:status`, gated by `service.webserver.health.fabric.enabled` (default `false`)

Each ping uses timeout `service.webserver.health.conn.timeoutMs` (default `3000`ms). Connection is torn down after each check regardless of outcome.

### 6.2 `GET/POST /SilkwaveAdminServlet` — `SilkwaveAdminServlet`

Primary hub-status/dashboard backend, `application/json`, driven by `?action=`:

| `action` value | Behavior |
|---|---|
| *(none)* or `load` | Returns a large JSON blob: logged-in user (from client cert CN or principal), current time, JVM start time/uptime, message counts, per-queue stats (Incoming, Heartbeat, Core, Authentication — total/size/min/max/avg), dead-letter counts by category (see §6.5), connection/privileged-connection counts, neighbor/hub counts, and min/max hub security markings |
| `resetFailed` | Resets `DeadLetterManager` counters to zero |
| `logout` | Invalidates the current HTTP session |
| `domain` | Returns `{"domain": "<Constants.MYDOMAIN>"}` |
| `validate` | Accepts a raw envelope body, parses/validates it as a SILKWAVE `Envelope`, optionally validates payload if `payload=true` is passed; returns plaintext success/error report |

Identity for the JSON `loggedName` field is resolved from `request.getUserPrincipal()` first, falling back to the CN of an mTLS client certificate (`jakarta.servlet.request.X509Certificate` attribute) if present.

### 6.3 `GET /NamingServiceAdminServlet` — `NamingServiceAdminServlet`

Backend for a jqGrid-style name-server browser, `?action=`:

| `action` value | Behavior |
|---|---|
| `nameservers` | Lists all registered nameserver domains as JSON |
| `query` | Paginated/sortable/filterable list of all `RouteEntry` records across all domains. Supports jqGrid-style filter JSON (`groupOp` AND/OR, `rules[]` with `field`/`op`/`data`), sort (`sidx`/`sord`), and paging (`page`/`rows`) |
| `subquery` | Given a specific route `name`, returns the paginated list of `NetId` entries (id, owner, timestamp) under that route |

Filter operators supported: `eq`, `ne`, `bw` (begins-with), `ew` (ends-with), `cn` (contains), `lt`, `gt` (numeric for the `count` field, lexicographic otherwise).

*(Note: a `NullPointerException` risk exists in the `default -> null` branch of the field switch if an unrecognized `field` is supplied in a filter rule, since `value.equals(...)` is then called on `null`. Also, `subquery`'s `rows = rows;` line is a no-op — the `rowsString` parameter is parsed but never actually applied to `rows`.)*

### 6.4 `GET /resources-export/*` — `ResourcesExportServlet`

Read-only JSON export of the internal `NameServer` resource maps, added specifically to support the export capability described in `NAMESERVER_EXPORT_API.md`. All responses are `application/json`.

| Path | Returns |
|---|---|
| `/resources-export/` or `/resources-export` | List of registered nameserver domains + list of available sub-endpoints |
| `/resources-export/resources?domain=` | The domain's `resources` map (`ConcurrentMap<String, ResourceRoute>`), plus `count` and `timestamp` |
| `/resources-export/netResources?domain=` | The domain's `netToResources` map, plus `count`/`timestamp` |
| `/resources-export/nameServerResources?domain=` | The domain's `nameServersToResources` map, plus `count`/`timestamp` |
| `/resources-export/all?domain=` | All three maps in one response, plus `exportTimestamp` |
| *(anything else)* | `404` |

`domain` is optional; if omitted, the first domain returned by `NameServerLocator.getNameServerDomains()` is used. Returns `404` if the domain isn't found or no nameservers are registered; `500` on unexpected exceptions (message included, no stack trace).

Underlying data source (`NameServer.java`, not part of this upload but documented in `NAMESERVER_EXPORT_API.md`) exposes the three maps via new getters that acquire the class's existing `ReentrantReadWriteLock` (read lock) and return defensive copies — so the export path is safe for concurrent reads without blocking writers, and callers cannot mutate live internal state through the returned maps.

### 6.5 `POST/GET /rest/v3/*` — `CasportV3SimServlet` *(optional, dev/test)*

Only mounted when `service.webserver.casportsim.enabled=true`, on its own context path `/rest/v3` (not the root context — see §4.2 for the security implication). Simulates a CASPORT v3 group-membership-check API. Expects request URIs of the form:

```
/rest/v3/groups/{group}/members/{member}
```

Behavior:
- `400` if the URI doesn't split into exactly 7 `/`-delimited parts, or the `groups`/`members` keywords are missing/misplaced
- `400` if `{group}` (case-insensitive) is `invalidgroup`
- `404` if `{group}` (case-insensitive) is `groupnotfound`
- Otherwise, returns `{"isMember": true}` only for the exact hardcoded pair — group `testgroup`, member `CN=Test Client,OU=SILKWAVE,O=JICD 4.2,L=Warrenton,ST=Virginia,C=US` (URL-decoded, case-sensitive) — `{"isMember": false}` for everything else

This is purely a canned-response test double for exercising CASPORT integration logic; it does not talk to a real group directory.

### 6.6 Dead-letter counters (`DeadLetterManager`) surfaced via `/SilkwaveAdminServlet`

Not a servlet itself, but worth noting since its counts are exposed in the JSON above: `DeadLetterManager` is a singleton that tracks failed-message counts by `DeadLetterTypeEnum` (`BAD_ENVELOPE`, `INVALID_SOURCE`, `NOT_RESOLVABLE`, `NOT_ROUTABLE`, `NOT_DELIVARABLE`, `NOT_AUTHORIZED`, `INVALID_NET_PAYLOAD`, `IMPROPER_CLASSIFICATION`, `UNKNOWN`), deduplicates outbound failure notifications per source/type/dest using a Guava cache (size/TTL configurable via `service.deadletter.maxCacheSize` / `service.deadletter.maxCacheAge`), and can send a `FailureNotification` SILKWAVE message back to the originating source.

---

## 7. Deprecated / compatibility-only components (present but not part of the active API surface)

These are marked `@Deprecated` with "*included for compatibility with 2.4.1.4*" and are not wired into `WebServer`:

- `AppGeoEnvelopeProcessor` — legacy envelope-processing plugin
- `HttpRequestMonitor` — legacy Jetty `HttpClient` connection pool/cleanup utility (contains a commented-out, non-compiling `SslContextFactory` block; effectively non-functional as-is)
- `MockConnection` — test double for `ConnectionInterface`, used in unit/integration tests, not part of the runtime web API

## 8. Test/scaffolding components not part of the production API

- `RestrictionsLoginService` — a `LoginService` implementation with `validate()` hardcoded to `false` and no real credential check. It is referenced only as a commented-out alternative (`//securityHandler.setLoginService(new RestrictionsLoginService());`) in `WebServer.java` and is **not** the login service actually wired in — `HashLoginService` is used instead.

---

## 9. Summary — quick reference

- **Access the API at:** `http://<host>:8501/...` or `https://<host>:8601/...` (mutually exclusive, HTTPS wins if both enabled)
- **Authenticate via:** Form login at `/login.html`, default creds `admin`/`silkwave` unless a realm file or different props are set — **but only enforced when running in HTTP mode**
- **Unauthenticated in all modes:** `/rest/v3/*` (if casport-sim enabled)
- **Unauthenticated in HTTP mode only:** `/health`, `/resources-export/*`, static login assets
- **Key JSON endpoints:** `/SilkwaveAdminServlet?action=load` (full hub dashboard), `/resources-export/all` (nameserver resource dump), `/NamingServiceAdminServlet?action=query` (route browser)
