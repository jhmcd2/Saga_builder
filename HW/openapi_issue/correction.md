Two separate things going on here — a display problem and a spec-accuracy problem. Let's take them in order.

## Why it's not showing up in Swagger UI

The YAML block you pasted for `/resources-export/routing` is syntactically valid — no indentation or parsing errors that would explain Swagger UI silently dropping just that one entry. If the YAML had a real syntax error, `js-yaml` would fail to parse the *whole* document and you'd get a "Failed to load API definition" error, not a UI that renders everything else fine and just quietly omits one path.

That points strongly at **stale content being served, not a spec problem**. Two likely culprits:

1. **Browser caching the static `.yaml` file.** `index.html` fetches it via a plain relative path with no cache-busting, and Jetty's `DefaultServlet` doesn't set aggressive no-cache headers by default — so the browser can happily keep serving you the pre-edit version from cache.
2. **You edited a copy that isn't the one actually being served.** Worth confirming there's only one `silkwave-web-api-openapi.yaml` under your `WebContent/docs/` (or wherever you landed it) and that it's the file you actually edited.

Quickest way to tell which: hit the raw spec file directly in the browser and search it —

```
http://localhost:8501/docs/silkwave-web-api-openapi.yaml
```

Ctrl+F for `routing` on that page. If it's there → it's a browser cache issue on the *rendered* `/docs/` page (hard refresh: Ctrl+Shift+R, or open in an incognito window). If it's **not** there → you're editing/deploying a different file than the one being served, and it's a deployment-path mismatch, not caching.

Longer-term fix for #1: add a cache-buster in `index.html` so this doesn't bite you again every time you edit the spec:

```js
url: "./silkwave-web-api-openapi.yaml?v=" + Date.now(),
```

## The bigger issue: the schema doesn't match reality

Separately — the response body you pasted from the actual live endpoint:

```json
{
  "routes": [],
  "alternateRoutes": [],
  "neighbors": [],
  "timestamp": 1785253325424
}
```

doesn't match what your YAML currently documents. You wrote the `200` response as a **bare array** of `{routeId, destination}` objects. The real response is an **object** with four fields (`routes`, `alternateRoutes`, `neighbors`, `timestamp`), and none of those field names match `routeId`/`destination`. Once the display issue is sorted, this would show up in Swagger UI looking plausible but describing something that isn't what the servlet actually returns.

Here's a corrected block matching the observed shape — since all three arrays were empty in your sample, I don't know their item structure yet, so I've left those as loosely-typed `object` with a note rather than guessing field names:

```yaml
  /resources-export/routing:
    get:
      operationId: getResourceExportRouting
      summary: Read-only export of NameServer routing table
      description: >
        Exposes routing state: known routes, alternate routes, and neighbor list.
        This path is whitelisted from Form authentication because it falls under the
        `/resources-export/*` prefix.
      tags:
        - resources-export
      security: []
      responses:
        "200":
          description: Successfully retrieved routing table export.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RoutingExportResponse'
        "500":
          description: Internal server error mapping routing tables.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
```

And add this schema alongside your others:

```yaml
    RoutingExportResponse:
      type: object
      properties:
        routes:
          type: array
          description: >
            Item structure not yet confirmed — observed empty in testing.
            Populate item schema once a non-empty response is available.
          items:
            type: object
            additionalProperties: true
        alternateRoutes:
          type: array
          items:
            type: object
            additionalProperties: true
        neighbors:
          type: array
          items:
            type: object
            additionalProperties: true
        timestamp:
          type: integer
          format: int64
          description: Epoch millis.
      required: [routes, alternateRoutes, neighbors, timestamp]
```

Also worth a quick check on your end: your `handleDomainList` (in the `resourcesExportServelet.java` you pasted) already lists `/routing` in the `endpoints` array it returns, which is good — that means `DomainListResponse`'s `endpoints` example in the spec (`["/resources", "/netResources", ...]`) is now slightly stale too, missing `/routing`. Minor, but worth updating the example list while you're in there.

Once you get a real (non-empty) `routes`/`alternateRoutes`/`neighbors` sample, send it over and I'll tighten up the item schemas instead of leaving them as loose objects.