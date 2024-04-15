# Calling Ops with the `OpBuilder`

Ops are designed to be called from the Op matcher, using the `OpBuilder` syntax. OpBuilder chains follow the [builder pattern](https://refactoring.guru/design-patterns/builder), allowing users to create complex Op calls by "chaining" or appending consecutive, simpler method calls.

On this page, we will be constructing an `OpBuilder` call on an `OpEnvironment ops` to execute a Gaussian Blur on an input image `inImage`, with our output buffer `outImage`.

## Specifying the name with `.op()`

From the `OpEnvironment`, an `OpBuilder` call is initialized with the method `OpEnvironment.op(String)`, which is used to describe the name of the Op that the `OpBuilder` must return:

```groovy
ops.op("filter.gauss")
```

## Passing the inputs with `.input()`

With the name defined in the `OpBuilder` call, we can then chain the inputs with the `input()` method.

For our gaussian blur, we will pass as inputs our input image `inImage`, and a `double` as our sigma parameter:

```groovy
ops.op("filter.gauss").input(inImage, 2.0)
```

## Passing an output buffer with `.output()`

Now that the inputs are specified, we can chain the output buffer using the `output()` method.

For our gaussian blur, we will pass as the output buffer our output image `outImage`:

```groovy
ops.op("filter.gauss").input(inImage, 2.0).output(outImage)
```

## Computing with `.compute()`

With all of the components of the needed Op specified, we can begin computation with the `.compute()` method.

```groovy
ops.op("filter.gauss").input(inImage, 2.0).output(outImage).compute()
```

In the call to `compute()`, the `OpEnvironment` will use the components of the `OpBuilder` syntax to:
* Match an Op based on the name provided, as well as the types of the provided input and output `Object`s
* Execute the Op on the provided input and output `Object`s.

## Additions: Repeating execution

When an Op should be executed many times on different inputs, the `OpBuilder` syntax can be modified to return the *Op* instead. Instead of calling the `.compute()` function at the end of our `OpBuilder` call, we can instead call the `.computer()` method to get back the matched Op:

```groovy
var gaussOp = ops.op("filter.gauss").input(inImage, 2.0).output(outImage).computer()
gaussOp.compute(inImage, 2.0, outImage)
```

*Note that the default `OpEnvironment` implementations cache Op requests* - this means that repeated `OpBuilder` requests targeting the same action will be faster than the original matching call.

## Common Pitfalls: Wildcards

Using [wildcards](https://docs.oracle.com/javase/tutorial/extra/generics/wildcards.html), such as `Img<?> inImage`, can make Op reuse difficult. For example, the following code segment will not compile in a Java runtime:

```java
Img<?> inImage = ...;
var gaussOp = ops.op("filter.gauss").input(inImage, 2.0).output(outImage).computer();
gaussOp.compute(inImage, 2.0, outImage);
```

### Solution 1: Use `compute` instead of `computer`

If you don't need to save the Op to a variable, *just call it directly* as shown [here](#computing-with-compute). Generally speaking, op requests are **cached**, meaning repeated OpBuilder calls that directly execute Ops will **not** significantly increase performance.

### Solution 2: Use Type Parameters on your functions

If you *know* that your `Img` will always contain bytes, define your variable as an `Img<ByteType>`
