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
- **Incarnational versioning.** The `3` in `common3` and `io3` is not a
  workaround for a package collision; it is the component's *incarnation*.
  Breaking changes are made by forking to a new incarnation with a new
  artifactId and package prefix, so both incarnations can sit on one classpath
  and downstream code keeps working. See the
  [incubator README](https://github.com/scijava/incubator/blob/-/README.md).
  - **A major version bump within one incarnation should never happen.** If
    compatibility must break, bump the incarnation digit — artifactId, package
    prefix and major version together — so the old one can stay loaded
    alongside.
  - Ideally the major version equals the incarnation number
    (`scijava-common3` at `3.x.y`). Today it is at `1.x.y`, because the first
    release of this reactor was a single monoversioned `1.0.0` big bang:
    releasing a multi-module reactor piecemeal with heterogeneous versions
    needs tooling support that `release-version.sh` does not yet have. The
    realignment opportunity is the removal of the interfaces that moved to
    `scijava-spi`, which breaks compatibility anyway and could land as `3.0.0`.
    `scijava-io3` is unreleased, so it starts at `3.0.0-SNAPSHOT` already;
    releasing a reactor with heterogeneous versions still awaits the
    `release-version.sh` work.
- **Names are otherwise case by case.** Where an SJ3 package would collide
  with an SJC one and no incarnation digit applies, pick a natural
  collision-free noun. Every decision is recorded in
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
| Reflective construction of a class in a package opened **to the container**, performed by a *different* module (e.g. a shared discovery library) | **fails**: `IllegalAccessException` — the check is against the module that performs it |

The load-bearing conclusion is the second-to-last row: **`opens` is not
`exports`.** A qualified `opens` grants the container deep reflective access at
runtime while leaving the package invisible to ordinary callers at compile
time. Implementations therefore stay encapsulated — callers cannot import
them, cannot cast to them, and cannot call their non-API methods — and the
container can still construct and inject them.

That last row is easy to design past and costly to discover late: it is not
enough to know *that* reflection needs an `opens`, one must know **which
module the `opens` names**. Generic discovery code cannot construct a plugin on
a container's behalf, so the container passes an instantiator defined in its
own module, and users open their packages to the container. `Discovery.of` and
`IndexDiscoverer` therefore take an optional instantiator.

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
  `Progress`, which is an `InheritableThreadLocal`. Propagation to workers is
  therefore deliberate. In `TaskExecutors` most of it was already there:
  `ExecutorService.invokeAll` cancels its unfinished tasks — interrupting
  them — when the awaiting thread is interrupted. What was missing was the
  other half: the `InterruptedException` was wrapped in a plain
  `RuntimeException` and **the caller's own flag was never restored**, so
  cancellation died at that layer. It now restores the flag and throws
  `CancellationException`.
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
4. `Parallelization` propagates cancellation to its workers: `TaskExecutors`
   restores the interrupt flag and throws `CancellationException` rather than
   swallowing the interrupt in a `RuntimeException`.
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
  - From `io.nio`, `ByteBufferByteBank` **is** ported: a direct buffer is
    off-heap, so its bytes reach native code and shared memory without a
    copy — which matters increasingly for Appose's named shared memory
    blocks. `NIOService` is dropped, being two JDK calls behind a
    system-property switch; nothing in the ported architecture precludes
    reintroducing `FileChannel` later, since a handle may open a channel and
    a bank may be backed by a mapped buffer. This component is therefore
    complete.
- **`scijava-events`** (new, `org.scijava.events`): a clean-room typed event
  bus. **Done.** Not a port of the bushe fork, so the attribution obligation
  is gone. The external `scijava-listeners` stays a separate concern: a
  listener list is the right primitive for "this object notifies its
  observers", and is not a bus.
  - **Lambdas, not annotations.** `subscribe(Class<E>, Consumer<? super E>)`
    is type-safe, needs no reflection, and so needs no `opens`.
    `@EventHandler` ergonomics are not lost: `scijava-context` already
    reflects over services, so it can scan annotated methods, register them as
    lambdas, and hold the subscriptions. The convenience lives in the tier
    that already pays for reflection.
  - **Strong references plus an explicit `Subscription`.** SciJava Common held
    subscribers weakly, which produced handlers that silently stopped firing
    and forced a `WeakHashMap` named `keepEm` whose only job was stopping the
    proxies from being collected too early — the implementation fighting its
    own design. Cleanup is structural instead: `EventBus.close()` drops every
    subscription, and a context calls it on dispose, so a service that
    subscribes never arranges its own cleanup.
  - **No shared instance, deliberately.** A bus is nothing but shared mutable
    subscriber state, so an ambient singleton would let two application
    contexts hear each other's events. Each context owns one bus. NB: this is
    the opposite call from `Locations.get()` and `DataHandles.get()`, which
    are safe to share precisely because they hold no such state.
  - **Class-hierarchy dispatch** is retained — it is what makes a bus more
    than a listener list — and any object may be an event, with no marker
    interface, so a type from another library can be one.
  - **Consumption is opt-in**, via a `Consumable` interface on the event, on
    the model of AWT's `InputEvent.consume()`. Toolkit-style input events can
    then stop propagation without every event paying for the concept.
  - **Subscriber order** is by `scijava-priority`, descending, ties broken by
    subscription order. It matters most for consumable events, where an
    earlier subscriber gets first refusal.
  - **A failing subscriber does not truncate delivery.** Everyone still
    receives the event, and the failures are thrown together afterward as an
    `EventDeliveryException` — first as cause, rest suppressed, none lost.
    Cancellation is the exception: an interrupted subscriber, or one throwing
    `CancellationException`, stops delivery at once, since an interrupt means
    this thread is being cancelled and must not be buried among unrelated
    failures.
  - **`EventHistory` is dropped.** The real need behind it — a macro recorder
    that tracks module executions so a user can review what they did — is
    better served by an explicit layer later; note that SciJava Ops already
    tracks execution provenance per output object (`OpHistory.executionsUpon`),
    which answers "what produced this?" rather than "what happened?".
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
  - **The container core is done**: `Service`, `Context`, lazy creation,
    priority selection, lifecycle, disposal, and one `EventBus` per context.
    `scijava-context-test` is a sibling module — the only way to test the
    claims that matter, since a module must really declare `provides`, and its
    implementation package must really be unexported, for the test to mean
    anything. It verifies that a service in a package that is neither exported
    nor opened is still discovered and constructed.
  - **Services are discovered as `Service`, never as their own interface.**
    `ServiceLoader` resolves `uses` against its *caller's* module, so a
    container cannot look up an arbitrary third-party interface on that
    module's behalf. Declaring one base type that this module `uses` is what
    makes discovery work under JPMS at all; the container then sorts
    implementations by the interfaces they implement. `Provider.type()` keeps
    that cheap, since implementations of other types are never constructed.
  - **Two services may depend on each other**, because dependencies are
    acquired in `initialize` rather than in the constructor — a cycle that
    constructor injection could not resolve at all. The cost is that a service
    in a cycle may briefly see a peer that has not finished initializing.
  - **Disposal reverses the order in which services finished initializing**,
    not the order they were requested, so a service is always torn down before
    the ones it depends on.
  - **The plugin tier is done**: `@Plugin` (indexable, with `@Attr`) and
    `Context.plugins(Class)`, which returns `Discovery` descriptors from the
    annotation index — class name, metadata and priority, with nothing loaded
    until asked. Tested through a real build in `scijava-context-test`:
    annotated classes in an unexported package, indexed by the annotation
    processor at compile time, discovered with their metadata, and constructed
    only on demand.
  - **A downstream project must put the processor on the annotation processor
    path** (as the Ops modules do for `scijava-ops-indexer`). javac will not
    find it on the module path, and says nothing when it finds no processor at
    all: the build simply produces no index and the plugins silently fail to
    appear. This is the hazard noted above, met first-hand. `pom-scijava`
    configuring this for SciJava components would remove the trap for the
    people most likely to fall into it.
  - **Field injection is done**: `@Dependency` fills in services, the
    `Context` and the `EventBus`, after construction and before
    `Service.initialize`. It is deliberately not called `@Parameter` as in
    SciJava Common, where that annotation meant two unrelated things — a
    dependency to inject, and an input to a module — the second of which is
    now `org.scijava.struct`'s business.
    `scijava-context-test` injects **private** fields of a plugin across a
    real module boundary, proving both halves of the earlier claim at once:
    the qualified `opens` grants the container deep reflection, and the
    package is still exported to nobody.
  - **`@EventHandler` scanning is done**, and it is what the two-tier design
    was protecting: the event bus itself stays reflection-free, while the
    container — which already reflects — turns annotated methods into
    subscriptions. A service's handlers are subscribed when the context
    creates it and dropped when the context is disposed, so a service author
    writes no subscription or cleanup code at all.
  - Objects the context did **not** create are not subscribed automatically:
    a plugin may be created and discarded many times in a session, and
    handlers outliving their objects would pile up. `Context.subscribe(Object)`
    returns the subscriptions for the caller to close.
  - What a handler throws reaches the publisher unwrapped, rather than as an
    `InvocationTargetException`.
  - Still to do here: the one-way legacy bridge, and the core services
    themselves.
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

- **Factoring the member parsing out of `scijava-ops-engine`** is wanted, but
  the coupling is deeper than the file names suggest. Inspecting the nineteen
  classes in `ops.engine.struct`, only a handful are framework-neutral
  (`FieldInstance`, `ParameterData`, `FunctionalMethodType`,
  `SynthesizedParameterMember`, `RetypingRequest`). The parsers themselves
  reach into Op-specific exceptions (`FunctionalTypeOpException`,
  `NullablesOnMultipleMethodsException`), Op-specific annotations
  (`@Nullable`, `@OpDependency` in `scijava-ops-spi`), and `ops.engine.util`
  helpers. Lifting them means first deciding where an "optional parameter"
  annotation lives, and `scijava-ops-spi` is already released.
  - So: build the execution layer first and let a **second real consumer**
    show which abstractions are genuinely shared, rather than guessing from
    the Ops side alone. That is the same discipline applied to `ObjectService`
    and the settings store.
  - The execution layer must **not** depend on `ops-engine`; if a dependency
    is useful it runs the other way.
- **`scijava-execute`** (`org.scijava.execute`): pre/postprocessor chains,
  input resolution, a `@Parameter` equivalent, and a runner — built on
  `scijava-struct`. Cancellation needs nothing of its own: a run is a
  `Callable` for an executor, and cancelling the `Future` interrupts it.
  - **Started**: `@Parameter` on fields, a `MemberParser` that reads them
    (inherited ones included), and `Executables.run`, which binds inputs by
    name and returns outputs by name.
  - `@Parameter` here means only what it meant in SciJava Common's *module*
    sense — an input or output of something runnable. Injecting a service is
    `@Dependency`, in `scijava-context`. Conflating those two is what made the
    original confusing.
  - **A required input must be supplied, not merely non-null.** Checking the
    field instead would let a missing required `double` run silently as `0.0`,
    since a primitive field is never null.
  - **The processor chain is done**: `Preprocessor` and `Postprocessor`
    plugins, and `Runner.run`, which is always asynchronous and returns
    `Future<ExecutionResult>`. One verb covers both cases, since a caller
    wanting to block writes `run(...).get()` — as SciJava Common's
    `ModuleService` has long demonstrated.
  - **Declining is not cancellation.** A preprocessor may stop a run before it
    happens — the user closed the dialog, or a precondition is unmet — which
    is an ordinary outcome, not an error and not interruption of work under
    way. It is reported by `ExecutionResult.isDeclined()`, with `reason()` for
    the user and `declinedBy()` naming the preprocessor.
  - **`ExecutionResult` is deliberately not a `Future`.** A future's
    `isCancelled()` and `get()` are coupled by contract — whenever the former
    is true the latter must throw — so modelling a declined run as a cancelled
    future turns it back into an exception, which returning a result was meant
    to avoid. The asynchrony lives in `Future<ExecutionResult>`; what happened
    lives in the result.
  - **`declinedBy()` returns the preprocessor instance, not its class**, since
    a chain may hold two instances of one class and the useful question is
    which of them objected.
  - **Declining lives on the execution, not the preprocessor.** SciJava Common
    put `isCanceled()`/`getCancelReason()` on the plugin, which made
    preprocessors stateful and unsafe to share across runs. Telling the
    execution keeps them stateless — which matters here, since `Context`
    constructs a fresh plugin instance per call.
  - Still to do here: factoring the framework-neutral member parsing out of
    `ops-engine` — which is a larger job than it looks, see below.
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

## Layering: how Fiji gets built on SJ3

The problem with SciJava Common was never that it had menus. It is that the
*core* had them: one artifact bundles the plugin framework, the application
container, menus, displays, tools and a UI opinion, so a library wanting only
plugin discovery inherits `MenuPath`, `Display` and a transitive AWT flavour.

The remedy is **stratification, not deletion**. Everything Fiji needs still
exists; it moves to a layer that can be depended on separately. Where this
document says a subsystem is "dropped", that means *dropped from the core* —
unless it says there is no replacement at all, which it says explicitly.

| Layer | Contents | Depends on |
| --- | --- | --- |
| **0. Foundation** (built) | `spi`, `common3`, `collections`, `priority`, `progress`, `concurrent`, `discovery`, `index`, `struct`, `io3`, `events`, `context`, `execute` | nothing above |
| **1. Application model** (built, less `scijava-desktop`) | `scijava-command` (a struct plus presentation metadata, the menu tree and its walk), `scijava-ui3` (toolkit-agnostic contracts), `scijava-desktop` | layer 0 |
| **2. Toolkit bindings** | `scijava-awt` (and the AWT platform layer the others use), `scijava-swing`, `scijava-javafx`, headless | layer 1 |
| **3. Application** | Fiji: which menus exist, branding, update sites, defaults | layer 2 |

Only layer 2 imports AWT or Swing. Layer 0 never mentions a user interface, so
headless is not a mode — it is what the core does.

### The mechanisms are already in place

The machinery the application shell needs was built in phases 1-3, and in
several cases was built *because* of it:

| Fiji needs | Built |
| --- | --- |
| Menus over hundreds of commands, without loading their classes | the `@Plugin` index, and `Discovery.implClassName()`/`attrs()` before `type()`/`get()` |
| Parameter dialogs | `Preprocessor`: harvesting is a preprocessing step |
| "The user pressed Cancel" | `Execution.decline(reason)`, reported by `ExecutionResult` |
| Showing outputs | `Postprocessor`: the `DisplayPostprocessor` seam |
| Tool and input events, one handler claiming a keystroke | `scijava-events`, with `Consumable` |
| Menu ordering | `scijava-priority`, plus a weight attribute in the index |

NB: SciJava Common builds its menu from `ModuleInfo`, whose
`getDelegateClassName()` names the class without loading it and whose
`loadDelegateClass()` loads on demand. That is exactly the
`implClassName()`/`type()`/`get()` split of `Discovery`, so the expensive part
of the menu story is already solved and tested.

### What changes, and what does not

**All of the presentation metadata survives** — menu path, label, icon,
accelerator, weight, selection group, visibility, enablement. Fiji's menus are
built from it, and hundreds of commands depend on it.

**It becomes data rather than an inheritance chain.** SciJava Common has
`BasicDetails` → `UIDetails` → `AbstractUIDetails`, which every plugin extends
in order to carry a label. Here it is attributes in the annotation index, read
without loading, with a command-info view over `Discovery`. The same
information, fewer types, a faster menu build. Expect commands to look like:

```java
@Command(menuPath = "Image>Adjust>Brightness/Contrast...", weight = 12,
         accelerator = "^C", iconPath = "/icons/bc.png")
public class BrightnessContrast implements Runnable {
	@Parameter private Dataset image;
}
```

`@Command` as its own `@Indexable` annotation: `IndexDiscoverer` is generic
over the annotation type, so a dedicated, self-documenting annotation costs
nothing compared with stuffing this into `@Plugin` attributes.

**Reworked rather than ported:** `AbstractUIDetails`'s inheritance, the
`display` package's `DisplayService`/`Display`/`DisplayViewer`/`DisplayPanel`
layering, and the `Gateway`/`SciJava` convenience object. Those are shape
problems; the capabilities survive.

### The real risk is migration, not architecture

Hundreds of commands live across dozens of repositories with different
maintainers. Per command the change is mechanical, but there is no flag day on
which they all update.

So the **one-way legacy bridge matters more than its deferral implies**: for
Fiji to run on SJ3 before every command is ported, an SJ3 application must
present SciJava Common `ModuleInfo`s as SJ3 commands — metadata flowing both
ways, dependency flowing one way. That is what makes the migration incremental
rather than a big bang, and it should be sequenced *with* layer 1 rather than
after it.

### Conversion

**`scijava-convert3`** (`org.scijava.convert3`) is built, and deliberately
sits *below* the execution layer rather than beside it: binding an input to a
parameter converts, and a script, a command line and a text field all have
only strings to offer. It therefore depends on nothing but `scijava-common3`
and `scijava-priority`, and finds its converters through plain
`ServiceLoader` - no container, no configuration.

- **One question, not eight.** `Converter.supports(Object, Type)` asks whether
  *this value* can become *that type*. SciJava Common had eight `canConvert`
  overloads across `Object`/`Class`/`Type` pairings, plus a
  `ConversionRequest` to carry the combinations, plus an `AbstractConverter`
  to make the overloads bearable. The declared `sourceType()`/`destType()`
  answer the common case by themselves, so
  `Converter.of(String.class, Foo.class, Foo::parse)` is a whole converter.
- **One converter per concern**, rather than one `DefaultConverter` holding
  every special case in sequence: cast, number, string-to-value, to-string,
  array, collection, file/path, and a wrapping constructor last. Each is
  discoverable, orderable by priority, and replaceable by a downstream
  component that disagrees.
- **Unconvertible is not zero.** `tryConvert` returns empty where a value
  cannot become the type, and `convert` throws. Text that is half-typed
  converts to nothing, which is what lets a widget leave a parameter alone
  while the user is still typing - a distinction SciJava Common could not
  make, since it returned null for both "no" and "null".
- **Exactness where the type asked for it**: a `BigDecimal` from a `double`
  goes through the string, so `0.1` is `0.1` and not the binary double nearest
  to it.
- **It removed duplication rather than adding a layer.** The UI's
  `Widgets.toType` - three bindings' worth of text parsing - is now one call
  into it, so a widget turning text into a value does exactly what a script
  does.

Still open: an `ObjectService` equivalent (what values *exist* that could be
converted into this parameter) is what a chooser widget wants, and is a
separate question from what conversions are possible. It lands with the object
widget.

### Scripting

**`scijava-script3`** (`org.scijava.script3`) is built, and the shape of it is
one claim: **a script is an `Executable` like any other**. It runs through the
same `Runner`, meets the same preprocessors, harvests its inputs through the
same dialog in whichever toolkit, and can sit in a menu beside a Java command.
Nothing above this layer knows which it is - a claim now tested, in
`scijava-swing`, by rendering a Groovy script's parameters with the Swing
widgets and watching its callbacks fire.

- **`javax.script` is the first backend, not the only one.** `ScriptLanguage`
  and `ScriptSession` are the SPI; `JsrLanguage` turns every JSR-223 engine on
  the classpath into one, so Groovy, Jython, JRuby, Clojure and BeanShell cost
  nothing and need declare nothing. GraalVM polyglot and Appose are further
  `ScriptLanguage` implementations when they are wanted, and neither needs this
  layer to change.
- **Callbacks work for scripts**, which SciJava Common never managed.
  `ExecutableInstance.behavior(String)` resolves a name however that kind of
  code does, and for a script that means a function the script defined - so
  `#@ double(callback = "celsiusChanged") celsius` behaves exactly as the Java
  annotation does. What the function changed is converted back into the
  parameters afterwards, because a callback exists precisely to change other
  parameters. Validators and `choicesFrom` come along for the same ride.
- **A session, not an `eval`.** Keeping the interpreter is what makes the
  functions callable afterwards, and it is why `ScriptSession` exists rather
  than a one-shot evaluation.
- **Declarations are blanked, not removed**, so the language's own error
  messages still point at the line the author is looking at. `#@` is a comment
  in Python and a syntax error in Groovy, so leaving it in was not an option.
- **Every declared parameter is bound, including the unfilled ones.** A
  language that resolves names at run time fails on an unbound name rather
  than seeing null, so a callback mentioning a parameter the user has not
  reached would otherwise blow up instead of behaving.
- **Not ported**: the `ScriptProcessor` chain, which existed to rewrite a
  script before evaluation and was used for parameters and a shebang, both of
  which the header reader does directly. `ScriptService` has no equivalent -
  `Scripts.get().of(path)` is a line. `ScriptREPL`, `ScriptInterpreter`,
  `AutoCompleter` and `CodeGenerator` belong with the script editor.
- **A script declares its presentation the way a command does.**
  `#@script(menu = "Process>Filters>Blur It", accelerator = "^B", label = ...,
  weight = ..., iconPath = ...)` carries the same keys `@Menu` does, and where
  a script says nothing, the directory it sits in speaks for it:
  `scripts/Process/Filters/Blur_It.groovy` means the same thing, underscores
  read as spaces. That is how the scripts already in the wild are arranged, so
  it had better keep working.
- **A script names its own language** with `#@script(language = "jython")` or
  a `#!` line, which is what settles the languages sharing an extension -
  `.py` being Jython or Python depending on which is meant. Saying nothing
  means the extension decides, as before.
- **`ExecutableInfo` is how an application takes more than one source.** A
  `CommandInfo` is one; `ExecutableInfo.of(executable, attrs)` makes one out
  of a script and its directives; a SciJava Common `ModuleInfo` will make one
  through the bridge. An application gathers what it can run from as many
  sources as it has, and nothing downstream learns there was more than one.
  **This is the `AppLoader` shape the `fijifx` notes sketched**, and it is now
  load-bearing rather than planned: all three shells build their menus from
  the annotation index *and* a directory of Groovy scripts, and nothing below
  them can tell which came from where.
  - It is **not** called `MenuEntry`, and the distinction matters: a menu path
    is optional. A command invoked by name from a script, one reached only
    through the search bar, one that exists to be called by something else -
    all are ordinary things to have, and all are perfectly runnable.
    `MenuTree` takes the subset with a path; the type is named for what it is
    rather than for the one place it is most often shown.
- **Open**: whether a parameter with declared choices and no value should
  preselect the first. SciJava Common does; the model here leaves it unset,
  which is honest but shows an empty chooser. It is a one-line change in
  either direction and wants a look at real dialogs before being made.

### Phase 4 — UI and desktop

- **`scijava-harvest`** (`org.scijava.harvest`): the model behind a parameter
  dialog — groups, dependencies between parameters, validation — with no
  toolkit anywhere, so it is testable headless and a Swing or JavaFX binding
  renders it. **Prototyped**, against the four dialog behaviours actually
  asked for, and each taught something:
  - **A chosen implementation brings its own parameters.** Expansion is driven
    by the **value's class**, not the declared type: the field says `Joke`, the
    value is a knock-knock joke, and it is the joke's parameters that appear.
    Expanding the declared type finds nothing, an interface having no fields —
    so `Structs.expand`, which uses the static `childStruct()`, is not enough.
  - **Static nesting is then the degenerate case** where the value's class
    never varies. One concept covers both, and the reason is the value-driven
    expansion rather than anything about `isStruct()`.
  - **A group whose size depends on a value cannot be fields at all**, since a
    class's fields are fixed when it is compiled. It needs parameters built at
    runtime — which is exactly what a script header needs, so both are served
    by `Parameters.builder()`, members over a map. Two requirements that looked
    unrelated turned out to be one primitive.
  - **An "advanced" toggle** is a group with a `visibleWhen` behavior; a
    hidden group disappears rather than showing as an empty box.
  - **Callbacks work for scripts**, which SciJava Common never managed: it
    resolved `callback = "foo"` by Java reflection, leaving a script nothing to
    name. `ExecutableInstance.behavior(String)` resolves a name however that
    kind of code does — Java reflects a method, a script asks its engine for a
    function.
  - **Cascading callbacks terminate.** A callback may change any parameter, so
    changes cascade, and two that set each other — Celsius and Fahrenheit, a
    real case — would never stop. Each parameter's callback runs at most once
    per `set`.
  - **Validation has one protocol**: return a message, or do not. SciJava
    Common allows either throwing *or* returning a non-empty string, which is
    why it needs both `validate` and `validateMessage`; with one protocol a
    thrown exception means the validator is broken, rather than being a second
    way to report a bad value.
  - **`ObjectService` is not the mechanism** after all. A subgroup's parameters
    come from whatever object the parameter holds; where that object came from
    is a separate question, for a chooser widget's candidate list. It is one
    source among several, so the harvester is not blocked on it.
  - **`DynamicCommand` is supported in use, not in form.** Counting its uses
    across the scijava and imagej repositories shows two different needs
    wearing one coat: about 39 files mutate the metadata of a *declared*
    parameter (`setChoices`, `setMinimumValue`), and about 31 add parameters
    that do not exist statically (`addInput`). SciJava Common serves both by
    making `ModuleInfo` mutable and per-instance, which is precisely what makes
    harvesting hard: a platform cannot know a command's parameters without
    constructing it.
    - Adding parameters is the generated group above — and better, because it
      is *declared*: `@Group(membersFrom = ...)` tells a platform exactly where
      the dynamic part is and that everything else is fixed.
    - Computing metadata is `@Parameter(choicesFrom = "behavior")`, which
      leaves the parameter statically visible while its values follow the
      state. `@Parameter(choices = {...})` covers the static case, and
      `ParameterNode.choices()` hands a UI the resolved list either way. Being
      a behavior, it works for scripts too.
    - So there is no `DynamicCommand` base class and no mutable description.
      If `min` and `max` want the same treatment, `minFrom`/`maxFrom` follow
      the identical pattern — worth adding when a widget needs it, not before.
  - **Known rough edges**: group placement is provisional (a group appears
    where its first member would, and a generated group needs an explicit
    `after` anchor); `ParameterModel` decides the tree changed by comparing
    `toString()`, which is right for correctness and wrong for a UI that should
    patch rather than rebuild — the Swing dialog rebuilds its widgets whenever
    the tree changes, which is correct and loses focus on a parameter whose
    callback reshapes the dialog.

- **`scijava-ui3`** (`org.scijava.ui3`) and **`scijava-swing`**
  (`org.scijava.swing`): the contracts, and the first toolkit to meet them.
  **Built**, and the shape of the split is the point:
  - `Widget` is a view of a `ParameterNode`, and that is all it is. There is no
    `WidgetModel`: SciJava Common needed one because a widget could not see the
    module, whereas a widget here reads the node for label, description,
    bounds, style and resolved choices, and writes through `ParameterModel`.
    Writing through the model is what makes callbacks run and the dialog
    reshape itself, so a widget gets the dynamic behavior by not doing anything
    clever.
  - `WidgetPanel` **is a `Widget`**, so a group nests in its parent's list with
    no special case, however deep. `WidgetPanels` walks the tree the same way
    for every toolkit; a binding supplies factories and a panel factory, some
    150 lines of tree-walking it no longer writes.
  - `InputHarvester` is a `Preprocessor`. Dismissing the dialog is
    `Execution.decline`, an ordinary outcome that the `ExecutionResult`
    reports, rather than a `canceled` flag on a stateful preprocessor.
  - **MigLayout is gone**, replaced by `GridBagLayout`: two-column label-field
    layout is not worth a third-party dependency, still less an automatic
    module in a JPMS build.
  - **Seven widgets so far** — number (spinner, with an optional slider or
    scroll bar, and labelled tick marks), text (field, password, area, and the
    arbitrary-precision numbers a spinner cannot step), toggle, choice, file,
    message, and the panel itself. The button, color, date, file-list and
    radio-choice widgets are not ported; each earns its place when something
    asks for it.
  - **An enum needs no annotation**: its constants are its choices, resolved
    in `ParameterTree` so every toolkit gets it. `softMin`/`softMax` bound the
    slider where the permitted range is far wider than the useful one, while
    the spinner still enforces the hard `min`/`max`.
  - **A widget reads its node out of the current tree**, not the one it was
    built from. The tree is rebuilt on every `set`, so a captured node goes
    stale — and since `ParameterModel` rebuilds the widgets only when the
    tree's *shape* changes, a parameter whose computed choices changed while
    its shape did not would otherwise keep offering the old values.
  - **The dialog never moves and never shrinks.** After the first build it may
    grow, to make room for parameters that have just appeared, but a dialog
    that re-centred itself, or closed up around a group the user had just
    collapsed, would move the controls out from under the pointer.
  - **An empty dialog says why.** A missing annotation index — javac produces
    one only when scijava-index is on the annotation processor path, and says
    nothing when it is not — means no widget factories are discovered, and the
    dialog comes up with nothing but OK and Cancel. That is now a warning
    naming the missing index, as is a parameter no factory accepted.
  - **Style hints are advisory strings** (`@Parameter(style = "slider")`,
    `"format:0.00"`), which a toolkit is free to ignore, so a parameter styled
    for Swing still renders elsewhere. `min`, `max` and `stepSize` are strings
    too, read back in the parameter's own type — because metadata has to
    survive a script header.
  - **Demos with `main()`** (`Demos` in the test sources) put each tricky
    behavior on screen: the kitchen sink, the advanced toggle, the generated
    group, computed choices, live validation, and two values that keep each
    other in step.

- **Input harvesting is a headline deliverable**, not a port. It is the
  workhorse that lets Fiji users write scripts without UI-specific concerns.
  Build it on `scijava-struct`, and complete the long-requested
  **dynamic subgroups** (widgets appearing based on another widget's value) —
  see [scijava-common#42](https://github.com/scijava/scijava-common/issues/42#issuecomment-332658377)
  and the prototype preserved on the `historical/scijava-ops-prototype` branch
  (`widget/`, `swing-widget/`), which is the design starting point rather than
  `org.scijava.widget` as written.
- **A shell exists, as a demo**: `Shell` in `scijava-swing`'s test sources is
  a window with menus, a search bar and a status line. It is the first thing
  to put the whole stack together - commands found through the annotation
  index, arranged by `MenuTree`, rendered by `SwingMenus`, run by `Runner`,
  filled in by `SwingInputHarvester` - and starting it loads no command class.
  `MenuCreator`/`Menus` (in `scijava-command`, beside the tree they walk) hold
  the toolkit-free part of the walk, so a second toolkit implements four
  methods and inherits the rest.

- **From the `fijifx` experiment** (unpublished, since retired), which asked
  the right questions before this design existed:
  - *"Is there any way we can build the menus before loading the command
    classes? Not if we use `@Plugin` annotations on the commands themselves
    -- we can do it if all menu items are scripts."* That conclusion is now
    wrong, and usefully so: the annotation index is read as **data**, and
    `@Menu` is indexed separately from `@Plugin` and joined on the class name,
    so a menu of ten thousand commands costs no class loading at all. The
    experiment's own class-loading test - a static initializer that sleeps a
    second - is the measurement that made the question urgent.
  - **`AppLoader` is the legacy bridge's shape.** The experiment sketched one
    loader per source of commands - SciJava's plugin index, ImageJ 1.x's
    `plugins.config`, a Fiji `.toml`, a directory of scripts - each
    contributing menu entries into one application model, and each able to
    report progress for a splash screen. That is what the bridge needs:
    several discovery sources feeding one `MenuTree`, with SciJava Common's
    `ModuleInfo`s as one source among them rather than a special case.
  - **The main window is menu bar, tool bar, progress bar, status bar and a
    search field**, and the search field is not an afterthought: a command
    that is in no menu is still reachable, which the demo shell shows.

- **A second binding, `scijava-javafx`**, is what turns "toolkit-agnostic"
  from an assertion into a measurement. The whole of `scijava-ui3` survived it
  with **two changes**, both of which were Swing keeping something that was
  never Swing's:
  - `Widget.isLabeled()` - whether a parameter wants a label beside it is a
    statement about the parameter, and every toolkit would have answered it
    identically.
  - `WidgetPanel.showProblems(Map)` - how a problem *looks* is a toolkit's
    business, but that there is one, shown per parameter and cleared when
    fixed, is not.
  Everything else - `Widget`, `WidgetFactory`, `WidgetPanelFactory`,
  `WidgetPanels`, `Widgets`, `InputHarvester`, `MenuCreator`, `Menus` - was
  untouched, and `ParameterModel` and `ParameterTree` never came into
  question.

- **What the second binding had to do differently** is worth recording,
  because it is what a third will face too:
  - **The toolkit must be started, once**, and JavaFX has no `invokeAndWait`.
    `FxThread` is that plumbing, and it deliberately does not use
    `Application.launch`, which takes over the process and returns only when
    the last window closes - wrong for a binding that an application, a script
    or a test may use.
  - **`showAndWait` runs a nested event loop**, so the dialog must be built
    and shown in one visit to the JavaFX thread, where Swing wanted two.
  - **JavaFX spinners do not preserve a parameter's type** - their value
    factories are `Integer` or `Double` and nothing else - so the widget
    converts on the way out, and an editable spinner needs its editor watched
    or a typed value is silently lost when focus moves.
  - **Some things are simply easier**: a collapsible group is a `TitledPane`,
    where Swing needs a hand-rolled header button; `KeyCombination.SHORTCUT_DOWN`
    says what Swing needs a `Toolkit` call to compute.

- **A third binding, `scijava-awt`**, is where "toolkit-agnostic" stops being
  a claim about two similar toolkits. It changed **nothing** in the contracts.
  What it did change is where the shared code lives:
  - `AbstractWidget` moved into `scijava-ui3`, because three bindings had
    written the same bookkeeping - find the node in the *current* tree, write
    through the model, do not mistake a refresh for the user typing. Two would
    have been a coincidence.
  - `Accelerator` moved into `scijava-command`, because Swing wants a
    `KeyStroke`, JavaFX a `KeyCombination` and AWT a `MenuShortcut`: three ways
    of *saying* a shortcut, one thing being said. A binding that parses `^+C`
    itself is a binding that can disagree about what it means.
  - AWT is the harshest case so far, and instructive for it: no spinner, no
    slider, no titled border, no collapsible pane, no icons in menus, and a
    `MenuShortcut` that cannot express alt. The number widget is a text field
    and a scroll bar honoring `min`, `max`, `stepSize` and the soft bounds;
    the collapsible group is a button that hides a panel; an accelerator AWT
    cannot say is dropped rather than approximated into some other key.
    **None of it asked the model or the contracts for anything.**
  - It also found a difference worth knowing: AWT's `TextComponent.setText`
    does not notify text listeners, where a Swing document would. Harmless
    here - the refresh guard covers it either way - but it is the kind of
    thing only a third toolkit tells you.

- **On the structure of `scijava-awt` and `scijava-swing`**, which SciJava
  Common got into four components (`scijava-ui-awt`, `scijava-ui-swing`,
  `imagej-ui-awt`, `imagej-ui-swing`): the evidence says the toolkit axis is
  not where the duplication was. `scijava-ui-swing` imported exactly six
  things from `scijava-ui-awt` - `AWTInputEventDispatcher`,
  `AWTDropTargetEventDispatcher`, `AWTColors`, `AWTWindowEventDispatcher`,
  `AWTWindows`, `AWTClipboard` - and **not one widget**, because
  `java.awt.Choice` and `JComboBox` have nothing whatever in common. So:
  - **Swing depends on AWT for the platform, never for widgets.** `Edt` is the
    first instance: the dispatch thread belongs to `java.awt.EventQueue`, and
    `SwingUtilities` merely forwards to it, so code that needs to reach the
    EDT need not drag in Swing. Colors, cursors, clipboard, drag-and-drop,
    window placement and screen geometry follow the same way.
  - **The widget types stay siblings.** `SwingWidget` could extend
    `AwtWidget` - `JComponent` is a `Component` - but discovery filters on
    `WidgetFactory.widgetType()`, so that would mean a pure-AWT application
    silently filling its dialogs with Swing controls the moment scijava-swing
    appeared on the classpath. Mixing the two is then an explicit adapter,
    which is what embedding an ImageJ 1.x canvas in a Swing frame will want
    anyway.
  - **The image-processing layer should not repeat the matrix.** What made
    four components was (generic | ImageJ) x (AWT | Swing), and the ImageJ
    half of that is mostly one thing: a canvas that paints a `BufferedImage`
    and handles a tool's events. That is AWT code, and it works unchanged
    inside a Swing frame - which is precisely what ImageJ 1.x has always done.
    So the image layer wants an AWT canvas plus per-toolkit frames, not two
    parallel implementations.

- **`scijava-ui3-test` is the anti-drift mechanism**: the sample commands and
  a conformance suite that every binding extends - eight assertions about
  widgets, three about menus. Swing and JavaFX render the same declarations
  and are held to the same behavior, and a third binding starts by making the
  suite pass. A binding's own tests then cover only what is genuinely its own:
  which control it chose, how it parses an accelerator.

- **The application shell moves to layers 1 and 2**, it is not discarded:
  `ApplicationFrame`, `Desktop`, `StatusBar` and `ToolBar` are contracts in
  `scijava-ui3` and implementations in the toolkit bindings. What the core
  drops is any knowledge that they exist.
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
| `ui`, `menu` | `scijava-ui3` contracts and `scijava-menu` (layer 1), with toolkit bindings in layer 2 (Phase 4) |
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

### The core services are not a block to port

The plan once said Phase 2 would bring over "the core services: object
registry, prefs, thread, app/status". Measured against usage and against what
has since been built, most of that does not survive:

| SJC service | Files outside SJC | Disposition |
| --- | --- | --- |
| `StatusService` | 57 | **Superseded** by `scijava-progress`. Porting it would re-create what was just replaced. |
| `ThreadService` | 76 | Mostly EDT dispatch, which is a **UI** concern: `scijava-concurrent` covers parallelism, and the rest belongs with the UI layer (Phase 4). |
| `ObjectService` | 52 | Real, but its consumers are the widgets and the conversion layer. It should land with them, so its shape is driven by a consumer rather than guessed (Phase 3/4). |
| `AppService` | 52 | Largely version and title metadata, which `scijava-meta` covers; the rest is app-shell material already dropped. |
| `PrefService` | 31 | Split in two — see below. |

#### Preferences: a store, and a policy

`PrefService` is really two things, and only one of them is core.

**Widget value persistence** — remembering what a user last typed into a given
parameter of a given module — is *policy*. It needs module identity, parameter
names, and a rule for when to save, none of which mean anything without the
input harvester. It belongs with the harvester, in Phase 4.

**A settings store** — durable key/value state in a file — is a general
capability whose API needs no consumer to get right. Deferred, but with the
design settled now, so that Phase 4 inherits decisions rather than a debate:

- **Not `java.util.prefs`.** Its opacity is the actual complaint: the registry
  on Windows, plists on macOS, `~/.java/.userPrefs` on Linux; values over 8KB
  silently truncated; nothing a user can inspect or diff.
- **TOML.** It reads like the INI files everyone already understands
  (`key = value`), supports comments, and — unlike YAML — is not
  whitespace-fragile and has no implicit typing, so `no` stays the string
  `no` and `1.10` stays `1.10`. Those YAML traps bite exactly the values a
  config file holds. Jaunch already uses TOML, so the format is familiar
  across the stack. The cost is a dependency: the JDK has no TOML parser,
  where `snakeyaml` was already present — worth choosing the library when
  this is built.
- **`$XDG_CONFIG_HOME/fiji`, else `~/.config/fiji`** — what users asked for —
  on every platform rather than `%APPDATA%` and `~/Library`. The complaint was
  opacity, and a path users can find, back up and quote in a bug report beats
  platform convention here. Overridable by system property, and the effective
  path should be printable at runtime.
- **Namespaced by class**, as SJC does, mapping onto sections in the file.
- **No migration** from the old store: SJC keeps working for SJC, and a
  one-time import is a feature to add if asked for, not a reason to couple the
  two.
- **Open, and genuinely blocked on the harvester:** what to do about stale
  keys. Remembered values accumulate for modules that no longer exist, and
  neither SJC nor a plain file store has an answer. The harvester is what
  knows which keys are still live.

## Later, not now

- **`scijava-persist`** (in the incubator): polymorphic serialization where
  the implementations live in *other people's* repositories, resolved by
  discovery — a real and recurring need, and the discovery half is exactly
  SciJava-shaped. Worth rebuilding when a consumer appears, but not worth
  lifting: it hard-wires Gson into its SPI (`IClassAdapter extends
  JsonSerializer`), vendors a copy of Gson's `RuntimeTypeAdapterFactory`, and
  is built on `Context` + `@Plugin`. A rewrite would be much smaller, since
  Jackson has first-class polymorphic support and only the discovery-driven
  subtype registration would be ours. Its author ended up inlining equivalent
  logic downstream, and there is no concrete core consumer today.

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
