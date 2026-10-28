# SciJava Discovery: A library for the abstraction of service discovery mechanisms

This module provides the `Discoverer` interface, an abstraction for service discovery: finding implementations of some type, by some mechanism - `ServiceLoader`, a generated annotation index, a YAML file, or a hand-curated list.

## `<T> List<Discovery<T>> Discoverer.discover(Class<T> c)`

The one method a `Discoverer` must implement. It returns a list of `Discovery` descriptors for implementations of `c`. Descriptors, rather than constructed objects, are returned so that mechanisms able to report what is available without loading classes can do so.

## `<T> List<T> Discoverer.instances(Class<T> c)`

A convenience for callers that simply want the objects: it constructs an instance from each `Discovery` returned by `discover(c)`.

## Creating `Discoverer`s

Static factory methods on `Discoverer` cover the common cases:

* `Discoverer.usingProviders(...)` wraps a function yielding `ServiceLoader.Provider`s, e.g. `Discoverer.usingProviders(c -> ServiceLoader.load(c).stream())`.
* `Discoverer.using(...)` wraps a function yielding already-constructed objects.
* `Discoverer.all()` returns every `Discoverer` provided via `ServiceLoader`.
