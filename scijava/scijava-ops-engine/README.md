# SciJava Ops Engine: A default implementation of SciJava Ops API

This module ([currently](https://github.com/scijava/scijava/issues/55)) uses SciJava Common's `Plugin` framework to create the `DefaultOpEnvironment`. By discovering Op implementations at compile time, there is no need to provide an explicit list of Op classes.

The `DefaultOpEnvironment` utilizes many different routines to match as many Ops as possible. These routines are outlined below, along with a given example Op:

```java
@OpField(names = "math.add")
public final BiFunction<Number, Number, Double> fooOp = (in1, in2) -> in1.doubleValue() + in2.doubleValue(); 
```

## Direct matching

The simplest type of Op matching involves a direct comparison on the functional types of the requested Op and the Ops known to the environment. Thus our example `math.add` Op might be returned as a direct match for the following `OpBuilder` call:

```java
BiFunction<Number, Number, Double> op = OpEnvironment.op("math.add") //
	.inType(Number.class, Number.class) //
	.outType(Double.class) //
	.function();
```

## Runtime-safe matching

`DefaultOpEnvironment` can also return Ops whose assignment would throw compile-time errors, but are safe to call at runtime. These situations often arise with function generics, when the requested input types are more specific than those defined by the Op, or the requested output type more generic than that declared by the Op. Runtime-safe matching provides much flexibility, allowing users to deviate from the Op's signature when more convenient. Thus our example `math.add` Op might be returned as a runtime-safe match for the following `OpBuilder` call:

```java
BiFunction<Double, Double, Number> op = OpEnvironment.op("math.add") //
	.inType(Double.class, Double.class) //
	.outType(Number.class) //
	.function();
```

Since `Double` is a subtype of `Number`, it is assignable to `Number` and that the inputs we wish to provide to the Op will satisfy the Op we have. A similar argument can be made about the output type.

## Adaptation

`DefaultOpEnvironment` is able to retype Ops whose functional type is not the same as the requested type when it is aware of the Ops needed to do the retyping. For example, a `BiFunction<I1, I2, O>` can be converted into a `Computer.Arity2<I1, I2, O>` iff there exists a `Computers.Arity1<O, O>` `copy` Op to copy the output of the `BiFunction` into the output provided by the `Computer`. Thus, supposing we have some Op:

```java
@OpField(names = list.populator)
public final BiFunction<Double, Double, List<Double>> barOp = (in1, in2) -> Arrays.asList(in1, in2);
```

it might be returned as an adaptation for the following `OpBuilder` call:


```java
Computers.Arity2<Double, Double, List<Double>> op = OpEnvironment.op("math.add") //
	.inType(Double.class, Double.class) //
	.outType(new Nil<List<Double>>() {}) // note the need for a Nil, because we need to specify a generic type.
	.computer();
```

## Simplification

`DefaultOpEnvironment` is able to retype Ops whose input and output parameter types are not the same as the requested arguments types when it is aware of the Ops needed to do the retyping. For example, a `BiFunction<A1, A2, O>` can be converted into a `BiFunction<B1, B2, P>` iff there exists a chain of Ops to convert from `A1` to `B1`, from `A2` to `B2`, and from `O` to `P`. Thus, supposing we have some Op:

```java
@OpField(names = list.populator)
public final BiFunction<Double, Double, List<Double>> barOp = (in1, in2) -> Arrays.asList(in1, in2);
```

it might be returned as a simplification for the following `OpBuilder` call:


```java
BiFunction<Integer, Integer, List<Integer>> op = OpEnvironment.op("math.add") //
	.inType(Integer.class, Integer.class) //
	.outType(new Nil<List<Integer>>() {}) // note the need for a Nil, because we need to specify a generic type.
	.function();
```

## Reduction

`DefaultOpEnvironment` supports nullable parameters by enabling the retrieval Ops **with or without** their optional parameters (denoted with the `@Nullable` annotation). Parameters can only be optional when **all input parameters to its right in the signature are also optional**. When a parameter is declared as optional, `null` is passed to the parameter and it is the **Op's** responsibility to replace that `null` value with a reasonable default. To prevent confusion, an optional parameter can **only** be omitted when all optional parameters to its right are **also omitted**. Thus, supposing we have some Op:

```java
@Plugin(type = Op.class, name = math.add)
public class bazOp implements BiFunction<Double, Double, Double> {

	@Override
	public Double apply(Double in1, @Nullable Double in2) {
		if (in2 == null) in2 = 0.0;
		return in1 + in2;
	
	}

}
```

it might be returned as a reduction for the following `OpBuilder` call:


```java
BiFunction<Double, Double, Double> op = OpEnvironment.op("math.add") //
	.inType(Double.class) //
	.outType(Double.class) //
	.function();
```
