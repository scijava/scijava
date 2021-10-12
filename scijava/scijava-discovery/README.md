# SciJava Discovery: A library for the abstraction of service discovery mechanisms

This module provides the `Discoverer` interface, an abstraction for service discovery. `Discoverer`s are designed to return `Discovery` objects, which combine the discovered `Object` with a tag (explained below). `Discoverer`s should implement one (or both) of `Discoverer`'s methods:

## `List<Discovery<? extends Class<T>>> Discoverer.implsOfType(Class<T> c)`

This method is designed to return a list of implementations of some `Class<?> c`. In other words, `c` is a superclass extended by/interface implemented by each of the returned `Class`es.

## `List<Discovery<AnnotatedElement>> Discoverer.elementsTaggedWith(String tagType)`

This method is designed to return a list of `AnnotatedElements` (`Class`es, `Method`s, or `Field`s) tagged with the given tag type.

## Tags

** TODO**
