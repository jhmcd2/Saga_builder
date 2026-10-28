# Original
core/core/src/main/java/gov/ic/silkwave/routing/RoutingService.java 
        return tables.get().getPrimaryRoutes();
    }

    
    public RouteTables getRouteTables() {
        return tables.get();
    }
Comment on lines +148 to +151
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
I would avoid a getter that accesses the RouteTables directly. I think it would be better to emulate the other functions above and do something like...

public List<Route> getAltRoutes() { return tables.get().getAltRoutes(); }
Then later you could replace the instantiation of tables with just one call to this method alongside the other similar functions.

Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644

        RoutingService routingService = ServiceLocator.getRoutingService();

        if (routingService == null) {
            resp.setStatus(503);
            writeError(resp, "Routing service unavailable", 503);
            return;
        }

        RouteTables tables = routingService.getRouteTables();

        Map<String,Object> result = new LinkedHashMap<>();

        result.put("routes", tables.getPrimaryRoutes());
        result.put("alternateRoutes", tables.getAltRoutes());
        result.put("neighbors", tables.getNeighborNames());
Comment on lines +78 to +84
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
I would remove the first line of this, so with my above comment implemented too, this block would look like:

    Map<String,Object> result = new LinkedHashMap<>();

    result.put("routes", routingService.getPrimaryRoutes());
    result.put("alternateRoutes", routingService.getAltRoutes());
    result.put("neighbors", routingService.getNeighborNames());
I think we shouldn't have a getter exposed for the RouteTables directly since it may be risky and it deviates from the standing pattern of how we grab information from this table.

Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        RoutingService routingService = ServiceLocator.getRoutingService();

        if (routingService == null) {
            resp.setStatus(503);
            writeError(resp, "Routing service unavailable", 503);
            return;
        }

        RouteTables tables = routingService.getRouteTables();

        Map<String,Object> result = new LinkedHashMap<>();

        result.put("routes", tables.getPrimaryRoutes());
        result.put("alternateRoutes", tables.getAltRoutes());
        result.put("neighbors", tables.getNeighborNames());
        result.put("timestamp", System.currentTimeMillis());
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
Instead of System.currentTimeMillis(), we are moving to using java.time.Instant. So this would be...

result.put("timestamp", Instant.now().toEpochMilli());
Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        result.put("neighbors", tables.getNeighborNames());
        result.put("timestamp", System.currentTimeMillis());

        writeJson(resp, result);
    }
    
    // GET /resources-export/resources?domain=...
    private void handleResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        ConcurrentMap<String, ResourceRoute> data = ns.getResources();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
Same replacement with java.time.Instant

Edited 1 week ago by Kaitlin Nibeck krorr
Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        result.put("resources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
    }

    // GET /resources-export/netResources?domain=...
    private void handleNetResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        ConcurrentMap<String, NetResources> data = ns.getNetToResources();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("netToResources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
java.time.Instant

Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        result.put("netToResources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
    }

    // GET /resources-export/nameServerResources?domain=...
    private void handleNameServerResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        ConcurrentMap<String, NetResources> data = ns.getNameServersToResources();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nameServersToResources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
java.time.Instant

Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        result.put("nameServersToResources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
    }

    // GET /resources-export/all?domain=...
    private void handleAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resources", ns.getResources());
        result.put("netToResources", ns.getNetToResources());
        result.put("nameServersToResources", ns.getNameServersToResources());
        result.put("exportTimestamp", System.currentTimeMillis());
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
java.time.Instant

Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
            writeError(resp, "No nameservers available", 404);
            return null;
        }

        return NameServerLocator.getNameServer(domains.get(0));
    }

    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        resp.getWriter().write(gson.toJson(obj));
    }

    private void writeError(HttpServletResponse resp, String message, int status) throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        error.put("status", status);
        error.put("timestamp", System.currentTimeMillis());
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
java.time.Instant

Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
            return null;
        }

        return NameServerLocator.getNameServer(domains.get(0));
    }

    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        resp.getWriter().write(gson.toJson(obj));
    }

    private void writeError(HttpServletResponse resp, String message, int status) throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        error.put("status", status);
        error.put("timestamp", System.currentTimeMillis());
        resp.getWriter().write(gson.toJson(error));
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
We are replacing gson in the codebase, so instead of this, do:

import static gov.ic.silkwave.JsonMapper.MAPPER;

...

resp.getWriter().write(MAPPER.writeValueAsString(error));
Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
package gov.ic.silkwave.web;

import com.google.gson.Gson;
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
Remove Gson import, see below for fix to where this is used.

Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import gov.ic.silkwave.routing.RouteTables;
import gov.ic.silkwave.routing.RoutingService;
import gov.ic.silkwave.ServiceLocator;


public class ResourcesExportServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ResourcesExportServlet.class);
    private static final Gson gson = new Gson();
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
Remove line

Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        }

        // No domain specified - use first available
        List<String> domains = NameServerLocator.getNameServerDomains();
        if (domains.isEmpty()) {
            resp.setStatus(404);
            writeError(resp, "No nameservers available", 404);
            return null;
        }

        return NameServerLocator.getNameServer(domains.get(0));
    }

    private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        resp.getWriter().write(gson.toJson(obj));
    }
Comment on lines +171 to +173
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
Not needed, replacing with in line suggestion where this is called.

Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
                }
            }
        } catch (Exception e) {
            log.error("Error handling resources-export request", e);
            resp.setStatus(500);
            writeError(resp, "Internal server error: " + e.getMessage(), 500);
        }
    }

    // GET /resources-export/
    private void handleDomainList(HttpServletResponse resp) throws IOException {
        List<String> domains = NameServerLocator.getNameServerDomains();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("domains", domains);
        result.put("endpoints", List.of("/resources", "/routing","/netResources", "/nameServerResources", "/all"));
        writeJson(resp, result);
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
Replace function call with:

resp.getWriter().write(MAPPER.writeValueAsString(result));
Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        if (routingService == null) {
            resp.setStatus(503);
            writeError(resp, "Routing service unavailable", 503);
            return;
        }

        RouteTables tables = routingService.getRouteTables();

        Map<String,Object> result = new LinkedHashMap<>();

        result.put("routes", tables.getPrimaryRoutes());
        result.put("alternateRoutes", tables.getAltRoutes());
        result.put("neighbors", tables.getNeighborNames());
        result.put("timestamp", System.currentTimeMillis());

        writeJson(resp, result);
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
Replace with:

resp.getWriter().write(MAPPER.writeValueAsString(result));
Edited 1 week ago by Kaitlin Nibeck krorr
Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        result.put("timestamp", System.currentTimeMillis());

        writeJson(resp, result);
    }
    
    // GET /resources-export/resources?domain=...
    private void handleResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        ConcurrentMap<String, ResourceRoute> data = ns.getResources();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
Replace with:

resp.getWriter().write(MAPPER.writeValueAsString(result));
Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
    }

    // GET /resources-export/netResources?domain=...
    private void handleNetResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        ConcurrentMap<String, NetResources> data = ns.getNetToResources();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("netToResources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
Replace with:

resp.getWriter().write(MAPPER.writeValueAsString(result));
Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
    }

    // GET /resources-export/nameServerResources?domain=...
    private void handleNameServerResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        ConcurrentMap<String, NetResources> data = ns.getNameServersToResources();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nameServersToResources", data);
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr
@krorr
1 week ago
Owner
Replace with:

resp.getWriter().write(MAPPER.writeValueAsString(result));
Reply…
Kaitlin Nibeck krorr
Kaitlin Nibeck krorr @krorr started a thread on the diff 1 week ago
core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java  0 → 100644
        result.put("count", data.size());
        result.put("timestamp", System.currentTimeMillis());
        writeJson(resp, result);
    }

    // GET /resources-export/all?domain=...
    private void handleAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        NameServer ns = resolveNameServer(req, resp);
        if (ns == null) return;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resources", ns.getResources());
        result.put("netToResources", ns.getNetToResources());
        result.put("nameServersToResources", ns.getNameServersToResources());
        result.put("exportTimestamp", System.currentTimeMillis());
        writeJson(resp, result);



# ChatGPT cleared up
Based on the review comments, only 2 Java files need to be updated. Most of the fixes are in `ResourcesExportServlet.java`, while `RoutingService.java` needs a new accessor method and removal of the direct `RouteTables` getter.

## 1. Files that need to be fixed

1. `RoutingService.java`

`core/core/src/main/java/gov/ic/silkwave/routing/RoutingService.java`

- Remove `getRouteTables()`.
- Add `getAltRoutes()` to follow the existing accessor pattern.
- Ensure the servlet can access the alternate routes without exposing `RouteTables`.

2. `ResourcesExportServlet.java`

`core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java`

- Replace all `System.currentTimeMillis()` calls with `Instant.now().toEpochMilli()`.
- Replace Gson serialization with `MAPPER.writeValueAsString(...)`.
- Remove the Gson import and `Gson` instance.
- Remove the unnecessary `writeJson()` helper.
- Use the individual `RoutingService` accessor methods instead of `getRouteTables()`.

## 2. Exact changes to make

### File 1: `RoutingService.java`

Add the alternate-route accessor alongside the existing route accessors:

java

```
public List<Route> getAltRoutes() {
    return tables.get().getAltRoutes();
}
```

Keep the existing accessors for primary routes and neighbors, assuming they already exist:

java

```
public List<Route> getPrimaryRoutes() {
    return tables.get().getPrimaryRoutes();
}

public List<String> getNeighborNames() {
    return tables.get().getNeighborNames();
}
```

Use the actual return types from your existing methods if they differ. Then remove:

java

```
public RouteTables getRouteTables() {
    return tables.get();
}
```

### File 2: `ResourcesExportServlet.java`

A. Update imports

Remove:

java

```
import com.google.gson.Gson;
import gov.ic.silkwave.routing.RouteTables;
```

Add:

java

```
import java.time.Instant;

import static gov.ic.silkwave.JsonMapper.MAPPER;
```

Keep the other imports that are still used.

B. Remove the Gson instance

Delete:

java

```
private static final Gson gson = new Gson();
```

C. Update the routing response

Replace:

java

```
RouteTables tables = routingService.getRouteTables();

Map<String,Object> result = new LinkedHashMap<>();

result.put("routes", tables.getPrimaryRoutes());
result.put("alternateRoutes", tables.getAltRoutes());
result.put("neighbors", tables.getNeighborNames());
result.put("timestamp", System.currentTimeMillis());

writeJson(resp, result);
```

With:

java

```
Map<String, Object> result = new LinkedHashMap<>();

result.put("routes", routingService.getPrimaryRoutes());
result.put("alternateRoutes", routingService.getAltRoutes());
result.put("neighbors", routingService.getNeighborNames());
result.put("timestamp", Instant.now().toEpochMilli());

resp.getWriter().write(MAPPER.writeValueAsString(result));
```

D. Replace timestamps throughout the servlet

There are 7 occurrences of `System.currentTimeMillis()` in the provided code. Replace each with:

java

```
Instant.now().toEpochMilli()
```

These occur in:

- `handleRouting()`
- `handleResources()`
- `handleNetResources()`
- `handleNameServerResources()`
- `handleAll()`
- `writeError()`

The review also shows the timestamp in the routing response separately; count each actual occurrence in your file to ensure none are missed.

E. Replace all JSON serialization calls

Every occurrence of:

java

```
writeJson(resp, result);
```

should become:

java

```
resp.getWriter().write(MAPPER.writeValueAsString(result));
```

Likewise, replace:

java

```
resp.getWriter().write(gson.toJson(error));
```

with:

java

```
resp.getWriter().write(MAPPER.writeValueAsString(error));
```

Then delete the now-unnecessary helper:

java

```
private void writeJson(HttpServletResponse resp, Object obj) throws IOException {
    resp.getWriter().write(gson.toJson(obj));
}
```

## 3. Final checklist

### Review-fix checklist

0 of 8

[ ]Add getAltRoutes() to RoutingService.java

[ ]Remove getRouteTables() from RoutingService.java

[ ]Update ResourcesExportServlet to use individual routing accessors

[ ]Import java.time.Instant and replace all currentTimeMillis() calls

[ ]Import JsonMapper.MAPPER and remove Gson

[ ]Replace all writeJson() and gson.toJson() usages

[ ]Delete the writeJson() helper

[ ]Compile and run tests

Bottom line: You have 2 files to modify, with most of the work concentrated in `ResourcesExportServlet.java`. The review comments do not identify any other files that must be changed.