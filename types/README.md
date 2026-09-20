# SciJava Types: A library for generic type reasoning

This library provides a suite of utilities for working with generic types in Java.

## `TypeService` and `TypeExtractor`s: an extensible system for discovering generic types

`TypeService.reify()` offers provides a powerful, extensible way to determine the generic type for, or "reify", a given `Object`. Under the hood, it relies on `TypeExtractor` implementations, allowing the user to extend its capabilities by defining new implementations.

Also of note is the interface `GenericTyped`, which can be implemented to quickly make any type reifiable.

## `Nil`: a container Object able to specify generic Types

`Nil`s can best be described as a "typed `null`", able to generate a generic type defined by the `Nil`'s type parameter. Given a generic type `X`, we can create a `Type` instance of `X` using the code

```java
Type x = new Nil<X>() {}.type();
```

For example, if we wanted to specify the type `Function<Double, Double>`, we could write

```java
Type functionType = new Nil<Function<Double, Double>>() {}.type();
```
