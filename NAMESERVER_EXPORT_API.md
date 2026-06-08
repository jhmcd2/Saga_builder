# NameServer Resources Export API — Implementation Notes

## Background

The goal is to expose three internal `ConcurrentMap` collections from `NameServer.java` over HTTP as JSON,
so remote systems can read the nameserver's resource state. The application is Java 17 on Gradle 5,
using Jakarta Servlets and GSON (both already in the project).

The three maps being exported:

```java
protected ConcurrentMap<String, ResourceRoute> resources;
protected ConcurrentMap<String, NetResources> netToResources;
protected ConcurrentMap<String, NetResources> nameServersToResources;
```

---

## What Already Existed

### NameServerLocator.java

`gov.ic.silkwave.nameserver.NameServerLocator` is a static registry that tracks all running
`NameServer` instances keyed by domain. It was already in the project and is the correct way
to get a `NameServer` reference from a servlet.

```java
NameServerLocator.getNameServerDomains();        // List<String> of all registered domains
NameServerLocator.getNameServer("example.com");  // returns NameServer or null
NameServerLocator.putNameServer(domain, ns);     // called by the app at startup
```

No changes needed to this file.

---

## Changes Made

### 1. NameServer.java — Three Getter Methods Added

Added at the bottom of `core/silkwave.nameserver/src/main/java/gov/ic/silkwave/nameserver/NameServer.java`:

```java
public ConcurrentMap<String, ResourceRoute> getResources() {
    read.lock();
    try {
        return new ConcurrentHashMap<>(resources);
    } finally {
        read.unlock();
    }
}

public ConcurrentMap<String, NetResources> getNetToResources() {
    read.lock();
    try {
        return new ConcurrentHashMap<>(netToResources);
    } finally {
        read.unlock();
    }
}

public ConcurrentMap<String, NetResources> getNameServersToResources() {
    read.lock();
    try {
        return new ConcurrentHashMap<>(nameServersToResources);
    } finally {
        read.unlock();
    }
}
```

Each method acquires the existing `read` lock (from the class's `ReentrantReadWriteLock`) and returns
a defensive copy. No new imports needed — `ConcurrentHashMap` was already imported.

---

### 2. ResourcesExportServlet.java — New File

**Location**: `core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java`

This is the only new file. It handles all five endpoints and uses `NameServerLocator` directly
to get the `NameServer` instance. No DTOs are needed — GSON serializes the maps directly.

**Endpoints**:

| Path | Returns |
|------|---------|
| `GET /resources-export/` | List of available domains |
| `GET /resources-export/resources` | All `ResourceRoute` entries |
| `GET /resources-export/netResources` | Local net → `NetResources` mapping |
| `GET /resources-export/nameServerResources` | Remote nameserver → `NetResources` mapping |
| `GET /resources-export/all` | All three maps in one response |

All endpoints accept an optional `?domain=` query parameter. If omitted, the first available
domain from `NameServerLocator` is used.

**Error responses**:

- `404` — domain not found, or no nameservers registered yet
- `500` — unexpected exception (message included, no stack trace exposed)

---

### 3. WebServer.java — One Line Added

In `core/core/src/main/java/gov/ic/silkwave/web/WebServer.java`, register the servlet
alongside the existing servlet registrations (around line 93):

```java
context.addServlet(new ServletHolder(new ResourcesExportServlet()), "/resources-export/*");
```

---

## Why No DTOs

An earlier version of this plan created four DTO classes (`NetIdDTO`, `ResourceRouteDTO`,
`NetResourcesDTO`, `ResourcesExportDTO`) to handle GSON serialization. These are not needed.

`ResourceRoute` and `NetResources` are plain data-holder objects. GSON serializes them
directly without circular reference issues. If GSON does fail on a specific field in the future,
add a targeted fix at that point — don't add four files preemptively.

---

## File Summary

| File | Change |
|------|--------|
| `NameServer.java` | +3 getter methods (~25 lines) |
| `ResourcesExportServlet.java` | New file (~160 lines) |
| `WebServer.java` | +1 line (servlet registration) |
| DTOs | Not needed |

---

## Quick Test

Once the application is running:

```bash
# List available domains
curl http://localhost:8080/resources-export/

# Export all resources for a domain
curl "http://localhost:8080/resources-export/resources?domain=example.com"

# Export everything at once (pretty print with jq)
curl -s http://localhost:8080/resources-export/all | jq .

# Check resource count
curl -s http://localhost:8080/resources-export/resources | jq '.count'
```

---

## Thread Safety

- The getters in `NameServer.java` use the class's existing `ReentrantReadWriteLock`
- Read locks allow concurrent requests without blocking each other
- Defensive copies prevent concurrent modification after the lock is released
- The servlet itself holds no state, so concurrent requests are safe

---

## Notes

- No new Gradle dependencies — uses existing GSON and Jakarta Servlet libraries
- The servlet follows the same pattern as other servlets in `gov.ic.silkwave.web`
- `NameServerLocator` is the correct integration point; do not try to pass `NameServer`
  as a constructor argument to the servlet, as `WebServer` may not have a reference to it
- This API is read-only by design
