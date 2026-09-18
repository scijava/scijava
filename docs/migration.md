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
| `Bytes` | `org.scijava.io3.Bytes` | ported |
| `CheckSezpoz` | none | dropped |
| `ClassUtils` | `org.scijava.common3.Classes` (mostly already deprecated in SJC) | ported |
| `ColorRGB`, `ColorRGBA`, `Colors` | UI layer (Phase 4) | open |
| `CombineAnnotations`, `Combiner`, `MetaInfCombiner`, `ServiceCombiner` | build-time tooling; `scijava-maven-plugin` | dropped |
| `ConversionUtils` | `org.scijava.convert3.Converters`; already deprecated in SJC | ported |
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
| `Cancelable` | **not ported** — cancellation is thread interruption, and the client API is `Future.cancel(true)`. See the cancellation section of [migration-plan.md](migration-plan.md) | dropped |
| `ItemIO` | `org.scijava.struct.ItemIO` | ported |
| `ItemVisibility` | input harvesting (Phase 4) | open |
| `Context`, `NoSuchServiceException` | `org.scijava.context.Context` and `org.scijava.context.NoSuchServiceException`. Services are created lazily rather than all at once, and a context is `AutoCloseable` | ported |
| `Contextual`, `AbstractContextual`, `NullContextException` | dropped — a service receives its context in `Service.initialize(Context)`, so it need not carry one, and there is no null-context state to report | dropped |
| `Gateway`, `AbstractGateway`, `SciJava` | Phase 2, if retained at all | open |
| `Instantiable`, `InstantiableException` | subsumed by `Discovery` | planned |
| `BasicDetails`, `AbstractBasicDetails`, `UIDetails`, `AbstractUIDetails`, `MenuEntry`, `MenuPath` | `scijava-command3` presentation metadata (layer 1). All of it survives — menu path, label, icon, accelerator, weight, selection group, visibility, enablement — but as attributes in the annotation index rather than an inheritance chain every plugin extends | planned |
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
| `io.handle` | `org.scijava.io3.handle`. `DataHandle` no longer extends `WrapperPlugin`: the contract was `getType()` plus `set`/`get`, now inlined as `locationType()`, `supports`, `set`, `get` and `priority()`. `DataHandleService` becomes `DataHandles`, backed by plain `ServiceLoader`. `DataHandles.copy` reports progress to a `LongConsumer` rather than a `Task`, and so no longer supports cancellation | ported |
| `io.nio.ByteBufferByteBank` | `org.scijava.io3.ByteBufferByteBank`, with `direct()` factories and an `isDirect()` accessor. Off-heap direct buffers are its reason to exist alongside `ByteArrayByteBank`: bytes reach native code and shared memory without a copy, which matters increasingly for Appose's named shared memory blocks. **Bug fixed in the port:** growing a direct bank reallocated with `ByteBuffer.allocate`, quietly moving the bytes back onto the heap; it now grows through the bank's own provider | ported |
| `io.nio.NIOService` | **not ported** — it wraps two JDK calls behind a system-property switch, was inherited from old Bio-Formats code, and SCIFIO only exposes it as a gateway accessor (`SCIFIO.nio()`) without using it. Nothing in the ported architecture precludes `FileChannel`-based handles later: a handle is free to open a channel, and a byte bank is free to be backed by a mapped buffer | dropped |
| `io` (`IOPlugin`, `IOService`, `RecentFileService`) | `scijava-io3` (Phase 1), pending review of what is still used | open |
| `event.EventHandler` | `org.scijava.context.EventHandler`, scanned by the container rather than the bus: a service's handlers are subscribed on creation and dropped on context dispose. The bus itself stays reflection-free | ported |
| `event`, `event.bushe` | `org.scijava.events`, a clean-room rewrite. `EventService` becomes `EventBus`, one per context and with no shared instance. `subscribe` takes a lambda rather than scanning `@EventHandler` methods, and returns a `Subscription`; subscribers are held strongly and dropped by `EventBus.close()`. Events need no `SciJavaEvent` base type. Consumption is opt-in via `Consumable`; subscriber order is by `scijava-priority` | ported |
| `event.EventHistory`, `DefaultEventHistory` | dropped — a subscriber on `Object.class` reproduces it. The macro-recorder need behind it deserves an explicit layer | dropped |
| `parse` | Parsington (`org.scijava.parsington`) | planned |
| `log` | SLF4J, used directly | dropped |
| `cache` | `scijava-cache` moves in, keeping `org.scijava.cache` (Phase 2) | planned |
| `thread` | `org.scijava.concurrent` | ported |
| `task` | `org.scijava.progress` | ported |
| `plugin.Parameter` (as dependency injection) | `org.scijava.context.Dependency`. Renamed because `@Parameter` meant both "inject this" and "this is a module input"; the latter is now `org.scijava.struct` | ported |
| `plugin` (`Plugin`, `Attr`, `PluginService`, `PluginInfo`, `PluginIndex`) | `org.scijava.context.Plugin` and `Attr`, discovered via `Context.plugins(Class)`. `PluginInfo` becomes `org.scijava.discovery.Discovery`, which reports class name, metadata and priority without loading the class; `PluginService` and `PluginIndex` are subsumed by `Context.plugins` | ported |
| `plugin` (the `HandlerPlugin`/`WrapperPlugin`/`TypedPlugin`/`SingletonPlugin` hierarchy and their services) | not ported — each user of these inlines the small contract it needs, as `DataHandle` did | dropped |
| `service` | `org.scijava.context`: `Service` (no longer a plugin, and discovered via `ServiceLoader`), with `Context` holding them. `SciJavaService` has no equivalent — a marker is not needed. `ServiceHelper` and `ServiceIndex` are internal to `Context` | ported |
| `object` | with the widgets and conversion layer that use it (Phase 3/4), so a consumer shapes it | planned |
| `prefs` | split: a TOML settings store under `~/.config/fiji` (design settled in [migration-plan.md](migration-plan.md)), and widget value persistence, which belongs with the input harvester (Phase 4). Explicitly **not** `java.util.prefs` | planned |
| `app` | mostly `org.scijava.meta` for version and title metadata; the rest is app-shell material, dropped | planned |
| `thread` | EDT dispatch belongs with the UI layer (Phase 4); parallelism is `org.scijava.concurrent` | planned |
| `convert` | `org.scijava.convert3`: `Converter` asks one question - can you turn *this value* into *that type* - where SciJava Common had eight `canConvert` overloads and a `ConversionRequest` to carry the combinations. `ConvertService` becomes `Converters`, backed by plain `ServiceLoader` so that conversion sits *below* the container: parameter binding converts, and a script or command line supplies strings for everything. `DefaultConverter`'s pile of special cases becomes one converter per concern (cast, number, string, to-string, array, collection, constructor, file/path), each contributable and orderable by priority. `Converter.of(String.class, Foo.class, Foo::parse)` covers the common case in a line | ported |
| the legacy bridge | `org.scijava.bridge`: `LegacyCommandInfo` presents a SciJava Common `ModuleInfo` as a SciJava3 `CommandInfo`, so every existing ImageJ2 command and Fiji plugin appears in SciJava3 menus and runs through the SciJava3 runner, harvester and dialogs unchanged. Service and context parameters are filtered out (SciJava Common's `@Parameter` meant both "input" and "inject"); `DynamicCommand`s work because the module's own description is read after initialization; callbacks and validators resolve by name through `behavior(String)`. The dependency goes one way and lives only here | ported |
| `module`, `module.process` | `org.scijava.execute`: `@Parameter` on fields, `Executables` for the plain case, and `Runner` for the processor chain. `ModuleService.run` becomes `Runner.run`, still returning a future, but of an `ExecutionResult` that reports a declined run with its reason and its author rather than through `ModulePreprocessor.isCanceled()` | ported |
| `command` | `org.scijava.command3`: `Command` is a `Runnable` whose `@Parameter` fields are its struct; `@Menu` (repeatable) carries the presentation metadata, read from the annotation index without loading the class. `CommandInfo` replaces `ModuleInfo` and needs no `Context` - a command is anything a user can run, whether its implementation is a class (`ClassCommandInfo`, read from the index) or a script; `MenuTree` replaces `ShadowMenu`. There is no `DynamicCommand`: see `choicesFrom` and `@Group(membersFrom)` | ported |
| `script`, `script.process` | `org.scijava.script3`: `ScriptLanguage`/`ScriptSession` are the SPI, with `JsrLanguage` making every JSR-223 engine on the classpath available for free; GraalVM polyglot and Appose are further languages, not replacements. `ScriptInfo`/`ScriptModule` become `ScriptExecutable`, an ordinary `Executable`, so a script runs through the same `Runner`, preprocessors, dialogs and menus as a Java command. `ScriptService` has no equivalent: `Scripts.get().of(path)` is a line. The `#@` header syntax is unchanged, and declaration lines are blanked rather than removed so the language's error messages keep pointing at the right line. The processor chain (`ScriptProcessor` and friends) is not ported: it existed to rewrite scripts before evaluation, and parameters plus a shebang are all it was actually used for. `ScriptREPL`, `ScriptInterpreter`, `AutoCompleter` and `CodeGenerator` await the script editor. `ScriptFinder` is ported: it reads `#@script` directives and falls back to the directory hierarchy for a menu path, handing an application metadata rather than menus, since this component knows nothing about menus | ported |
| `widget` (`InputWidget`, `InputPanel`, `InputHarvester`, `WidgetModel`, `WidgetStyle`) | `org.scijava.ui3`: `Widget` is a view of a `ParameterNode`, `WidgetFactory` a discoverable `@Plugin`, `WidgetPanel` a widget holding widgets (so a group nests with no special case), and `WidgetPanels` walks the tree for every toolkit. `WidgetModel` has no equivalent - a widget reads the `ParameterModel` and the node directly, and `WidgetStyle`'s parsing is `Widgets.isStyle`/`styleValue`. `InputHarvester` is a `Preprocessor` that declines the run when the user dismisses the dialog, rather than setting a `canceled` flag on itself | ported |
| `ui.awt` (satellite repo `scijava-ui-awt`) | `org.scijava.awt`: the widget set in pure `java.awt` - no spinner, so the number widget is a field and a scroll bar - plus `Edt`, the first of the AWT platform layer that the Swing binding shares. `AWTColors`, `AWTWindows`, `AWTClipboard`, `AWTInputEventDispatcher` and the drag-and-drop dispatchers belong there too, when the `input`/`tool` layer arrives; the `AWTUI`/`AWTApplicationFrame`/`AWTStatusBar` shell classes await the application layer | ported |
| `ui.javafx` (satellite repo `scijava-ui-javafx`) | `org.scijava.javafx`: the same widget set as the Swing binding, plus `FxThread` for JavaFX's application thread. `JavaFXKeys`'s key-code mapping is worth keeping when the `input`/`tool` layer arrives; `JavaFXMenuCreator` is superseded by `FxMenus`, some 40 lines against `MenuCreator` | ported |
| `ui.swing.widget` | `org.scijava.swing`: number (spinner, with an optional slider or scroll bar and labelled ticks), text (field, password, area, and `BigInteger`/`BigDecimal`, which a spinner cannot step), toggle, choice, file, message, and the panel and dialog. `SpinnerBigDecimalModel`, `SpinnerBigIntegerModel`, `SpinnerTypedNumberModel` and `SpinnerNumberModelFactory` have no equivalent: `SpinnerNumberModel` already steps in the type of the value it holds, and arbitrary precision goes to the text widget. MigLayout is gone, in favor of `GridBagLayout`: one fewer dependency, and one fewer automatic module. The button, color, date, file-list and radio-choice widgets are not ported yet - each earns its place when something asks for it | ported |
| `menu` (`MenuCreator`, `AbstractMenuCreator`, `MenuService`, `ShadowMenu`) | `org.scijava.command3`: `CommandInfo` is anything a user can run (a menu path being optional), `MenuTree` arranges the subset that has one, and `MenuCreator`/`Menus` are the toolkit-free walk, with `SwingMenus`, `FxMenus` and `AwtMenus` implementing it. `MenuService` has no equivalent: a menu bar is `MenuTree.of(Commands.discover(context))`, which is a line rather than a service | ported |
| `ui`, `menu` | `scijava-ui3` (toolkit-agnostic contracts) and `scijava-menu` (the menu tree), with `ApplicationFrame`/`Desktop`/`StatusBar`/`ToolBar` as contracts there and implementations in the toolkit bindings. Dropped from the *core*, not from the stack — see the layering section of [migration-plan.md](migration-plan.md) | planned |
| `input`, `tool` | UI-agnostic facades on `scijava-events` (Phase 4) | planned |
| `display` | overhauled, not ported (Phase 4) | open |
| `platform`, `ui.dnd` | `scijava-desktop` modernization (Phase 4) | planned |
| `console`, `main`, `run` | single CLI module (Phase 4) | planned |
| `startup` | standalone module (Phase 4) | planned |
| `welcome`, `download`, `text`, `options` | likely dropped | open |
| `minimaven`, `test` | out of scope | dropped |
