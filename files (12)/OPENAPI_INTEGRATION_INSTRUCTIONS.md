# Wiring OpenAPI/Swagger UI into SILKWAVE WebServer

Two files were generated:

```
silkwave-web-api-openapi.yaml   — the OpenAPI 3.1 spec
swagger-ui/index.html           — self-contained Swagger UI page (CDN-loaded assets)
swagger-ui/silkwave-web-api-openapi.yaml — same spec, colocated with the HTML for convenience
```

Everything below is instructions only — no more files needed from you for *this* step, since it's just static content plus a couple of lines in `WebServer.java`.

---

## 1. Drop the files into the static content root

`WebServer` already serves static content via `DefaultServlet` from whatever `service.webserver.content.location` points at (default: `WebContent`):

```java
context.setResourceBase(webcontent);
...
context.addServlet(DefaultServlet.class, "/");
```

Copy the `swagger-ui/` folder as-is into that content root, e.g.:

```
WebContent/
├── login.html
├── css/
├── js/
└── docs/                  <-- new
    ├── index.html
    └── silkwave-web-api-openapi.yaml
```

(Renaming the folder to `docs/` is just a suggestion for a clean URL — `swagger-ui/` works identically.)

No Gradle dependency changes are needed — the Swagger UI JS/CSS load from `cdnjs.cloudflare.com` at request time in the browser, not at build time. If the deployment environment has no outbound internet access from client browsers, see §4 for vendoring the assets instead.

## 2. Whitelist the docs path from authentication (HTTP mode)

Right now the default constraint in `WebServer.java` requires auth on `/*`, and only a specific list of paths is excluded (`/health`, `/resources-export/*`, login assets). Add a matching exclusion for the docs folder, next to the existing whitelist entries (around where `/resources-export/*` is added):

```java
constraintMapping = new ConstraintMapping();
constraintMapping.setConstraint(constraint);   // reuses the same "no auth" Constraint object
constraintMapping.setPathSpec("/docs/*");
securityHandler.addConstraintMapping(constraintMapping);
```

Without this, the docs page will redirect to `/login.html` in HTTP mode (same behavior every other admin path already has). If you're running HTTPS-only, recall from the earlier report that no security handler gets attached at all in that branch — the docs page (like everything else) will simply be open, no change needed there.

If you'd rather keep the docs page behind authentication (arguably more appropriate for an internal admin API), skip this step entirely — it'll just prompt for login like everything else.

## 3. Verify

Start the server, then hit:

```
http://<host>:8501/docs/index.html
```

(or `https://<host>:8601/docs/index.html` in HTTPS mode). You should see the Swagger UI page listing the five documented servlets.

## 4. Optional: vendor the Swagger UI assets instead of using the CDN

If outbound internet from the browser isn't available in your deployment (air-gapped, classified network, etc.), replace the two CDN `<link>`/`<script>` tags in `index.html` with locally-hosted copies:

1. Download the `swagger-ui-dist` npm package contents (`swagger-ui.min.css`, `swagger-ui-bundle.min.js`) once, from a machine that does have internet access.
2. Drop them into `WebContent/docs/vendor/`.
3. Change the two tags in `index.html`:
   ```html
   <link rel="stylesheet" href="./vendor/swagger-ui.min.css">
   ...
   <script src="./vendor/swagger-ui-bundle.min.js"></script>
   ```

No Java/Gradle changes needed for this either — it's still just static files.

---

## 5. Extending the spec for the remaining servlets

The spec currently covers `HealthServlet`, `SilkwaveAdminServlet`, `NamingServiceAdminServlet`, `ResourcesExportServlet`, and `CasportV3SimServlet`. Still undocumented (registered in `WebServer.java` but their source wasn't reviewed):

- `DiscoveryServiceAdminServlet`
- `NetworkStatusAdminServlet`
- `RegistrationAdminServlet`
- `RoutingAdminServlet`
- `SecurityAdminServlet`
- `NamingAdminServlet`
- `StreamAdminServlet`
- `FileAdminServlet`

Since you can't upload more files right now, here's how to extend the YAML yourself once you have access to that source (or paste the relevant servlet code into a future chat and ask me to do it):

### What to pull out of each servlet's `doGet`/`doPost`

1. **The path** it's mounted at — check `WebServer.java`'s `context.addServlet(...)` call for that class.
2. **Dispatch parameter**, if any — most of these servlets follow the same `?action=` pattern as `SilkwaveAdminServlet`/`NamingServiceAdminServlet`. Look for `request.getParameter("action")` and the `if`/`switch` branches on it.
3. **Other query params read per action** — grep for `request.getParameter(...)` calls inside each branch.
4. **Response content type** — `response.setContentType(...)`, usually `application/json` or `text/plain`.
5. **Response shape** — since these servlets build JSON via manual `StringBuilder` concatenation (not a POJO + serializer), the field list is whatever string literals get appended. Read the append calls in order.
6. **Status codes used** — look for `response.setStatus(...)` calls, typically `SC_OK`/`SC_BAD_REQUEST`/`SC_NOT_FOUND`/`SC_INTERNAL_SERVER_ERROR`.

### Template to copy per new path

Paste this into the `paths:` section of `silkwave-web-api-openapi.yaml`, then fill in the blanks:

```yaml
  /YourServletPath:
    get:
      operationId: yourServletAction
      summary: <one line>
      tags: [admin]              # or a new tag if it deserves its own section
      parameters:
        - name: action
          in: query
          required: false
          schema:
            type: string
            enum: [actionOne, actionTwo]   # fill in from the source
      responses:
        "200":
          description: <describe per-action shape, or split into oneOf like SilkwaveAdminServlet>
          content:
            application/json:
              schema:
                type: object
                additionalProperties: true   # loosen until you've mapped every field
```

Add a matching `tags:` entry near the top of the file if you want it grouped separately in the Swagger UI sidebar.

### A note on effort vs. value

Given these servlets follow the exact same hand-rolled, `StringBuilder`-JSON, `?action=`-dispatch pattern as the ones already documented, this is mechanical but not fast — expect roughly the same amount of source-reading per servlet as went into the five already covered. There's no shortcut via annotation scanning here (as covered earlier), so it's a straight read-and-transcribe job each time.
