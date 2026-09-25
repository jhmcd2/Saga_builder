# WebServer.java — Items to Confirm With Original Developer

Reviewed against the version received after the Jetty 9→12 (EE9) migration was merged in,
and after the `Constraint` → `ServletConstraint` API fix (already applied — see note at bottom).
The items below are things that compile and probably run fine, but look like leftovers from a
merge rather than deliberate design. None of these are things I changed — flagging them for
the other developer to confirm intent before anyone "cleans them up" and accidentally removes
something load-bearing.

---

## 1. Three separate "no-auth" constraints that all do the same thing

Three different `ServletConstraint` objects are created in three different places, and all three
are built identically — `createConstraint("none", false, null, 0)` (no roles, no authentication
required):

| Variable | Where declared | What it's used for |
|---|---|---|
| `constraint` | main constructor, right after the docs/resources-docs block | `/resources-export`, `/resources-export/*` |
| `none` | main constructor, immediately after `constraint` | `/jquery-ui/*`, `/css/login.css`, `/css/login-error.css`, `/jquery/*` (again), `/js/login.js`, `/js/login-error.js`, `/health` |
| `none` (separate local variable, same name, different scope) | inside `disableTrackTrace()` | the TRACE/TRACK method-omission mapping on `/` |

Functionally these are interchangeable — they'd behave identically if consolidated into one
constant. The question is whether the split was **intentional** (e.g. each was meant to
eventually diverge — say, `resources-export` getting its own rate limiting or logging tag later)
or whether it's simply an artifact of two branches each adding their own copy during the merge.

**Ask:** Is there a reason to keep `constraint` and `none` separate, or is it safe to
consolidate to a single shared constant?

---

## 2. `/jquery-ui/*` is whitelisted twice

Two separate, identical `ConstraintMapping` entries are registered for the exact same path spec:

```java
constraintMapping.setPathSpec("/jquery-ui/*");   // first occurrence
...
constraintMapping.setPathSpec("/jquery-ui/*");   // second occurrence, same constraint, same path
```

This one predates the current Jetty migration — it was already present in the pre-migration
version of the file, so it's not something this latest merge introduced. But it's worth
confirming now while everything else is being tidied up.

**Ask:** Was the second entry meant to be a different path (a typo for something else, e.g.
`/jquery/*` is already handled separately) or is it just a duplicate that can be deleted?

---

## 3. Log message says "Websocket" in a servlet-security context

```java
for (ConstraintMapping cm : securityHandler.getConstraintMappings()) {
    log.info("Websocket Security mapping: {} authenticate={}",
            cm.getPathSpec(),
            cm.getConstraint().getAuthenticate());
}
```

This loop is logging the HTTP `ConstraintSecurityHandler`'s path/auth mappings — nothing to do
with WebSockets. An earlier version of this same loop (pre-migration) logged this as
`"JHM Security mapping: {} authenticate={}"` instead. The wording changed to "Websocket" somewhere
between then and now, which reads like text carried over from a different, unrelated file during
a merge or copy-paste, rather than an intentional rename.

**Ask:** Was "Websocket" an intentional relabel, or should this revert to something scoped to
what it's actually logging (e.g. "WebServer Security mapping")?

---

## 4. Roles declared on the default constraint that the fallback login path never grants

The default `/*` constraint requires one of three roles:

```java
ServletConstraint allow = ConstraintSecurityHandler.createConstraint(FormAuthenticator.FORM_AUTH,
        true, new String[]{"user", "admin", "moderator"}, 0);
```

But the fallback `HashLoginService` path (used whenever `service.webserver.http.realm.file` isn't
set) only ever grants a single user the `admin` role:

```java
userStore.addUser(Constants.props.getProperty("service.webserver.user", "admin"),
        new Password(Constants.props.getProperty("service.webserver.pass", "silkwave")),
        new String[]{"admin"});
```

So in the common case (no realm file configured), `user` and `moderator` are roles that exist in
the constraint but can never actually be satisfied by anyone logging in through the fallback path.
This isn't new to this merge — it's pre-existing — but it's adjacent to the auth code that just
got touched, so it seemed worth a mention while this is already under review.

**Ask:** Is a realm file always expected to be present in real deployments (making this a
non-issue in practice), or should the fallback `UserStore` also grant `user`/`moderator`, or
should the constraint's role list be narrowed to just `admin`?

---

## Not a "leftover," but flagging for completeness: two new, unreviewed servlets

`QueuesAdminServlet` (`/QueuesAdminServlet/*`) and `PropertiesServlet` (`/properties`) are newly
registered in this version. These aren't duplicates or leftovers — they're genuinely new — but no
source for either has been reviewed yet on this end, so they're not represented in the OpenAPI
spec. Not a question for the other developer about *intent*, just a note that their source will
be needed to document those two endpoints whenever it's convenient.

---

## For reference: what was already fixed (not included above, just context)

Three blocks referencing the old, no-longer-imported `Constraint` class (which would have failed
to compile under the current Jetty 12 EE9 imports) were already corrected in a prior pass:
a dead/unused default-FORM_AUTH block was removed, and the `publicConstraint` and `constraint`
(resources-export) declarations were converted to `ConstraintSecurityHandler.createConstraint(...)`.
No path specs, roles, or servlet registrations were changed as part of that fix.
