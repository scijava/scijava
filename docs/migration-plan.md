# Migrating SciJava Common into SciJava3

This document plans the migration of the remaining
[SciJava Common](https://github.com/scijava/scijava-common) (SJC) subsystems
into this repository (SciJava3, SJ3).

Companion document: [migration.md](migration.md) — the class-by-class mapping
table from SJC to its SJ3 replacement (or to "dropped", or to a standard
library equivalent).

## Goal

**Deprecate all of SciJava Common. Port only what earns its place.**

The deliverable is not a reimplementation of SJC. It is:

1. a set of small, layered, dependency-light SJ3 modules covering the
   functionality that is still worth having;
2. an `@deprecated` javadoc pointer on every SJC class, naming its replacement
   — in SJ3, in the Java standard library, or nowhere;
3. a documented path by which each downstream component drops its
   `scijava-common` dependency.

## Guiding principles

- **Every line of code is a liability.** If the Java standard library covers
  it, recommend that instead and drop the class. A row in
  [migration.md](migration.md) reading "use `java.util.X`" or "no replacement"
  is a successful outcome, not a gap.
- **Classpath-first, module-path-clean.** Fiji runs on the classpath, so JPMS
  strictness binds only those who deliberately modularize. Design so that
  modularization is *possible* without contorting for it: no split packages,
  plugin classes public in exported packages, constructor injection rather
  than private-field reflection, resources under `META-INF/`. The one thing we
  give up is `setAccessible`-based injection into private fields.
- **Leave SJC alone.** No functional changes, no Java version bump, no
  delegation layer. Only `@deprecated` javadoc.
- **One-way migration.** Downstream components migrate by swapping the
  dependency, updating imports, adjusting to API changes, and bumping their
  major version. There is no two-way bridge.
- **Build bridges on demand.** Adapters between SJC and SJ3 are written when a
  concrete need appears, not on a schedule.
- **Names are case by case.** Where an SJ3 package would collide with an SJC
  one, pick a natural collision-free noun if one exists; otherwise use a `3`
  suffix (`common3`, `io3`). Every decision is recorded in
  [migration.md](migration.md).

## Hard constraints

1. **SJ3 cannot reuse SJC package names.** Fiji will have both on the
   classpath for years, and SJC's automatic module name is `org.scijava`, so
   any shared package is a JPMS split-package error. Nothing new may live in
   the root `org.scijava` package, nor in `org.scijava.{log, event, plugin,
   service, convert, io, script, module, ui, widget, command, prefs, thread,
   app, display, input, tool, menu, console, main, run, startup, text,
   download, options, object, cache, parse, platform, task, annotations,
   welcome}`.
2. **SJC targets Java 8 and will not be bumped.** It therefore cannot depend
   on SJ3. Parity between an SJC class and its SJ3 replacement is *documented*,
   not enforced by delegation, and SJC bugfixes do not flow forward. This is
   acceptable for a component in wind-down.
3. **`scijava-legacy` bridges the other way** (SJ3 → SJC), and is the only
   place in this repository permitted to depend on `scijava-common`.

## Current state

These SJ3 modules already cover SJC subsystems. They do not need migrating —
they need the corresponding SJC classes deprecated with pointers.

| SJ3 module | Supersedes in SJC |
| --- | --- |
| `scijava-common3` | `util.ClassUtils`, `util.GenericUtils`, `util.Types`, `util.AppUtils`, `util.PlatformUtils`, `util.DebugUtils`, `util.MersenneTwisterFast`, parts of `util.FileUtils` |
| `scijava-collections` | `util.*Array` |
| `scijava-meta` | `util.POM`, `util.XML`, `util.Manifest`, `util.VersionUtils` |
| `scijava-priority` | `Priority`, `Prioritized` |
| `scijava-progress` | `task` (roughly) |
| `scijava-struct` | the item model of `module` (`ModuleItem`, `ItemIO`) |
| `scijava-discovery` | the discovery role of `plugin` + `annotations` |
| `scijava-concurrent` | part of `thread` |
| SLF4J (used directly) | `log` |

Remaining SJC usage inside this repository:

- `scijava-code-api` — `Context`, `Plugin`, `Service`, `ScriptService`,
  `ModuleItem`. Intentional for now; it is the Phase 2 pilot.
- `scijava-legacy` — intentional; it is the bridge.
- `scijava-ops-image` / `scijava-ops-flim` tests — `util.MersenneTwisterFast`
  (16 usages), `util.LongArray`, `util.ListUtils`, `io.location.FileLocation`,
  `Context`. Removable today (Phase 0).
- `scijava-ops-tutorial` — one import in main code.

## Cross-cutting design decisions

### Discovery is two-tier, behind one API

- **Services** are discovered via `ServiceLoader` + `module-info` /
  `META-INF/services`. Services are a small, framework-level set written by
  developers who can manage a `provides` line. This gives JPMS-correct
  `uses`/`provides` wiring, and it breaks the bootstrap circularity in SJC
  where `PluginService` is itself a plugin found by an index.
- **The long tail** — commands, converters, widgets, formats, tools — is
  discovered via `@Plugin` and the generated index. This is where the
  zero-configuration ergonomics matter, and where reading metadata *without
  loading classes* matters (Fiji menu construction).
- `Service` does **not** extend `SciJavaPlugin`.

Both tiers surface through one `Discoverer` API, so consumers never care which
mechanism found a thing.

### `Discoverer` returns descriptors, not instances

The current signature `<U> List<U> discover(Class<U>)` returns constructed
objects. That forces eager instantiation, provides no metadata without class
loading, and carries no priority, attributes or provenance. An index-backed
`Discoverer` under this signature would have to throw away the index's one
advantage.

Change it to yield lazy descriptors:

```java
interface Discovery<T> {
  String implClassName();       // no class loading
  Map<String, String> attrs();  // menu path, label, priority, ...
  double priority();
  Class<? extends T> type();    // loads the class
  T get();                      // instantiates
}
```

All three mechanisms map cleanly: `ServiceLoader.stream()` supplies
`Provider::type` (loads without instantiating) and `Provider::get`; the
`@Plugin` index supplies `implClassName` and `attrs` with no loading at all;
op YAML files already have this shape.

**This refactor should land before `scijava-context` is built on top of
`Discoverer`.** It is mechanical now and expensive later. `ManualDiscoverer`
— hand-feeding implementations, which is valuable for tests, scripts and
embedding — is preserved.

### Annotation processing keeps `@Plugin` viable, with guards

`@Plugin` is retained: adding one annotation and having everything work,
with no configuration files, has been of large value to the SciJava community.
The risks are in *downstream* builds, not ours:

- Java 23+ no longer runs annotation processors found on the classpath
  implicitly. `pom-scijava-base` sets this up for SciJava components, but a
  hand-rolled Maven or Gradle build silently produces no index, and the plugin
  simply never appears.

Mitigations to build:

1. Generate the index from a Maven plugin **as well as** from the annotation
   processor, so a build that misses annotation processing still works.
2. Emit a loud runtime diagnostic when a `@Plugin`-annotated class is absent
   from the index, naming the exact fix. This converts a silent failure into a
   one-line answer.

`annotations.EclipseHelper` is **not** ported: modern m2e `.factorypath`
generation covers it, and the directory-indexing capability belongs in the
Maven plugin (under a name that does not say "Eclipse", since it runs
elsewhere too).

### JPMS specifics

- Index resources are safe: `META-INF/json/...` is not a valid package name,
  so it stays readable via `ClassLoader.getResources` from named modules.
- Instantiating a public class with a public no-arg constructor in an
  **exported** package needs no `opens`.
- `setAccessible` on private fields — how SJC injects `@Parameter`
  (`Context.java:510`, `ServiceHelper.java:317`) — **does** require `opens`.
  This is the one thing forcing every plugin module to open itself.

Therefore the SJ3 context uses **constructor injection** (or an explicit
`initialize(Context)`), so that `exports` suffices and `@Plugin` coexists with
JPMS.

### Logging

SLF4J, used directly, as `scijava-types` and `scijava-ops-engine` already do.
`log` is not ported. An SJC `LogService` → SLF4J bridge is built if needed.

## Phases

### Phase 0 — Groundwork

- Remove SJC usage where an SJ3 replacement already exists: switch the
  `ops-image` / `ops-flim` tests to `common3.MersenneTwisterFast` and
  `collections.LongArray`, replace `ListUtils`, fix the `ops-tutorial` import.
  Afterwards only `scijava-legacy` and `scijava-code-api` depend on SJC.
- Add `@deprecated` javadoc pointers in SJC for everything already superseded
  (see the table above).
- Start [migration.md](migration.md), scripting the mapping where possible.
  Note that `common3.Types` is not a superset of `util.Types`: `box`, `field`,
  `isBoolean` and friends moved to `Classes`, and `enumFromString` / `args`
  were dropped.
- Refactor `Discoverer` to the `Discovery` descriptor API (see above).

### Phase 1 — Standalone leaves

No application context required by anything in this phase.

- **`scijava-spi`** (new): pure interfaces, no behavior, no dependencies —
  `Versioned`, `GenericTyped`, `Named`, `Identifiable`, `Locatable`, `Typed`,
  `Disposable`. `Versioned` and `GenericTyped` move here from `common3`, which
  retains deprecated sub-interfaces so implementors stay source-compatible.
  `common3` does **not** gain a transitive dependency on it.
  - The test for membership: *if a class can implement it without knowing
    about any SciJava framework, it is SPI.* `Cancelable` fails that test
    (cancellation concerns a running thing — it belongs with the execution
    layer, near `Progress`/`Task`), as do `Validated`, `BasicDetails`,
    `UIDetails` and `Plugin` (metadata bags for the plugin/module layer) and
    `Initializable` (lifecycle, so it belongs with `scijava-context`).
  - `Named` exposes `String name()` only. Mutability is a separate contract;
    a default `setName` that throws `UnsupportedOperationException` forces
    every caller to feature-detect.
- **`scijava-common3`** absorbs the remaining general utilities: `ArrayUtils`,
  `StringUtils`, `Bytes`, `DigestUtils`, `NumberUtils`, `ProcessUtils`,
  `ListUtils`, `UnitUtils`, and the non-URL parts of `FileUtils`. It stays
  dependency-free. (`Timing` is on hold — unclear downstream usage.)
- **`scijava-index`** (new, `org.scijava.index`): ports `annotations` — the
  annotation processor and index reader — plus a `Discoverer` backed by the
  index. This is what supplies plugin metadata without loading classes.
- **`scijava-io3`** (new, `org.scijava.io3.{location, handle, nio}`): ports
  `io.location`, `io.handle`, `io.nio` and `ByteBank`. Handles and resolvers
  are found via `Discoverer` + `Priority`. The external `scijava-io-http`
  folds in later as `org.scijava.io3.http`.
- **`scijava-events`** (new, `org.scijava.events`): a clean-room event bus,
  no context, designed for typed topics and weak-reference subscribers. Not a
  port of the bushe fork — rewriting removes the attribution obligation. The
  external `scijava-listeners` folds in here.
- **Key/value parsing moves to Parsington**, not into a SJ3 module. The whole
  SJC `parse` package is 319 lines whose only non-Parsington dependency is
  `ObjectArray` (replaceable with `ArrayList`), and Parsington already carries
  an `eval` package and a CLI. This adds no dependencies there and deletes a
  module from this plan.
- **Not ported:** `log` (SLF4J), `cache` (see Phase 2), build-time and
  obsolete utilities (`Combiner`, `MetaInfCombiner`, `ServiceCombiner`,
  `CheckSezpoz`, `MirrorWebsite`, `TunePlayer`, `ReflectedUniverse`,
  `util.Prefs`, `ConversionUtils`), and the collection odds and ends
  (`LastRecentlyUsed` → `LinkedHashMap` with `accessOrder`; `IteratorPlus` →
  nothing; `SizableArrayList` → nothing, as its reflective resizing rationale
  died with Java 17; `TreeNode` → on hold, only `imagej-common` uses it).

### Phase 2 — The application context

- **`scijava-context`** (new, `org.scijava.context`): services with a
  lifecycle and **constructor-based** dependency injection; plugin metadata
  from `scijava-index`; instantiation via `Discoverer`; ordering via
  `scijava-priority`. Plus the core services: object registry, prefs, thread,
  app/status.
  - Deliberately a much smaller surface than SJC's `Context` +
    `PluginService` + `PluginInfo` + the handler/wrapper/typed-service
    hierarchy. Keep what is used; drop the rest.
  - **Keep the class name `Context`.** The scripting layer controls the
    parameter alias table, so `#@ Context ctx` can resolve to whichever class
    we choose; script compatibility does not force a rename, and the decision
    can be made late.
- **`scijava-convert3`** (name TBD): ported, not replaced by Ops. Implicit
  parameter conversion and `getCompatibleInputs` are foundational to the
  execution layer and the input harvester — `imagej-legacy`'s
  `DatasetToImagePlusConverter` and friends are the proof. A `Converter` SPI
  plus a convert service discovered via `Discoverer`, with **one** adapter
  exposing Ops conversions as converters. Ops matching-based conversion stays
  a distinct mechanism joined by that adapter, not merged.
- **`scijava-cache`** moves into this repository, keeping the
  `org.scijava.cache` package, with its SJC dependency repointed to
  `scijava-context`. It never reached 1.0.0, so there is no SemVer concern.
- **One-way bridge only:** a service hosting an SJC `Context` inside the SJ3
  context, plus preprocessors that inject legacy objects into existing
  scripts. Nothing is added to SJC.
- **Pilot:** port `scijava-code-api` off SJC. It is new, small, and already
  exercises `Context`, `Plugin` and `Service`.
- Fiji startup can then construct the SJ3 `Context` instead of the SJC one.

### Phase 3 — Execution layer

- **Factor the framework-neutral member parsing out of `scijava-ops-engine`**
  into `scijava-struct` (or a `scijava-struct-reflect` beside it):
  `ClassParameterMemberParser`, `Field`/`MethodParameterMemberParser`,
  `FunctionalParameters`, `SynthesizedParameterMember`. The Op-specific pieces
  (`OpDependency` members, retyping/resizing) stay in `ops-engine`. The
  execution layer must **not** depend on `ops-engine`; if a dependency is
  useful it runs the other way, `ops-engine` → execution layer.
- **Execution module** (name TBD): pre/postprocessor chains, input resolution,
  a `@Parameter` equivalent, and a runner — built on `scijava-struct`.
  `Cancelable`'s replacement lives here.
- **Commands:** a struct, plus menu metadata, plus `run`. Commands and Ops
  remain separate concepts sharing this layer.
- **Scripting:** `javax.script` is not general enough. GraalVM's Truffle
  languages expose `org.graalvm.polyglot.Context` (a `ScriptEngine` shim loses
  polyglot bindings, sandboxing and resource limits), and Appose-based Python
  runs out of process, which `javax.script` cannot model at all. So:
  - `scijava-scripting-api`: a small `ScriptLanguage`/`ScriptEngine` facade
    with **no `requires java.scripting`**;
  - separate adapter modules for `javax.script`, GraalVM polyglot, and Appose.

  `#@` parameters parse into structs. The external `scripting-*` repositories
  migrate onto the facade.

### Phase 4 — UI and desktop

- **Input harvesting is a headline deliverable**, not a port. It is the
  workhorse that lets Fiji users write scripts without UI-specific concerns.
  Build it on `scijava-struct`, and complete the long-requested
  **dynamic subgroups** (widgets appearing based on another widget's value) —
  see [scijava-common#42](https://github.com/scijava/scijava-common/issues/42#issuecomment-332658377)
  and the prototype preserved on the `historical/scijava-ops-prototype` branch
  (`widget/`, `swing-widget/`), which is the design starting point rather than
  `org.scijava.widget` as written.
- **Drop** the opinionated application shell: `ApplicationFrame`, `Desktop`,
  `StatusBar`, `ToolBar`. The SJ3 UI API is minimal.
- **`input` and `tool`** are re-framed, not frozen: UI-agnostic facades over
  GUI concepts, layered on `scijava-events`, letting code listen for GUI
  events or implement interactive behavior (e.g. a pencil tool) without
  binding to Swing or JavaFX.
- **`display`** gets an overhaul, not a port. The role — matching a module's
  outputs to a way of presenting them, as `DisplayPostprocessor` does — is
  worth keeping; the current design is overengineered.
- **`platform` and `ui.dnd`** feed a **`scijava-desktop` modernization
  track**: desktop integration, drag-and-drop and `fiji://` links matter to
  the community and must keep working.
- **CLI module:** `console`, `main`, `run`. **`startup` stands alone** — it is
  relevant whether the application starts from the CLI or a GUI.
- **Likely dropped:** `welcome`, `download`, `text`, `options`.

## Per-subsystem disposition

| SJC package | Disposition |
| --- | --- |
| `util` | Split across `common3`, `collections`, `meta`, `spi`; remainder dropped |
| `annotations` | → `scijava-index` (Phase 1) |
| `io.location`, `io.handle`, `io.nio` | → `scijava-io3` (Phase 1) |
| `event` | → `scijava-events`, clean-room rewrite (Phase 1) |
| `parse` | → Parsington (Phase 1) |
| `log` | Dropped — use SLF4J |
| `cache` | External `scijava-cache` moves in (Phase 2) |
| `thread` | → `scijava-concurrent` |
| `task` | → `scijava-progress` |
| `Context`, `plugin`, `service`, `object`, `prefs`, `app` | → `scijava-context` (Phase 2) |
| `convert` | → `scijava-convert3` (Phase 2) |
| `module`, `command` | → execution layer (Phase 3) |
| `script` | → scripting facade + adapters (Phase 3) |
| `widget` | → input harvesting, redesigned (Phase 4) |
| `ui`, `menu` | Minimal UI API; app shell dropped (Phase 4) |
| `input`, `tool` | Re-framed as UI-agnostic facades (Phase 4) |
| `display` | Overhauled, not ported (Phase 4) |
| `platform`, `ui.dnd` | `scijava-desktop` modernization (Phase 4) |
| `console`, `main`, `run` | Single CLI module (Phase 4) |
| `startup` | Standalone module (Phase 4) |
| `welcome`, `download`, `text`, `options` | Likely dropped |
| `minimaven`, `test` | Out of scope |

## Per-subsystem recipe

1. Port the code and tests (JUnit 4 → JUnit 5) with a `module-info.java`, no
   SJC dependency, and no split packages.
2. Add an adapter in `scijava-legacy` only if a concrete need exists.
3. Mark the SJC counterpart `@deprecated` with a pointer, and add rows to
   [migration.md](migration.md).
4. Update the downstream components that were using it.

## Scope: satellite repositories

The end state is that this repository contains all *core* SciJava3
subsystems, with no dependency on external `scijava-<foo>` components.
`scijava-cache`, `scijava-listeners`, `scijava-optional`, `scijava-table`,
`scijava-search` and `scijava-log-slf4j` all move in over time. Components
intended to work standalone — Parsington, for instance — keep their
independence and should not carry the `scijava-` prefix.

## Open decisions

- **Names:** the execution-layer module, and `scijava-convert3`.
- **Splitting `scijava-legacy`** into an SJC-only bridge and a separate
  ImageJ-specific module, so that a bridge needing only SJC does not drag in
  `imagej-common`. Agreed in principle; not blocking, so it is deferred until
  the bridge content grows.
- **`util.Timing` and `util.TreeNode`:** port or drop, pending evidence of
  downstream usage.
- **`display`:** what the redesigned output-presentation mechanism looks like.
