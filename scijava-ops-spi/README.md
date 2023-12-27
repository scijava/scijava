# SciJava Ops SPI: A collection of interfaces for Op declaration

## What is an "Op"?

Operations, or "op"s, are algorithms that fulfill a particular contract. To be an Op, an algorithm **must**:
* implement *one* [`FunctionalInterface`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/FunctionalInterface.html).
* Have n inputs and *one* output.
* Be stateless; given the same inputs, an Op **must** produce the same output

## Why use Ops?

By fulfilling this contract, Ops provide a few key benefits:
* Ease of construction: Ops require only one method to provide the needed functionality, and can be written as, for example, a lambda expression
* Ease of use: Ops By exposing only one method, Ops leave no ambiguity in the way they should be run
* Reusability and Reproducability: Without any state, Ops maintain speed, and operate the same whether run once or millions of times
* Simplicity: Ops can only provide one output; this keeps Ops small and focused, encouraging code reuse to construct more complex algorithms.

## Declaring Ops

There are three main ways of declaring Ops:

### Ops as Classes

Classes can be discovered as an Op using the `@OpClass` annotation:

```java
@OpClass(names = "foo.bar")
public class FooBarOp implements Producer<String> {

	@Override
	public void create() {
		return "foobar";
	}

}
```

### Ops as Fields

Fields can be discovered within classes implementing the `OpCollection` marker interface. The fields themselves must be annotated using the `@OpField` annotation:

```java
public class FieldOpCollection implements OpCollection {

	@OpField(names = "foo.bar")
	public final Producer<String> fooBarField = () -> "foobar";

}
```

### Ops as Methods

Just like fields, methods can be discovered within `OpCollection` classes. The methods must be annotated using the `@OpMethod` annotation. For methods, it is important to specify the `type` that the method is attempting to mimic.

```java
public class MethodOpCollection implements OpCollection {

	@OpMethod(names = "foo.bar", type = Producer.class)
	public static String fooBarMethod() {
		return "foobar";
	}

}
```
