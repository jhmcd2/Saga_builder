# SILKWAVE Web Endpoint Schemas

This document describes the response data emitted by the embedded Jetty web server in
`core/core/src/main/java/gov/ic/silkwave/web/WebServer.java`. The schemas are based on the
servlet implementations and the jqGrid column definitions in `core/core/WebContent/js`.

## Conventions

Most administrative endpoints return a jqGrid-shaped JSON page:

```json
{
  "total": "<number of pages>",
  "page": "<1-based page number>",
  "records": "<number of matching records>",
  "rows": [
    {"id": "<1-based row id>", "cell": ["<column 1>", "<column 2>"]}
  ]
}
```

`total`, `page`, `records`, `id`, and every `cell` value are emitted as JSON strings,
including values that represent numbers or booleans. `rows` is paged with `rows` and
`page` query parameters; the default is 10 rows on page 1 unless noted otherwise.

Filtering uses a URL-encoded jqGrid object when supported:

```json
{
  "groupOp": "AND|OR",
  "rules": [{"field": "<field>", "op": "eq|ne|bw|ew|cn|lt|gt", "data": "<value>"}]
}
```

## FileAdminServlet

Mounted at `/FileAdminServlet/*`; `GET` only. The implementation expects `action`, `rows`,
and `page` for all normal actions, and `token` for `file` and `cancel`.

### `action=files`

Returns the file-transfer page. `cell` columns are:

1. `start` - UTC timestamp `yyyy-MM-dd'T'HH:mm:ss'Z'`, or `NO ACTIVITY`.
2. `state` - file state, followed by source state and optional source error text.
3. `token` - transfer token.
4. `source` - source endpoint.
5. `actor` - requesting actor.
6. `activity` - source last-data-transmitted UTC timestamp, or `NO ACTIVITY`.
7. `cache` - source cache status (`true` or `false`).
8. `size` - file size in locale-formatted bytes.
9. `bytesTransferred` - source bytes transferred in locale-formatted units.
10. `percentComplete` - source completion percentage in locale-formatted units.

### `action=file&token=<token>`

Returns the destinations for one file. `total` and `page` are always `"1"`; `records` is
the number of destination entries. `cell` columns are:

1. `start` - UTC start timestamp, or `NO ACTIVITY`.
2. `state` - destination state and optional error text.
3. `localdest` - local destination.
4. `ultimatedest` - ultimate destination.
5. `activity` - last-data-transmitted UTC timestamp, or `NO ACTIVITY`.
6. `percentComplete` - locale-formatted completion percentage.
7. `bytesTransferred` - locale-formatted transferred bytes.

If the token is not found, the response is status `400` with the plain text body `Error`.

### `action=cancel&token=<token>`

Cancels the matching transfer when its state exists. The response has status `200` and no
defined JSON body.

Unknown actions return status `400` with the plain text body `Error`.

## StreamAdminServlet

Mounted at `/StreamAdminServlet/*`; `GET` only.

### `action=requests`

`cell` columns: `creation` (UTC timestamp), `token`, `source`, `actor`, `state` with
optional failure reason, `passive`, and `current` (`isStreaming`).

### `action=streams`

`cell` columns: `streamid`, `source`, `rate` (average rate), `timeout`, `lastpacket` (UTC
timestamp), `state` with optional failure reason, `packets`, `bytes`, and `dropped`.

### `action=paths`

The lookup uses `token` or `streamId`. With `streamId`, `cell` columns are:
`requesttoken`, `actor`, `ultimateDestination`, `localDestination`, `enabled`, `passive`,
`failed`, `lastHeartbeat`, `phys` (stream URI), `packets`, `bytes`, `ldpackets`, and
`ldbytes`. With `token`, the first two and the last eight columns are omitted; the columns
are `ultimateDestination`, `localDestination`, and `enabled`.

## DiscoveryServiceAdminServlet

Mounted at `/DiscoveryServiceAdminServlet`; `GET` only. Returns a jqGrid page with
`cell` columns: `name` (service name), `net` (endpoint routing URI), and `local`
(`true`/`false`, whether the endpoint is local to the hub).

## NetworkStatusAdminServlet

Mounted at `/NetworkStatusAdminServlet`; `GET` only.

### Default or `action=query`

Returns owner status rows with `cell` columns: `owner`, `interval`, `count`, and
`lastSeen` (XML date/time string). Supports `filters`, `sidx` values `owner`, `interval`,
`count`, `lastSeen`, and `sord`.

### `action=subquery&owner=<owner>&interval=<interval>`

Returns one string column per row: `name`, the sorted network address subscribed by that
owner at that interval.

### `action=trace&resource=<resource>`

Returns `text/plain`, status `200`. The body is the trace text, or an empty line when
`resource` is absent.

## RegistrationAdminServlet

Mounted at `/RegistrationAdminServlet`; `GET` only.

### Default or `action=registrationInfo`

`cell` columns: `identity` (user name), `description`, `uri` (routing URI), `token`
(masked session token), `endpoint`, `transportid`, `privileged`, `idle` (milliseconds),
and `count` (destination queue size).

### `action=connectionInfo`

`cell` columns: `connectionId`, `clientId`, `uri` (remote address), `username`, `consumers`,
`producers`, `connected`, `active`, `blocked`, and `slow`.

### `action=connectioninfo&destination=<destination>`

Returns a non-paged JSON object whose values are strings:

```json
{
  "destinationname": "<name>",
  "destinationsize": "<current size>",
  "destinationenqueued": "<enqueued count>",
  "destinationdequeued": "<dequeued count>"
}
```

When the destination is not found, the implementation returns `{}` with status `200`.

## RoutingAdminServlet

Mounted at `/RoutingAdminServlet`; `GET` only. `cell` columns are `dest`, `neighbor`,
`cost`, and `alts` (number of alternate routes). Filtering fields are `dest`, `neighbor`,
`cost`, and `alts`.

## SecurityAdminServlet

Mounted at `/SecurityAdminServlet`; `GET` only.

### Default or `action=securityInfo`

`cell` columns are `name`, `type`, and `registered`.

### `action=securityCacheInfo`

`cell` columns are `netid`, `servicename`, `servicetype`, `outcome`, `time`, and
`restriction`. `restriction` may be an empty string.

## NamingAdminServlet

Mounted at `/NamingAdminServlet/*`; `GET` and `POST` (POST delegates to GET).

### `action=namecaches`

Returns:

```json
{"namecaches": ["<domain>", "..."]}
```

### `action=query`

Returns the merged naming-cache page. `cell` columns are `domain`, `routing`, `type`,
`count`, `local`, `state`, `updated`, and `expired`.

### `action=subquery&cacheURI=<routing URI>`

Returns one string column per row: `routing`, the resolved endpoint URI.

### `action=edit&oper=del&uri=<uri[,uri...]>`

Deletes cached routes. For a valid delete operation the response has no defined body.
Other edit operations and unknown actions return status `400` with plain text `Error`.

## NamingServiceAdminServlet

Mounted at `/NamingServiceAdminServlet`; `GET` only.

### `action=nameservers`

Returns:

```json
{"nameservers": ["<domain>", "..."]}
```

### `action=query`

Returns a route page with `cell` columns `name`, `type`, and `count` (number of network
IDs attached to the route). Filtering and sorting support `name`, `type`, and `count`.

### `action=subquery&name=<routing URI>`

Returns the route's network IDs with `cell` columns `name` (network ID), `owner`, and
`time` (XML date/time). If the route is not found, the page has zero records and an empty
`rows` array.

## SilkwaveAdminServlet

Mounted at `/SilkwaveAdminServlet`; `GET` and `POST` (POST delegates to GET).

### Default or `action=load`

Returns a JSON object. All values are strings. `loggedName` is optional. The fields are:

`loggedName`, `currenttime`, `starttime`, `uptime`, `messages`, `incomingtotal`,
`incomingsize`, `incomingmin`, `incomingmax`, `incomingavg`, `heartbeattotal`,
`heartbeatsize`, `heartbeatmin`, `heartbeatmax`, `heartbeatavg`, `coretotal`, `coresize`,
`coremin`, `coremax`, `coreavg`, `authenticationtotal`, `authenticationsize`,
`authenticationmin`, `authenticationmax`, `authenticationavg`, `summaryqueued`,
`failedmessages`, `badenvelopemessages`, `invalidsourcemessages`,
`notdelivarablemessages`, `notresolvablemessages`, `notroutablemessages`,
`unknownfailedmessages`, `invalidnetpayloadmessages`, `notauthorizedmessages`,
`improperclassificationmessages`, `connections`, `privconnections`, `neighbors`,
`totalhubs`, `hubminsecurity`, and `hubmaxsecurity`.

`currenttime` and `starttime` are XML date/time strings. `uptime` has the form
`<days> day(s) HH:MM:SS`. Queue fields use the queue names `incoming`, `heartbeat`,
`core`, and `authentication`; each has `total`, `size`, `min`, `max`, and `avg` fields.

### `action=resetFailed` or `action=logout`

Returns `{}` with status `200` when the action completes. `logout` may produce no body if
there is no current session.

### `action=domain`

Returns `{"domain": "<hub domain>"}`.

### `action=validate`

Returns `text/plain`, status `200`. The request body is a raw envelope. The response is a
line-oriented validation report containing `Valid Envelope`, optionally `Valid Payload`,
or an error message. The `payload=true` query parameter enables payload validation.

## HealthServlet

Mounted at `/health`; `GET` and `POST` have identical behavior. The content type is
`text/plain`: healthy returns status `200` and `good`; unhealthy returns status `500` and
`bad`.

## ResourcesExportServlet

Mounted at `/resources-export` and `/resources-export/*`; `GET` only. Responses are JSON
and use epoch milliseconds for timestamps. For map endpoints, optional `domain` selects a
nameserver; if omitted or blank, the first available domain is used.

### Root: `/resources-export` or `/resources-export/`

```json
{
  "domains": ["<domain>"],
  "endpoints": ["/resources", "/routing", "/netResources", "/nameServerResources", "/all"]
}
```

### `/resources-export/routing`

```json
{
  "routes": ["<Route>"],
  "alternateRoutes": ["<Route>"],
  "neighbors": ["<neighbor>"],
  "timestamp": 0
}
```

`routes` and `alternateRoutes` are Gson serializations of the routing lists. A `Route`
contains the model's serialized fields, including destination, neighbor, cost, and route
metadata; clients should treat additional fields as implementation-owned.

### `/resources-export/resources?domain=<domain>`

```json
{
  "resources": {
    "<resource id>": {
      "resId": "<resource id>",
      "netIds": {"<net id>": "<NetId>"},
      "type": "<ResourceType>"
    }
  },
  "count": 0,
  "timestamp": 0
}
```

`ResourceRoute.netIds` values are serialized `NetId` objects. Their exact fields are owned
by the generated/network model and may expand over time.

### `/resources-export/netResources?domain=<domain>`

```json
{
  "netToResources": {
    "<net id>": {
      "netId": "<net id>",
      "lastSeen": 0,
      "resourceMap": {"<resource id>": "<ResourceRoute>"}
    }
  },
  "count": 0,
  "timestamp": 0
}
```

### `/resources-export/nameServerResources?domain=<domain>`

Same envelope as `/netResources`, but the map property is `nameServersToResources`:

```json
{
  "nameServersToResources": {"<nameserver>": "<NetResources>"},
  "count": 0,
  "timestamp": 0
}
```

### `/resources-export/all?domain=<domain>`

```json
{
  "resources": {"<resource id>": "<ResourceRoute>"},
  "netToResources": {"<net id>": "<NetResources>"},
  "nameServersToResources": {"<nameserver>": "<NetResources>"},
  "exportTimestamp": 0
}
```

### Resource-export errors

Errors use:

```json
{"error": "<message>", "status": 404, "timestamp": 0}
```

The status is `404` for an unknown domain or when no nameservers are available, `503` for
an unavailable routing service, and `500` for an unexpected exception. Unknown resource
export paths return `404` with the same error shape.

## Optional CASPORT simulator

When `service.webserver.casportsim.enabled=true`, the unsecured `/rest/v3` context mounts
`GET` and `POST` at `/rest/v3/groups/{group}/members/{member}`. A successful response is:

```json
{"isMember": true}
```

or `{"isMember": false}`. The exact URL-decoded, case-sensitive pair `testgroup` and
`CN=Test Client,OU=SILKWAVE,O=JICD 4.2,L=Warrenton,ST=Virginia,C=US` returns `true`; other
valid pairs return `false`. Malformed paths return status `400` with a plain string message;
`invalidgroup` returns `400` and `Invalid group name`; `groupnotfound` returns `404` and
`Group not found`.

## Non-servlet resources

`/docs`, `/resources-docs`, `/`, CSS, JavaScript, and other static paths serve files from
the configured `WebContent` directory rather than a stable data schema. The OpenAPI
documents are available at `/docs/silkwave-web-api-openapi.yaml` and
`/resources-docs/resources-export-openapi.yaml` when those files are present.