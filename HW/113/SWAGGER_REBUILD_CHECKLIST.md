# Rebuilding ONLY your Swagger/OpenAPI work on top of the integration team's tree

Scope: this is deliberately narrow. It covers only what *you* added for the resources-export API + OpenAPI/Swagger UI docs. It ignores everything else in the tree diff (the module deletions, the Kotlin DSL migration, their test suite, their record/DTO refactor) — that's their mess to fix, not yours. Confirmed from the tree comparison: **none of your Swagger/OpenAPI work exists in `new_tree.md`** — no `ResourcesExportServlet.java`, no `WebContent/docs/`, no OpenAPI yaml anywhere. So this is a clean re-apply, not a merge.

Everything you need was already built and verified in this conversation. Re-attach these four pieces to whatever tree you end up rebasing onto.

---

## 1. `NameServer.java` — 3 getter methods

Per `NAMESERVER_EXPORT_API.md`, add to `core/core/.../gov/ic/silkwave/nameserver/NameServer.java` (file not touched by the integration team either way — confirm the class still has `resources`, `netToResources`, `nameServersToResources` fields and the `read`/`write` `ReentrantReadWriteLock` before pasting):

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

No new imports needed — `ConcurrentHashMap` was already imported.

## 2. `ResourcesExportServlet.java` — new file, drop in as-is

**Location:** `core/core/src/main/java/gov/ic/silkwave/web/ResourcesExportServlet.java`

This is attached unchanged (`ResourcesExportServlet.java` in this delivery) — it's the exact file you uploaded earlier in this conversation, confirmed to match the version that was in `tree.md`. It has no dependency on anything the integration team touched (uses `NameServerLocator`, `NameServer`, `NetResources`, `ResourceRoute`, GSON — none of those were renamed in the diff).

## 3. `WebServer.java` — registration + security whitelist

You'll be adding these to **whatever `WebServer.java` you end up with after the rebase**, not to the copy attached here (that copy is from before the integration team's changes and is for reference only). Three additions, in order:

**a. Servlet registration** (near your other `context.addServlet(...)` calls):
```java
ServletHolder exportHolder = new ServletHolder(new ResourcesExportServlet());
context.addServlet(exportHolder, "/resources-export/*");
context.addServlet(exportHolder, "/resources-export");
```

**b. Static docs mount** (serves the Swagger UI bundle — adjust the `webcontent` variable name to whatever the rebased file calls its `WebContent` base path):
```java
ServletHolder resourcesDocsHolder = new ServletHolder("resources-docs", DefaultServlet.class);
resourcesDocsHolder.setInitParameter("resourceBase",
    new File(webcontent, "resources-docs").getAbsolutePath());
resourcesDocsHolder.setInitParameter("pathInfoOnly", "true");
context.addServlet(resourcesDocsHolder, "/resources-docs/*");
```
(Use `/docs` + a `docs` subfolder instead of `/resources-docs` if you'd rather serve the consolidated spec at that mount — see file 4 below for which spec goes where.)

**c. Security whitelist** — inside the HTTP `ConstraintSecurityHandler` block, alongside your other public/no-auth `ConstraintMapping`s:
```java
ServletConstraint publicConstraint = ConstraintSecurityHandler.createConstraint("public", false, null, 0);

ConstraintMapping constraintMapping = new ConstraintMapping();
constraintMapping.setConstraint(publicConstraint);
constraintMapping.setPathSpec("/resources-docs");
securityHandler.addConstraintMapping(constraintMapping);

constraintMapping = new ConstraintMapping();
constraintMapping.setConstraint(publicConstraint);
constraintMapping.setPathSpec("/resources-docs/*");
securityHandler.addConstraintMapping(constraintMapping);

ServletConstraint noAuth = ConstraintSecurityHandler.createConstraint("none", false, null, 0);

constraintMapping = new ConstraintMapping();
constraintMapping.setConstraint(noAuth);
constraintMapping.setPathSpec("/resources-export/*");
securityHandler.addConstraintMapping(constraintMapping);

constraintMapping = new ConstraintMapping();
constraintMapping.setConstraint(noAuth);
constraintMapping.setPathSpec("/resources-export");
securityHandler.addConstraintMapping(constraintMapping);
```
**Both** `/resources-export` (bare) and `/resources-export/*` need their own `ConstraintMapping` — this is the gap that got flagged earlier in the original technical report; don't let it regress.

> If whatever `WebServer.java` you rebase onto is still on the old Jetty `Constraint` API (pre-migration), you'll hit the same `Constraint` → `ServletConstraint`/`ConstraintSecurityHandler.createConstraint(...)` conversion we already did once — same pattern as above, just adapt the class names.

## 4. OpenAPI spec + Swagger UI

Two options depending on how much of the consolidated spec (19 paths, covering servlets beyond resources-export) survived on your side vs. theirs:

- **`resources-export-openapi.yaml`** (attached) — scoped to just the 7 `/resources-export/*` paths, self-contained, zero dependency on anything else in the tree. Use this if you just want *your* endpoint documented and don't want to reconcile with whatever OpenAPI content (if any) the integration team's side has.
- **`silkwave-web-api-openapi.yaml`** (attached) — the full 19-path consolidated spec, if you want everything documented in one file.

Either way, drop the chosen yaml + the offline Swagger UI bundle (`swagger-ui-offline/` or `resources-docs-offline/`, both attached, fully vendored — no CDN calls) into the `WebContent/resources-docs/` (or `WebContent/docs/`) folder you create under `core/core/`, matching whichever mount you wired up in step 3b.

---

## Verify

Once rebuilt and running:
```bash
curl http://localhost:8501/resources-export/
curl http://localhost:8501/resources-export/all
curl http://localhost:8501/resources-docs/          # Swagger UI loads, no CDN calls in network tab
```
