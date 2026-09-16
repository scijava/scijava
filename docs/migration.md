# SciJava Common → SciJava3 migration table

Where each [SciJava Common](https://github.com/scijava/scijava-common) (SJC)
class has gone. See [migration-plan.md](migration-plan.md) for the strategy.

**Status values:**

- **done** — the SJ3 replacement exists and the SJC class carries an
  `@deprecated` pointer.
- **ported** — the SJ3 replacement exists; the SJC deprecation pointer is
  still to be added.
- **planned** — decided, not yet built. The phase is noted.
- **dropped** — no replacement. Use the standard library, or inline it.
- **open** — undecided.

A **dropped** row is a successful outcome, not a gap.

## `org.scijava.util`

| SJC class | Replacement | Status |
| --- | --- | --- |
| `AbstractPrimitiveArray` | `org.scijava.collections.AbstractPrimitiveArray` | ported |
| `AppUtils` | `org.scijava.common3.Apps` | ported |
| `ArrayUtils` | mostly dropped: `contains`/`indexOf` → `Arrays.asList(a).contains(v)` for object arrays, a loop or stream for primitives; `array` → an array literal; `toCollection` → no replacement. `safeMultiply32`/`safeMultiply64` → `common3.Numbers` (they wrap `Math.multiplyExact`) | ported |
| `BoolArray`, `ByteArray`, `CharArray`, `DoubleArray`, `FloatArray`, `IntArray`, `LongArray`, `ObjectArray`, `ShortArray`, `PrimitiveArray` | `org.scijava.collections.*` | ported |
| `Bytes` | `scijava-io3`, alongside the handles that need it (Phase 1, in progress) | planned |
| `CheckSezpoz` | none | dropped |
| `ClassUtils` | `org.scijava.common3.Classes` (mostly already deprecated in SJC) | ported |
| `ColorRGB`, `ColorRGBA`, `Colors` | UI layer (Phase 4) | open |
| `CombineAnnotations`, `Combiner`, `MetaInfCombiner`, `ServiceCombiner` | build-time tooling; `scijava-maven-plugin` | dropped |
| `ConversionUtils` | `scijava-convert3` (Phase 2); already deprecated in SJC | planned |
| `DebugUtils` | `org.scijava.common3.Threads` | ported |
| `DefaultTreeNode`, `TreeNode` | on hold — only `imagej-common` uses it; may move there | open |
| `DigestUtils` | `org.scijava.common3.Digests`. `digest` now throws `IllegalArgumentException` for an unknown algorithm rather than returning `null`; `best*` is SHA-1, since every Java platform guarantees it | ported |
| `FileUtils` | split: filename logic → `org.scijava.common3.FilePaths` (`getPath` → `normalizeSeparators`, `getExtension` → `extension`, `stripFilenameVersion` → `stripVersion`, `getAllVersions` → `allVersions`, `matchVersionedFilename`, `deleteRecursively`); `urlToFile`/`listContents`/`appendContents` → `common3.URLs`; `findResources` → `scijava-index` (Phase 1) | ported |
| `FileUtils` (dropped parts) | `readFile`/`writeFile` → `java.nio.file.Files.readAllBytes`/`write`; `createTemporaryDirectory` → `Files.createTempDirectory`; `getModifiedTime` → `Files.getLastModifiedTime`; `shortenPath`/`limitPath` → no replacement, used only inside SJC itself | dropped |
| `GenericUtils` | `org.scijava.common3.Types` (already deprecated in SJC) | ported |
| `IntCoords`, `IntRect`, `RealCoords`, `RealRect` | UI layer (Phase 4) | open |
| `IteratorPlus` | none — use the standard `Iterator`/`Iterable` API | dropped |
| `LastRecentlyUsed` | `java.util.LinkedHashMap` with `accessOrder=true` + `removeEldestEntry` | dropped |
| `ListUtils` | dropped: `first` is one line, inline it; `string` → `StringJoiner` or `String.join` | dropped |
| `LineOutputStream`, `ReadInto`, `StringMaker`, `Query` | none — inline as needed | dropped |
| `Manifest` | `org.scijava.meta.Manifest` | ported |
| `MersenneTwisterFast` | `org.scijava.common3.MersenneTwisterFast` | ported |
| `MirrorWebsite`, `TunePlayer`, `ReflectedUniverse`, `ReflectException` | none | dropped |
| `MiscUtils` | `org.scijava.common3.Comparisons` | ported |
| `NumberUtils` | `org.scijava.common3.Numbers`. `getMinimumNumber`/`getMaximumNumber` → `minimum`/`maximum`; `asBigDecimal`/`asBigInteger` → `bigDecimal`/`bigInteger`; `clampToRange` → `clamp`. **`toNumber` no longer consults a conversion framework** — it handles numbers and strings only, so `common3` stays dependency-free | ported |
| `PlatformUtils` | `org.scijava.common3.Platforms` | ported |
| `POM` | `org.scijava.meta.POM` | ported |
| `Prefs` | `scijava-context` prefs service (Phase 2); already deprecated in SJC | planned |
| `ProcessUtils` | `org.scijava.common3.Processes`, reimplemented on `ProcessBuilder`; no longer needs `ReadInto` | ported |
| `PropertiesHelper` | none — inline | dropped |
| `Sizable`, `SizableArrayList` | none — the reflective-resizing rationale died with Java 17 | dropped |
| `StringUtils` | `org.scijava.common3.Strings`: `padStart`, `padEnd`, `isNullOrEmpty`, `splitUnquoted`. `sanitize`, `sanitizeDouble`, `stripNulls` and `samePrefix` are dropped — no usages anywhere | ported |
| `Timing` | on hold — pending evidence of downstream usage | open |
| `Types` | `org.scijava.common3.Types` + `org.scijava.common3.Classes` — **not** a drop-in superset, see below | ported |
| `UnitUtils` | dropped — one usage ecosystem-wide; add it back on demand | dropped |
| `VersionUtils` | `org.scijava.meta.Versions` | ported |
| `XML` | `org.scijava.meta.XML` | ported |

### `util.Types` → `common3.Types` is not a superset

| `util.Types` member | Where it went |
| --- | --- |
| `box`, `unbox`, `isBoolean`, `isByte`, `isCharacter`, `isDouble`, `isFloat`, `isInteger`, `isLong`, `isNumber`, `isShort`, `isText`, `nullValue` | `common3.Classes` |
| `field`, `fieldType` | `common3.Classes.field` |
| `args`, `enumFromLabel`, `enumFromString` | dropped (`enumValue` remains) |
| `commonSuperTypeOf`, `isRecursive`, `isRecursiveBound`, `varsSatisfied`, `unroll` | new in `common3.Types` |

## Root package `org.scijava`

| SJC class | Replacement | Status |
| --- | --- | --- |
| `Priority`, `Prioritized` | `org.scijava.priority.*` | ported |
| `Versioned` | `org.scijava.spi.Versioned` (moved out of `common3`) | ported |
| `Named` | `org.scijava.spi.Named` — read-only `String name()`; mutability is a separate contract | ported |
| `Identifiable` | `org.scijava.spi.Identifiable` — `getIdentifier()` is now `id()` | ported |
| `Disposable` | `org.scijava.spi.Disposable` — `dispose()` is abstract, not a no-op default | ported |
| `Locatable` | deferred — its only content was a default implementation needing `common3.Classes.location`, which `scijava-spi` cannot depend on | open |
| `Typed` | deferred to the plugin layer — no consumer yet, and `Class<T> type()` collides with `GenericTyped.type()` | open |
| `Initializable` | `scijava-context` (Phase 2) — a lifecycle hook, not an SPI contract | planned |
| `Cancelable` | execution layer (Phase 3) — cancellation concerns a running thing | planned |
| `ItemIO` | `org.scijava.struct.ItemIO` | ported |
| `ItemVisibility` | input harvesting (Phase 4) | open |
| `Context`, `Contextual`, `AbstractContextual`, `NullContextException`, `NoSuchServiceException` | `org.scijava.context` (Phase 2) | planned |
| `Gateway`, `AbstractGateway`, `SciJava` | Phase 2, if retained at all | open |
| `Instantiable`, `InstantiableException` | subsumed by `Discovery` | planned |
| `BasicDetails`, `AbstractBasicDetails`, `UIDetails`, `AbstractUIDetails`, `MenuEntry`, `MenuPath` | plugin/module metadata (Phases 2–4) | open |
| `Validated`, `ValidityProblem` | execution layer (Phase 3) | open |
| `Optional` | `scijava-optional` (satellite repo, moves in later) | open |

## Subsystem packages

| SJC package | Replacement | Status |
| --- | --- | --- |
| `annotations` | `org.scijava.index`: `Indexable`, `Index`, `IndexItem`, `IndexReader`, `AbstractIndexWriter`, `AnnotationProcessor`, `ByteCodeAnalyzer`, `DirectoryIndexer`, plus the new `IndexDiscoverer`. The index format (`META-INF/json/<annotation>`) is unchanged, so indexes stay readable by both | ported |
| `annotations.EclipseHelper` | none — m2e `.factorypath` covers it; `DirectoryIndexer` is ported, so a Maven plugin can still index a directory | dropped |
| `annotations.AnnotationCombiner` | `scijava-maven-plugin` — it merges indexes for shaded JARs, which is build tooling | dropped |
| `annotations.legacy` | none — reads the pre-2013 `META-INF/annotations/` format | dropped |
| `io.location` | `org.scijava.io3.location`. `LocationService`/`DefaultLocationService` become `Locations`, backed by plain `ServiceLoader`; `LocationResolver` no longer extends `HandlerPlugin` and carries its own `priority()`. `URILocation`'s injected `LogService` was dead code and is gone | ported |
| `ByteBank`, `ByteArrayByteBank` | `org.scijava.io3` | ported |
| `io.handle`, `io.nio` | `org.scijava.io3.handle` (Phase 1, in progress) | planned |
| `io` (`IOPlugin`, `IOService`, `RecentFileService`) | `scijava-io3` (Phase 1), pending review of what is still used | open |
| `event`, `event.bushe` | `org.scijava.events` — clean-room rewrite, not a port (Phase 1) | planned |
| `parse` | Parsington (`org.scijava.parsington`) | planned |
| `log` | SLF4J, used directly | dropped |
| `cache` | `scijava-cache` moves in, keeping `org.scijava.cache` (Phase 2) | planned |
| `thread` | `org.scijava.concurrent` | ported |
| `task` | `org.scijava.progress` | ported |
| `plugin` | `org.scijava.context` + `org.scijava.index` + `Discoverer` (Phase 2) | planned |
| `service` | `org.scijava.context` — discovered via `ServiceLoader`; `Service` no longer extends `SciJavaPlugin` (Phase 2) | planned |
| `object`, `prefs`, `app` | `org.scijava.context` core services (Phase 2) | planned |
| `convert` | `scijava-convert3` (Phase 2) | planned |
| `module`, `module.process`, `command` | execution layer on `scijava-struct` (Phase 3) | planned |
| `script`, `script.process` | scripting facade + `javax.script` / GraalVM polyglot / Appose adapters (Phase 3) | planned |
| `widget` | input harvesting on `scijava-struct`, with dynamic subgroups (Phase 4) | planned |
| `ui`, `menu` | minimal UI API; `ApplicationFrame`/`Desktop`/`StatusBar`/`ToolBar` dropped (Phase 4) | planned |
| `input`, `tool` | UI-agnostic facades on `scijava-events` (Phase 4) | planned |
| `display` | overhauled, not ported (Phase 4) | open |
| `platform`, `ui.dnd` | `scijava-desktop` modernization (Phase 4) | planned |
| `console`, `main`, `run` | single CLI module (Phase 4) | planned |
| `startup` | standalone module (Phase 4) | planned |
| `welcome`, `download`, `text`, `options` | likely dropped | open |
| `minimaven`, `test` | out of scope | dropped |
