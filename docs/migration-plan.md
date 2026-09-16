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
  resources under `META-INF/`, and a qualified `opens` to the container where
  a module wants its implementations injected. Notably we give up **nothing**
  here: implementation packages stay unexported and encapsulated, and
  declarative field injection keeps working. See the JPMS section below for
  the measurements behind this.
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
4. **Every component POM must pin the version of every SciJava artifact it
   consumes, transitively included.** The modules here inherit from
   `pom-scijava`, not from this repository's aggregator POM, so a version
   property that is merely *absent* silently resolves the last *released*
   artifact instead of the reactor's snapshot. Nothing enforces the "these
   versions must match across all component POMs" convention, and the failure
   is invisible until an API changes — at which point it surfaces as
   `NoSuchMethodError` or `NoClassDefFoundError` at test time, far from its
   cause. Six components were found in this state while moving interfaces into
   `scijava-spi`. **An enforcer rule for this would pay for itself**; it is
   listed under open decisions.

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
- `scijava-ops-image` / `scijava-ops-flim` tests — `io.location.FileLocation`,
  imposed by SCIFIO's `ImgOpener` API. Gone once SCIFIO migrates; the utility
  usages were removed in Phase 0.
- `scijava-ops-benchmarks` — three imports.

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

**A JPMS constraint discovered while implementing this:** `ServiceLoader`
resolves `uses` declarations against the module of its *caller*, so
`scijava-discovery` cannot perform a `ServiceLoader` lookup on another
module's behalf — doing so fails for every type that `org.scijava.discovery`
does not itself declare `uses` for. The lookup function must therefore be
supplied by the consuming module:

```java
Discoverer.usingProviders(c -> ServiceLoader.load(c).stream())
```

The one exception is `Discoverer.all()`, which looks up `Discoverer` itself —
a type this module does declare `uses` for — and so can be a no-argument
convenience. This is a real constraint on any generic discovery facade under
JPMS, not an artifact of our design.

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

These were measured, not reasoned about, with a three-module test project
(`javac --module-source-path`, run on the module path, JDK 21):

| Situation | Result |
| --- | --- |
| Index resources (`META-INF/json/...`) read via `ClassLoader.getResources` | **works** — `META-INF` is not a valid package name, so it is never encapsulated |
| `ServiceLoader` finding a provider declared with `provides`, in a package that is neither exported nor opened | **works** — the `provides` declaration itself grants the access |
| Reflective `newInstance` on a public class in a package that is neither exported nor opened | **fails**: `IllegalAccessException` |
| The same, with a qualified `opens <pkg> to <container>` | **works** |
| `setAccessible` injection into a **private** field, with that qualified `opens` | **works** |
| An ordinary module importing a class from that opened-but-unexported package | **compile error**: "package ... is declared in module ..., which does not export it" |
| A service with a **private** constructor plus a `public static provider()` factory | **works** — `ServiceLoader` prefers the factory |

The load-bearing conclusion is the second-to-last row: **`opens` is not
`exports`.** A qualified `opens` grants the container deep reflective access at
runtime while leaving the package invisible to ordinary callers at compile
time. Implementations therefore stay encapsulated — callers cannot import
them, cannot cast to them, and cannot call their non-API methods — and the
container can still construct and inject them.

So the SJ3 context keeps **declarative field injection**, as SciJava Common
has always had. The cost to a plugin module is one line:

```java
opens my.plugins.impl to org.scijava.context;
```

### Cancellation is thread interruption

SciJava has invented several cancellation APIs over the years — `Cancelable`
in SJC's `module` package and across ImageJ Ops, the cancel half of
`task.Task`, and Appose's `cancel()`. SciJava Ops deliberately shipped without
any of them. The replacement is the platform's own mechanism, not a fourth
invention.

- **Mechanism:** thread interruption. Every interruptible JDK blocking call
  already honors it — `Thread.sleep`, `Object.wait`, `BlockingQueue`,
  `Lock.lockInterruptibly`, NIO channels — and Java 21's
  `StructuredTaskScope` cancels its subtasks by interrupting them, so this
  stays current.
- **Client API:** `ExecutorService.submit()` → `Future.cancel(true)`. That is
  what a caller holds: cancellation, completion and result in one object every
  Java developer already knows.
- **No `Cancelable`, and no cancellation token parameter.** Every scheme is
  cooperative, so that is not the differentiator; composition is. An ambient
  thread flag is honored by code we did not write, while an `isCanceled()`
  interface only works for code that knows about it, and a token pollutes
  every signature while still being unable to abort a JDK blocking call.

**An interrupt carries no reason.** `Thread.interrupt()` takes no argument and
the JDK constructs `InterruptedException` with a null message, so a reason
cannot ride along. A reason belongs to the requesting side — the `Future`, or
the caller's own record of why it cancelled — not to the exception.

#### Cooperating in code that cannot throw

Ops are `Function`, `Computers.Arity*` and similar, whose methods declare no
checked exceptions, so an op cannot throw `InterruptedException`. The idiom:

```java
if (Thread.interrupted()) {            // NB: this clears the flag...
    Thread.currentThread().interrupt(); // ...so restore it
    throw new CancellationException("op cancelled");
}
```

`java.util.concurrent.CancellationException` is unchecked and is what
`Future.get()` already throws for a cancelled task, so callers see one type.

**`Progress.update()` is the checkpoint.** Ops that do long work already call
it periodically, so making it check for interruption gives cancellation with
no new API, no extra parameter, and no per-op work. Progress and cancellation
are both ambient and thread-scoped; they belong on the same rail. An op that
reports no progress is not cancellable, which is honest, and a nudge toward
reporting progress.

#### Known limits, accepted deliberately

- `CompletableFuture.cancel()` does **not** interrupt the running thread; it
  only completes the future exceptionally. Expose `ExecutorService` futures
  where cancellation must reach a compute loop.
- The interrupt flag is thread-bound and **does not inherit**, unlike
  `Progress`, which is an `InheritableThreadLocal`. So `Parallelization` and
  `TaskExecutor` must propagate cancellation to worker futures deliberately.
  That is the one piece of real work this design implies.
- Plain `InputStream`/`FileInputStream` reads are not interruptible; only NIO
  channels are. Blocking I/O therefore aborts between operations, not during
  one.
- Native code ignores interrupts: a long OpenCV call cannot be cancelled
  mid-call.
- The common practical hazard is library code that catches
  `InterruptedException` and swallows it. The rule is always: propagate it, or
  restore the flag.

#### Across a process boundary

Interruption cannot cross one, so **Appose's explicit `cancel()` is correct as
it stands**. In-process: interruption. Across processes: an explicit protocol.

#### Consequences for this migration

1. `Cancelable` is **not ported** (see [migration.md](migration.md)).
2. The Phase 3 execution layer hands back a `Future`; module and op execution
   submit to an executor, and cancellation is `future.cancel(true)`.
3. `Progress.update()` checks for interruption and throws
   `CancellationException`.
4. `Parallelization` propagates cancellation to its workers.
5. `DataHandles.copy` checks once per block and throws
   `java.io.InterruptedIOException`, which is an `IOException` — so it fits
   the existing signature — and whose `bytesTransferred` field reports the
   partial copy. The granularity is per-block, identical to the
   `task.isCanceled()` check it replaces.

### Logging

SLF4J, used directly, as `scijava-types` and `scijava-ops-engine` already do.
`log` is not ported. An SJC `LogService` → SLF4J bridge is built if needed.

## Phases

### Phase 0 — Groundwork

- Remove SJC usage where an SJ3 replacement already exists: switch the
  `ops-image` / `ops-flim` tests to `common3.MersenneTwisterFast` and
  `collections.LongArray`, replace `ListUtils`. **Done.** Note that the
  remaining `io.location.FileLocation` usage in the `ops-image` and `ops-flim`
  tests is imposed by SCIFIO's `ImgOpener` API, so it cannot go away until
  SCIFIO migrates; `scijava-ops-tutorial` turned out to have no SJC usage.
- Add `@deprecated` javadoc pointers in SJC for everything already superseded
  (see the table above).
- Start [migration.md](migration.md), scripting the mapping where possible.
  Note that `common3.Types` is not a superset of `util.Types`: `box`, `field`,
  `isBoolean` and friends moved to `Classes`, and `enumFromString` / `args`
  were dropped.
- Refactor `Discoverer` to the `Discovery` descriptor API (see above).
  **Done.**

### Phase 1 — Standalone leaves

No application context required by anything in this phase.

- **`scijava-spi`** (new): pure interfaces, no behavior, no dependencies.
  **Done**, with `Versioned`, `GenericTyped`, `Named`, `Identifiable` and
  `Disposable`. `Versioned` and `GenericTyped` moved here from `common3` and
  were **not** left behind as deprecated aliases, which the repository's
  current adoption level permits.
  - The test for membership: *if a class can implement it without knowing
    about any SciJava framework, it is SPI.* `Cancelable` fails that test —
    and is not ported at all, since cancellation is thread interruption — as
    do `Validated`, `BasicDetails`,
    `UIDetails` and `Plugin` (metadata bags for the plugin/module layer) and
    `Initializable` (lifecycle, so it belongs with `scijava-context`).
  - `Named` exposes `String name()` only. Mutability is a separate contract;
    a default `setName` that throws `UnsupportedOperationException` forces
    every caller to feature-detect.
  - **Deferred, pending a consumer:** `Locatable`, whose only real content was
    a default implementation that needs `common3.Classes.location` — which
    this module cannot depend on; and `Typed`, which has no consumer until the
    plugin layer exists, and whose `Class<T> type()` would collide awkwardly
    with `GenericTyped.type()` returning a `Type`. Both are cheap to add when
    something needs them; neither is cheap to remove once published.
- **`scijava-common3`** absorbs the remaining general utilities: `ArrayUtils`,
  `StringUtils`, `Bytes`, `DigestUtils`, `NumberUtils`, `ProcessUtils`,
  `ListUtils`, `UnitUtils`, and the non-URL parts of `FileUtils`. It stays
  dependency-free. (`Timing` is on hold — unclear downstream usage.)
- **`scijava-index`** (new, `org.scijava.index`): ports `annotations` — the
  annotation processor and index reader — plus `IndexDiscoverer`, a
  `Discoverer` backed by the index. **Done.** This is what supplies plugin
  metadata without loading classes, and it is what the `Discovery` refactor
  was for: a test proves that discovery never asks the class loader for an
  implementation class, using a recording class loader rather than a static
  flag, since reading such a flag would itself load the class.
  - The index format (`META-INF/json/<annotation>`) is deliberately unchanged
    from SJC, so an index written by either side is readable by the other.
  - Which type an indexed item provides must be answerable from metadata,
    so `IndexDiscoverer` takes a function for it; for an annotation shaped
    like `@Plugin(type = Service.class)` that is
    `item -> item.annotation().type().getName()`, which loads the *declared*
    type but never the implementation.
  - The component sets `maven.compiler.proc` to `none` for itself: javac
    reads `META-INF/services` before the processor class it names exists.
    SJC has the same constraint, and solves it the same way.
  - Still to do: the loud runtime diagnostic for an annotated class missing
    from the index. It belongs with whichever component defines the
    annotation, so it lands in Phase 2.
- **`scijava-io3`** (new, `org.scijava.io3.{location, handle, nio}`): ports
  `io.location`, `io.handle`, `io.nio` and `ByteBank`. Locations are **done**;
  handles are next. The external `scijava-io-http` folds in later as
  `org.scijava.io3.http`.
  - **This component stays standalone**: it is broadly useful on its own — to
    Bio-Formats, for instance — so it depends on nothing but
    `scijava-collections`, and resolvers are discovered with **plain
    `ServiceLoader`** rather than `Discoverer` or a container. Nothing here
    requires buying into a framework.
  - That works because the `uses`-resolves-against-the-caller constraint only
    bites a *generic* facade discovering arbitrary types on another module's
    behalf. This component knows its own service type, so it declares
    `uses org.scijava.io3.location.LocationResolver` in its own
    `module-info.java` and is self-contained.
  - `LocationResolver` carries its own `priority()` as a plain `double`
    rather than depending on `scijava-priority`; the `Priority` constants are
    compatible with it. `Locations` can also be constructed with an explicit
    resolver list, for embedding and testing.
  - Handles are **done** too, on the same pattern: `DataHandle` absorbs the
    tiny `WrapperPlugin` contract (`locationType`, `supports`, `set`, `get`,
    `priority`) instead of extending the plugin framework, and
    `DataHandleService` becomes `DataHandles`.
  - Note that discovered handles are **prototypes**: a handle carries the
    position and buffers of one open stream, so `create()` instantiates a
    fresh one per call. SciJava Common got this from `WrapperService`; here
    it is explicit, and tested.
  - `DataHandles.copy` took a `Task`, which carried both progress *and
    cancellation*; its replacement `LongConsumer` carries only progress.
    Cancellation is thread interruption instead — see the cancellation
    section — so `copy` checks the flag once per block and throws
    `InterruptedIOException`.
  - Still to port: `io.nio`.
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
  lifecycle and dependency injection; plugin metadata from `scijava-index`;
  instantiation via `Discoverer`; ordering via `scijava-priority`. Plus the
  core services: object registry, prefs, thread, app/status.
  - **Dependencies are not constructor arguments.** Constructor injection
    would publish every dependency in a signature, so changing an internal
    dependency would be an API change and a binary compatibility break unless
    old constructors were kept forever. It also cannot work for the
    `ServiceLoader` tier, which requires a public no-arg constructor or a
    static `provider()` factory. Dependencies are instead acquired after
    construction — declaratively, by annotated field, as SciJava Common does —
    which keeps them an implementation detail.
  - Services may therefore have a **private** constructor and a
    `public static provider()` factory, exposing no construction API at all.
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
  Cancellation needs nothing here: the runner hands back a `Future`, and
  cancelling it interrupts the worker.
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
- **A build-time check that every consumed SciJava artifact is version-pinned
  in the consuming POM** — see hard constraint 4. Today this is caught only by
  a full clean build after an API change.
- **`display`:** what the redesigned output-presentation mechanism looks like.
