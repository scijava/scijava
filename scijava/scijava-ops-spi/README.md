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

SciJava Ops SPI is currently tied to the Plugin discovery mechanisms of [SciJava Common](https://github.com/scijava/scijava-common). While we may or may not [switch to Spring](https://github.com/scijava/scijava/issues/55) in the future, there are three main ways of declaring Ops:

### Ops as Classes
Classes can be discovered as an Op using the [`@Plugin`](https://javadoc.scijava.org/SciJava/org/scijava/plugin/Plugin.html) annotation with `type = Op.class`, adding `Plugin` metadata as you see fit:

```java
@Plugin(type = Op.class, name = "foo.bar")
public class FooBarOp implements Producer<String> {

	@Override
	public void create() {
		return "foobar";
	}

}
```

### Ops as Fields
Fields can be discovered within classes annotated using the [`@Plugin`](https://javadoc.scijava.org/SciJava/org/scijava/plugin/Plugin.html) annotation with `type = OpCollection.class`. The Fields themselves should then be annotated using the `@OpField` annotation.

```java
@Plugin(type = OpCollection.class)
public class FieldOpCollection {

	@OpField(names = "foo.bar")
	public final Producer<String> fooBarField = () -> "foobar";
}
```

### Ops as Methods
Just like Fields, Methods can be discovered within classes annotated using the [`@Plugin`](https://javadoc.scijava.org/SciJava/org/scijava/plugin/Plugin.html) annotation with `type = OpCollection.class`. The Methods themselves should then be annotated using the `@OpMethod` annotation. For methods, it is important to specify the `type` that the method is attempting to mimic.

```java
@Plugin(type = OpCollection.class)
public class MethodOpCollection {

	@OpMethod(names = "foo.bar", type = Producer.class)
	public static String fooBarMethod() {
		return "foobar";
	}
}
```