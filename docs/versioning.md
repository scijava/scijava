# Versioning and compatibility

How components in this repository are named, versioned and evolved, and why.
These principles were first worked out in the
[SciJava incubator](#history-the-scijava-incubator), which this repository
replaced; this document keeps the parts that still govern development here.

Companion document: [migration-plan.md](migration-plan.md), which applies
these rules to the migration of SciJava Common.

## The requirement: scripts keep working

ImageJ and Fiji are not software products so much as living processes, and
their users expect a script written against the public API to keep working
for many years. At the same time, some bugs cannot be fixed, and some designs
cannot be improved, without changing that API. The goal is to have both:
downstream code keeps working, *and* the codebase can still improve.

SemVer alone does not achieve this. A major version bump announces a break,
but it does not prevent one: a downstream component built against `2.x` fails
as soon as anything else on the classpath pulls in `3.x`, because only one
version of a given artifact can be loaded. Every component in the stack is
then forced to migrate in lockstep.

## Incarnational versioning

When a component needs a change that cannot be made compatibly, it is not
given a new major version. It is forked to a new **incarnation**: a new
artifactId *and* a new package prefix, both carrying the incarnation number.
For example, `scijava-common3` lives in `org.scijava.common3`, and a future
incompatible successor would be `scijava-common4` in `org.scijava.common4`.

Because the two incarnations share no artifact and no package, both can be on
one classpath at once. Code written against the old incarnation keeps running
unchanged, while new code adopts the new one, and each downstream component
migrates on its own schedule rather than all at once.

The rules:

- **Never bump the major version within an incarnation.** If compatibility
  must break, bump the incarnation digit instead: artifactId, package prefix
  and major version together.
- **The major version equals the incarnation number**, so that
  `scijava-io3` is at `3.x.y`, to avoid confusion about which number means
  what. (The `1.x.y` of most components here is a legacy of the first
  monoversioned `1.0.0` release, not the intended scheme; see
  [migration-plan.md](migration-plan.md) for how it is being realigned.)
- **Deprecate before you fork.** Within an incarnation, API is retired by
  deprecation, which keeps scripts working while pointing at the replacement.
  A new incarnation is for changes that deprecation cannot express.
- **The number is the incarnation, not a collision workaround.** Where a new
  package would collide with an existing one and no incarnation applies, pick
  a natural, collision-free noun instead of inventing a digit. Every such
  decision is recorded in [migration.md](migration.md).
- **Incarnations do not share data structures.** An object from `common3` is
  not inherently usable where `common4` expects its counterpart. Bridging the
  two is the job of an explicit compatibility layer, written when a concrete
  need appears; apart from such layers, a component uses one incarnation of
  each dependency at a time.
- **Fixes do not automatically cross incarnations.** Whether a fix is ported
  forward or back is decided case by case. Keeping the period during which two
  incarnations are both actively developed short keeps that cost small.

### Only the system class loader

An earlier approach
([forum post](https://forum.image.sc/t/8393)) isolated incompatible versions
with custom class loaders. That turned out to limit deployment: tools that
embed the JVM, such as Python bridges, might only be able to wrap classes from
the system class loader. Incarnations need nothing but distinct names, so they
work on a plain classpath, with no class loader tricks, for arbitrarily long.

### Transitive dependencies do not cascade

Suppose `imagej-common3` depends on `scijava-magic5`, and later moves to
`scijava-magic6`. Must `imagej-common3` itself become a new incarnation? Not as
long as no `scijava-magic5` type appears in its public API. A downstream tool
that uses both `scijava-magic5` and `imagej-common3` will simply end up with
both `scijava-magic5` and `scijava-magic6` on its classpath, and that is fine,
since they share nothing.

This is what prevents a domino effect, where one incarnation bump forces bumps
in everything above it. It is also a strong reason to keep dependency types
out of public API.

### Deprecated clutter

Over time an incarnation accumulates deprecated classes. Moving them into a
separate `-deprecated` component is not an option, because the packages would
be split across two modules, which JPMS forbids. When the clutter becomes a
real problem, that is a sign the next incarnation is due.

## Minimal API surface

The smaller the public API, the less there is that can never change. So:

- As few public classes and methods as possible, and interfaces wherever
  possible.
- Every component is a JPMS module that exports only its API packages.
  Implementation packages stay unexported.
- No code in a blanket package prefix such as the root `org.scijava` package.
  Each component owns its own package prefix, which keeps packages from being
  split across modules.

## Release early, but stable

The traditional mantra is "release early, release often". Its failure mode is
that a half-finished design gets released, gets adopted, and is then frozen by
the compatibility promise above. The refinement: release as early as possible,
but make that release one you are prepared to support indefinitely. Before
then, restructure freely; afterward, change compatibly or fork an incarnation.

## Monoversioning per layer

The core components of each layer of the stack live together in one
multi-module repository: this one, for SciJava. That gives one place to find
code and file issues, keeps tightly coupled modules building against each
other in CI, and makes it obvious which versions go together. Add-ons that
merely build on the core stay in their own repositories.

The first release from this repository was a single monoversioned `1.0.0`,
because releasing a reactor whose modules have different versions needs
release tooling that did not yet exist. The intent is for each component to
carry its own version, with the major version following its incarnation, as
described above.

## History: the SciJava incubator

From 2020 until 2024, new core development happened in the
[SciJava incubator](https://github.com/scijava/incubator), a single
repository where immature components could be restructured freely at version
`0-SNAPSHOT`, breaking changes were caught across all of them by one CI build,
and each component graduated once ready for `1.0.0`. The incubator was where
SciJava Ops and the other components here took shape, and it is where the
principles above were first written down.

With everything graduated into this repository, the incubator is retired and
archived. Its one component that never graduated, `scijava-persist`, is
discussed in [migration-plan.md](migration-plan.md).
