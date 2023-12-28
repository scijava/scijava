# Scijava Struct: A lightweight framework for collecting Members

This library provides a lightweight framework for collecting sets of typed fields. The project centers around two `Class`es:

## `Member`: A typed field

`Member`s enrich a generic `Type` with a collection of metadata (such as a name, a description, an I/O type, etc.)

## `Struct`: A collection of `Member`s

`Struct`s are nothing more than collections of `Member`s.

## So what? How is this useful?

While `Struct`s can be valuable in describing `Method`s, they can be particuarly useful when describing `Class`es and shine under a [Dependency Injection](https://en.wikipedia.org/wiki/Dependency_injection) paradigm. In such situations, we need a data structure to define **all** inputs to the injectable `Class`, be they constructor or `FunctionalInterface` arguments, or simply `Field`s that must be injected beforehand.

To see SciJava Struct in action, check out the SciJava Ops project!
