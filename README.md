[![](https://github.com/scijava/incubator/actions/workflows/build-main.yml/badge.svg)](https://github.com/scijava/incubator/actions/workflows/build-main.yml)

# SciJava Incubator

This repository is a place for complex but immature codebases to rapidly
develop, without concern for versioning. Each module here stays at version
`0-SNAPSHOT` until it is ready for a `1.0.0` release according to SemVer, at
which point the module code graduates to its own separate repository.

The primary motivation of doing this development work in a single place is to
work better together as a community, raise awareness of the work, and make it
easier to keep modules coupled together such that breaking changes are
quickly caught by CI and subsequently fixed across all incubating projects.

## Projects incubating here

* [Road to SciJava Ops](https://github.com/orgs/scijava/projects/1)

## Differences from the traditional SciJava development process

*The following writeup is a **work in progress**, published early to help
communicate the purpose of the incubator. It needs work. Please post on the
[Image.sc Forum](https://forum.image.sc/) in the Development category with
questions, or file a PR if there are areas you desire to improve yourself.
Thank you!*

The following narrative details the ways in which this approach refines and
updates the SciJava development process, with the goal of improving and
streamlining development of projects across the entire software stack,
including SciJava, ImageJ2, SCIFIO, Fiji, and other SciJava-based software
layers. This discussion assumes the reader is familiar with the
[current SciJava development methodology and project
architecture](https://imagej.net/develop/architecture).
Please read that document before proceeding further here.

### Executive summary

* **Incarnational versioning** – A revised approach to avoid breaking backwards
  compatibility of public API, so that downstream code continues to work the
  same or better into the far future, by forking component code to a new
  ***incarnation*** with new package prefix each time major changes are needed
  to a codebase. Multiple incarnations of a component can thus be included on
  the same classpath to minimize disruption of existing functionality, while
  enabling migration to new library incarnations.

* **The SciJava incubator** – A new unified repository on GitHub collecting all
  experimental and unstable development of core projects at
  https://github.com/scijava/incubator.

The foundational technologies and approach being used&mdash;Maven-based components

### Motivation

#### Values, mission and vision of key project layers

([1](https://ilovesymposia.com/2018/07/13/the-road-to-scikit-image-1-0/),
[2](https://courses.lumenlearning.com/wm-principlesofmanagement/chapter/reading-mission-vision-and-values/))

Values = https://imagej.net/develop/philosophy

* SciJava's key value is to foster the
  [FAIR principles](https://www.go-fair.org/fair-principles/).

Mission = https://imagej.net/software/imagej2/#mission

Vision = This document&mdash;at least from a technical perspective

* An accessible platform for continuous development of image processing and
  analysis that integrates and/or interoperates well with as broad a spectrum
  of cutting edge scientific software as possible. Part of the "shared
  integration platform" of science. In the case of Fiji: life sciences.
    * In this writeup: highlight that technically, ImageJ & Fiji are not
      *software products*, but living processes&mdash;we need sustainable ways
      for developers to contribute and extend the tools, as well as maintain
      their functions indefinitely with a minimum of time investment.
    * Sustainable core maintenance! Avoid the "academia trap" of new
      development+paper+abandonware.
    * Litmus test here: would a PI read the above and understand that this
      incubator proposal is about *sustainable development and maintenance*
      (actually: even growing it as much as we can) of core subsystems? As
      opposed to just continuing to add new shiny bits to Fiji forever and then
      letting them bit rot.

#### Requirements of our developer community

Requirements are derived from our values, and from pragmatic concerns. We
outline them here&mdash;discussing the shortcomings of the current system as
appropriate: *What works, what doesn't*.

##### Backwards compatibility

Never break backwards compatibility of public API. Therefore, *never increment
the SemVer major version*, as doing so is a break in API compatibility and
scripts may no longer function as they did previously.

##### Avoid duplication of work

Find a way to stay abreast of the core contributors' respective
efforts&mdash;i.e. avoiding overlap. This is for people adding functionality
intended for community reuse and will be an *opt in* process where GitHub still
exists and you can do whatever you want outside this framework too. However, it
remains a way to see what everyone is working on easily.

##### Minimize complexity

The goal here is to make the projects accessible to potential contributors.
Currently, even the naming of these projects is confusing&mdash;ImageJ, Fiji,
SciJava, ImageJ2, SCIFIO... Is there a way moving forward where we can
streamline naming and consider alternatives?

##### Release as early as possible, but be completely stable once released

One of our main Values is also a huge conundrum: [release early, release
often](https://imagej.net/develop/philosophy#release-early-release-often).
We need to refine this mantra: release as early as possible, but make that
release a stable one. As is often the case currently, projects that are only
half-done are unleashed to the public, and their adoption then leads to code
paralysis.

<blockquote>

*TODO: Reword these parts as more general requirements, and purge "incubator talk".*

##### Allow for restructuring before release

The intent of the incubator is to ease the restructuring of project modules
before they have settled. The incubator provides a space where the modules are
fluid

##### Maintain reproducible builds

Incubator modules can depend on other incubator modules, as long as the whole
system builds, and on release versions of things outside the incubator, as
usual.

##### Iterate on codebases in a 'breaking' way

We can do this by forking the code to a new package prefix each "incarnation".
E.g. imagej-common2 = net.imagej.common2

##### Minimize time with two 'active' incarnations of a codebase

Minimize the vulnerable time spent in incubation of the new version of a
project/module. This will also lead to a minimization of the time spent
supporting old incarnations while the new one is being developed.

##### Bug-Fixes need forward porting

During the development of a new incarnation, bug-fixes in old code need forward
porting. Saying that, bug-fixes in new incarnation should also be backported.

</blockquote>

<blockquote>

*TODO: Consider merging this section into bits above.*

##### Public API should be minimized

The Fiji software stack has a huge API surface. In some cases, bugs cannot be
fixed without changing that surface in a way that would break backwards API
compatibility. Furthermore, in our scientific community, there is an
expectation (or at least an ardent hope) that scripts written against that API
surface should continue to work many years into the future. As such, for many
years now, I have been pondering ways to guarantee long-term reproducibility of
SciJava-based code, without suffering from "dependency hell" where improvements
to the codebase can no longer be made. Two years ago, I made
[a post](https://forum.image.sc/t/8393) about an approach I was convinced could
work&mdash;but I was wrong: custom class loaders limit deployment scenarios.

For example, PyImageJ enables use of ImageJ from Python via a library called
PyJNIus, but only classes loaded by the system classloader are easily wrappable
into Python objects (see
[kivy/pyjnius#316](https://github.com/kivy/pyjnius/issues/316)).
\[NB: PyImageJ has since switched from PyJNIus to JPype, so the specific issue
highlighted here needs to be reexamined.\]

* As few API methods and public classes as possible.
* Only interfaces whenever possible.
* Seal helper packages&mdash;which hopefully includes all plugins.
* THESE THREE POINTS can segue to the next section...

</blockquote>

### The next iteration of SciJava development

The next iteration of our core development process, addressing these challenges
and applying lessons learned.

The SciJava, ImageJ2 and Fiji projects are distinct, but heavily overlap.

* **ImageJ2** consists of *core libraries* and an *end-user application* for
  scientific multidimensional image processing and analysis.
* **SciJava** consists of *core Java libraries more general than image
  processing*, including an extensible framework for plugins, an application
  container, and many useful utilities.
* **Fiji** is a *suite of ImageJ plugins for the life sciences* and a
  *distribution of ImageJ including those plugins*.

#### Proposed iteration of the SciJava development process: the SciJava incubator

* New repo scijava/incubator (what @fjug called "the beast" once at a
  hackathon) for all experimental core development. In this context, "core"
  means scijava/scifio/imagej/fiji and maybe/hopefully imglib2.
* Folders divided by org/repo off the root e.g. scijava/ops(/pom.xml).
* Always 0-SNAPSHOT version.
* No public 0.x releases, ever. For reproducible pinning to incubator builds,
  use jitpack&mdash;although no core component will never do this.
* Master still compiles with passing tests.
* Once component reaches 1.0.0 readiness, move code to own repo. E.g.
  scijava/ops becomes scijava-ops repo. Need process to decide details of
  promotion as a community.
    * This milestone is a natural PR (public relations) opportunity: announce it.
    * Avoids premature scrutiny of repos.
    * Ensure scijava team roles are explicitly committed.
* Fiji the application only includes release component, never incubating projects
    * Could have an "unstable" update site including incubator builds.
* Initial commit of new repo refs final commit of incubator prior to removal
  from there. This gives some continuity while retaining reproducibility.
    * We wouldn't actually migrate incubator git history to new repo. Keeps
      repos tidy and smaller size.
* All artifacts named with digit suffix to designate incarnation. E.g.:
  scijava-common2 with package prefix org.scijava.common2.
* For jigsaw and OSGi friendliness, use of blanket package prefix (e.g.
  org.scijava) directly is not allowed for new projects.
* Incubator projects may depend on non-incubator, but not vice versa (in production).
* Projects can depend on two or more incarnations of the same project but data
  structures across incarnations are not inherently compatible. Except for
  compatibility layers, core components will only use one incarnation at a
  time.
* Balance process for both agility and safety. No direct pushes to master for
  incubator. No binaries allowed&mdash;only source. All work done via PRs for
  linkability when discussing new work, and for CI checks. But self-merging is
  maybe OK (or each incubator project decides for itself). We might want to
  always squash-and-merge PRs to master, so that people can develop really fast
  on topic branches without needing to rebase afterward&mdash;then all commits
  reachable from master will still compile with passing tests. And if someone
  needs to bisect into a merged PR later, they can pull it from GitHub (TODO:
  verify this is true).
    * **Key requirement met** &ndash; This scheme preserves backwards
      compatibility for arbitrarily long with only the system class loader.
* Every module has an associated more stable "working repo slug" mapping:
  sometimes trivial (e.g. scijava/scijava-ops maps to scijava/scijava-ops,
  imagej/imagej-ops2 maps to imagej/imagej-ops2); but sometimes mapping
  multiple less stable modules to one more likely to be stable (e.g.
  scijava/scijava-context to scijava/scijava-common3).
  This slug is used for the GitHub issue tracker for those modules&mdash;no
  issue tracker for incubator, only PRs!
    * No one pushes to master of incubator except for core infrastructural
      single-commit improvements. For work on modules, use the working repo
      slug as a prefix e.g. https://github.com/scijava/incubator/pull/1.

**No SemVer major version bumps.**
(With deprecation to ease transition.)
Let's avoid it whenever possible. Goal is to keep user scripts working forever.
If breakage is needed (because backwards compatibility cannot be maintained for
technical reasons), make a new incarnation. In other words: each incarnation
stays at 1.x.y forever!
* Make major version always match incarnation number&mdash;to avoid confusion.

**Minimize API surface.**
Use Java 11 + jigsaw module system for all new developments. Export only API
packages! :smile:

#### Discussion

Ramifications and consequences of this design. Expected impact.
How to address potential issues with it.

**Transitive dependencies.**
What about transitive dependencies? E.g. if imagej-common3 depends on
scijava-magic5, but upgrades to scijava-magic6 in a way that does not break
public API, is this OK? I think it is! If imagej-common3 does not leak any
classes from scijava-magic5 in its API, all will still work downstream. If
awesome-tool depends on scijava-magic5 and imagej-common3 directly, when
imagej-common3 jumps to scijava-magic6, awesome-tool will now depend on both
scijava-magic5 and scijava-magic6&mdash;and that's OK! This is crucial to avoid
a domino effect of incarnations.

**Repository growth and sustainability.**
Years later, if incubator history gets too big, could reboot it. But for
permalinks, no renames: so should we put a year? incubator2020? Or just
incubator2 later?

**Relationship to custom class loaders and scijava-grab.**
What about custom class loader trees e.g. OSGi?

**Proliferation of deprecated classes over time.**
If many classes are deprecated, and are cluttering the IDE view, they could
maybe be moved to a new component e.g. scijava-common2-deprecated&mdash;but we
need to check if jigsaw allows this package mixing. It may be a problem.

**Heterogeneous Bill of Materials versions.**
Any issue with mixed pom-scijava parents across incubator components? I don't
think so... pom-scijava continues the same as before. It knows nothing about
the incubator.

**What about ImgLib2?**
ImgLib2 naming is not aligned with above. What to do? imglib2-common([1234])?
I'd rather pick a new name for imglib2+imagej2 future. This simplifies our
architecture, which is crucial to recruit new people.

**Encourages community contribution.**
Recruiting: invite people doing new dev to join incubator. Ensures we work
together. Makes it clearer who is a "core developer" as well.

* Missions:
    * SciJava: Cultivate a sustainable development community where powerful
      scientific tools grow.
    * ImgLib2: Provide powerful reusable image processing libraries.
    * ImageJ2: Broaden the paradigm of ImageJ beyond the limitations of the
      original ImageJ application.
    * Fiji: Make powerful scientific imaging software tools accessible to
      biologists. Batteries included.

**What about complexity compared to e.g. Python ecosystem?**
The "genes" of our Java ecosystem, accessibility with type safety, play to
certain strengths. Let's do work that plays to those strengths. For work that
doesn't, we can use different tools that do.

**Do we need a top-level aggregator POM?**
Top level aggregator pom for building all components is useful for CI, as well
as importing into an IDE.

## TO DO

**What does testing the incubator actually mean?**
Are we solely running all the tests of all incubator components? How does
testing of released components (e.g. the mega-melt) interact with incubator
testing?

**How much of maintenance will we continue doing for released components?**

**Write down a scenario: what happens step by step when developing new
component(s) in the incubator.**

**Update this document with our current thinking on monoversioning of core
components per layer.**
E.g. the various `scijava-<foo>` components incubating here will all go to
scijava/scijava as a monoversioned multi-module project.

## Monoversioning

<blockquote>

*Super rough draft about monoversioning per core layer:*

Pros and cons on consolidating code: starting with SJ3, we put all modules into
a repo called scijava when at 1.0.0. Same for other pillars (software suites
built on sj3, i.e. scifio, imglib, imagej2). Not migrating existing components.
Only the "core" components, not add-ons e.g. imagej-omero, imagej-server.

### Option 1: Straight file-based aggregation

Not multi-module, not mono-versioned, not snapshot-coupled.

#### Good

* Fewer git repos (people find code easier, brand identity)
* Issues and project boards are in one place (people file issues in one place)
* Sets framework for multi-module conversion in the future
* Doing things as part of SJ3 work is undisruptive
* Fewer places to look for a particular commit

#### Bad

* release not set up for subdirs
* not dogfooding &ndash; making plugins usually requires creating your own repo
* repo is overly complex (issues are conflated)
* Git history is intermingled
* Bisect could take longer

### Option 2:  Monoversioning

Multi-module codebase but everything has a single version for a given component.

#### Good

* Easy to tell what versions of things go together
* Project linkage (Everything is depending on project.version)
* Uber-jar

#### Bad

* Vacuous releases
* Uber-jar

#### Ugly

* imglib isn't using monoversioning &ndash; would they migrate or stick?
* Would have to decouple interdependencies between scifio, imglib, imagej,
  scijava (may require 5th org?)

Which layers?
- SciJava &rarr; scijava/scijava
- ImageJ2 &rarr; imagej/imagej3 (!) (but should we move on from this name?)
- SCIFIO &rarr; scifio/scifio (but it clashes with existing repo)
- Fiji &rarr; consider consolidating things like `fiji-lib`, `Fiji_Developer`
  into fiji? `fiji-lib` is the thing other plugins can depend on. Unless we
  want to migrate all `fiji-lib` stuff upstream to ImageJ? Think on it more.

</blockquote>

## Motivational quotes

"Yes, you did a ton of things wrong on this project. But you also did a ton of
things wrong that you don't know about yet. And there's no other way to find
out what those things are until you ship this version."
&mdash;[codinghorror.com](https://blog.codinghorror.com/version-1-sucks-but-ship-it-anyway/)

------------------------------------------------------------------------------

"It's hard for smart people to accept that perfectionism is a negative trait.
The more self-worth you derive from your previous successful outcomes, the more
susceptible your ego becomes to fear of future failures. Your amygdala is
telling you that you are successful already, so don't shake the boat. Tread
carefully and you'll always have an excuse for why things don't work out.
You'll never have to admit you just couldn't do it.

The solution is to divorce your self-worth from outcomes. Instead, derive
self-worth from the process. We can have complete control over our actions, but
never complete control over our outcomes."
&mdash;[ycombinator.com](https://news.ycombinator.com/item?id=4038680)

------------------------------------------------------------------------------

"True long-term thinking is goal-less thinking. It's not about any single
accomplishment. It is about the cycle of endless refinement and continuous
improvement."
&mdash;Atomic Habits
