# SciJava Ops API: a foundation for a flexible, extensible algorithms framework

This component comprises the abstract foundation for the collection and use of Ops (see SciJava Ops SPI for the declaration of Ops).

## The `OpEnvironment`
The `OpEnvironment` is intended to be the main interface for accessing Ops and has API designed to disconnect algorithm from implementation. Instead of identifying a particular implementation, Ops are retrieved through the `OpEnvironment` by specifying:
* a name
* a set of input types
* an output type
* a functional type

for example, suppose you are looking for a [`java.util.BiFunction`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/function/BiFunction.html) that adds two `Number`s. To obtain an Op that can perform this operation, you might specify:

| Parameter | Value |
|--|--|
| Name | `"add"` |
| Input Types | `{Number, Number}` |
| Output Type | `{Number}`|
| Functional Type | `BiFunction<Number, Number, Number>`|

To create *generic* typings, `OpEnvironment` often makes use of the `Nil` interface of SciJava Types. Anonymous `Nil`s are easily created for generic type `X` using `new Nil<X>() {}`. For example, to preserve the functional type in the above table we write `new Nil<BiFunction<Number, Number, Number>>() {}`.

In situations where the `OpEnvironment` knows of multiple satisfying Ops, it is up to the `OpEnvironment` to return the "best Op for the job". By delegating the decision to the `OpEnvironment`, the user can spend less time drowning in implementation documentation and construct an image processing chain faster.

To match the `BiFunction<Number, Number, Number>` above, we write the following:

``` java
Nil<BiFunction<Number, Number, Number>> funcNil = new Nil<BiFunction<Number, Number, Number>>() {};
Nil<Number> numNil = new Nil<Number>() {};

BiFunction<Number, Number, Number> foo = op("add", funcNil , new Nil[] {numNil, numNil}, numNil);
```

## `OpBuilder` can be used to simplify Op retrieval

By using `OpBuilder`, we can make this process *much* easier to read. All Op calls using the `OpBuilder` follow a similar pattern:
1. Specify the name of the Op using either `new OpBuilder(OpEnvironment env, String name)` or `OpEnvironment.op(String name)`
2. Specify the input using:
  * `OpBuilder.input(I1 in1, I2 in2, ...)`
  * `OpBuilder.inType(Nil<I1> in1Type, Nil<I2> in2Type, ...)`
  * `OpBuilder.inType(Class<I1> in1Class, Class<I2>, ...)`
3. Specify the output using:
  * `OpBuilder.output(O out)`
  * `OpBuilder.outType(Nil<O> outType)`
  * `OpBuilder.outTyep(Class<O> outType)`
4. Specify the desired return using:
  * For computers, `OpBuilder.computer()` to get the `Computer` or `compute()` to compute the result directly
  * For functions, `OpBuilder.function()` to get the `Function` or `apply()` to compute the result directly
  * For inplaces, `OpBuilder.inplace()` to get the `Inplace` or `mutateX()` to compute the result directly (where `X` indicates the index of the argument to be mutated)

Thus we can replace the code:

``` java
Nil<BiFunction<Number, Number, Number>> funcNil = new Nil<BiFunction<Number, Number, Number>>() {};
Nil<Number> numNil = new Nil<Number>() {};

BiFunction<Number, Number, Number> foo = op("add", funcNil , new Nil[] {numNil, numNil}, numNil);
```

with the code:

``` java
Nil<Number> numNil = new Nil<Number>() {};

BiFunction<Number, Number, Number> foo = op("add") //
	.inType(Number.class, Number.class) //
	.outType(Number.class) //
	.function();
```

Much more readable, no?

## `Hints` and `OpHints` can specify decision preferences

`OpEnvironment` implementations have the ability to respond to a set of `Hints` triggered by the user or by the Ops known to it. 

Suppose, for example, that a subset of the Ops known to your `OpEnvironment` specialize in performance, making drastic improvements in speed at the cost of computational accuracy. Through the `OpHints` annotation, these Ops can notify the `OpEnvironment` of their lossiness:

```java
@OpHints(hints = {Lossiness.LOSSY})
public class FastOp implements BiFunction<Number, Number, Number> {

	@Override
	public Number apply(Number in1, Number in2) {
		...
	}

}

@OpHints(hints = {Lossiness.LOSSLESS})
public class PreciseOp implements BiFunction<Number, Number, Number> {

	@Override
	public Number apply(Number in1, Number in2) {
		...
	}

}
```

In situations where precision should be prioritized, users can then tell their `OpEnvironment` to prioritize Ops that do not trade performance for speed.

```java
Hints hints = ...;
OpEnvironment env = ...;

public static void main(String[] args) {
	hints.setHint(Lossiness.LOSSLESS);
	env.setHints(hints);
}
```
