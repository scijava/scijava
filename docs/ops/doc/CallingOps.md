# Calling Ops with the `OpBuilder`



Use of the Ops framework centers on a process of matching Op requests to algorithm implementations based on the parameters provided. The easiest way to make these queries is to use the `OpBuilder` syntax, which follows the [builder pattern](https://refactoring.guru/design-patterns/builder) to assemble the required components of an Op matching request from a particular `OpEnvironment`.

In this page, we start after having [identified a Gaussian Blur Op](SearchingForOps) that we would like to use. We assume we already have created an `OpEnvironment` named `ops`, as well as the input image to blur, and a pre-allocated output image for the result&mdash;`inImage` and `outImage`, respectively.

**Note:** we are incrementally constructing one line of code in this example. Running an intermediate step simply returns an appropriate builder that knows what has been set so far, and which step is next. If you're following along in an IDE or script editor, the code you actually *run* would be the last step, once our builder call is fully constructed.

## Specifying the name with `.op()`

From the `OpEnvironment`, an `OpBuilder` chain is initialized with the `op(String)` method, which describes the name of the Op that we ultimately want to call:

```java
ops.op("filter.gauss")
```

## Passing the inputs with `.input()`

With the name established in the `OpBuilder` chain, we can then specify our input(s) with the `.input()` method.

For this Gaussian blur, we have two inputs: `inImage` is the image we want to blur, and a `double` value as our sigma parameter:

```java
ops.op("filter.gauss").input(inImage, 2.0)
```

## Passing an output buffer with `.output()`

After specifying inputs, we provide a preallocated output container using the `.output()` method.

For our Gaussian blur, we will pass our output image `outImage` as a receptacle for the result:

```java
ops.op("filter.gauss").input(inImage, 2.0).output(outImage)
```

## Computing with `.compute()`

With all of our desired Op's inputs and output now specified, we can run it with the `.compute()` method.

```java
ops.op("filter.gauss").input(inImage, 2.0).output(outImage).compute()
```

In the call to `compute()`, the `OpEnvironment` will use all of the parameters provided to:
* Match an Op based on the name provided, as well as the types of the provided input and output objects
* Execute the Op on the provided input and output objects.

After this step, `outImage` will contain the results of the Gaussian blur on `inImage`.

## Variations on use

### Using a *function* or *inplace* Op

Calling our Gaussian blur as a *computer* above is great when we have pre-allocated output, but for other scenarios we can request Ops as *functions* or *inplaces*.

*Functions* are used when we want to *create* the final output, indicated by ending the builder with `.apply()`:

```java
var outImage = ops.op("filter.gauss").input(inImage, 2.0).apply()
```

*Inplaces* are used when we want to destructively modify one of the existing inputs (which is explicitly forbidden by *computers*; a *computer* Op's output should be a different object from all of its inputs). We indicate this by the `mutate#()` method, where the `#` corresponds to the *parameter index* (starting from 1 for the first parameter) that will be modified:

```java
// Modify the first input in-place
ops.op("filter.gauss").input(inImage, 2.0).mutate1()
```

Note that although the final method call changes for each mode of operation, *this is based on the path taken through the `OpBuilder` chain*. For example, we cannot call the `compute()` method if we haven't provided an `.output()`:

```java
// Does not compute
ops.op("filter.gauss").input(inImage, 2.0).compute()
```

A key takeaway from this section is that how you **request** the Op does not necessarily need to match how the Op is **implemented**. *Functions* and *computers* should be largely interchangeable, thanks to the Ops engine's adaptation subsystem. For the 1.0.0 release we do not have the necessary adapters to go between *inplaces* and the other paradigms, but it is on our [development roadmap](https://github.com/scijava/scijava/issues/47)!

### Repeating execution

When you want to call an Op many times on different inputs, the `OpBuilder` can be used to return the *Op* itself, instead of performing the computation. Instead of calling the `.compute()` function at the end of our `OpBuilder` chain, we can use the `.computer()` method (or `.inplace()` or `.function()`, as appropriate) to get back the matched Op, which can then be reused via its `.compute()` method (or `.apply()` or `.mutate#()`, respectively):

```java
var gaussOp = ops.op("filter.gauss").input(inImage, 2.0).output(outImage).computer();
gaussOp.compute(inImage, 2.0, outImage1);
gaussOp.compute(inImage, 5.0, outImage2);
```

While we do pass concrete inputs and outputs in this example, they are essentially just being used to reason about the desired *types* - which we'll cover in the next section.

*Note that the default `OpEnvironment` implementations cache Op requests* - this means that repeated `OpBuilder` requests targeting the same action will be faster than the original matching call.

### Matching with types/classes

In addition to the `.input()` and `.output()` builder steps, there are parallel `.inType()` and `.outType()`
methods. These accept either a `Class` or a `Nil` - the latter allowing retention of generic types. 
These methods makes it possible to search for an Op without actually having an instance of the objects
you (eventually) plan to operate upon. For example, you might want to look up the `filter.gauss` Op that
would be used to smooth an `ImgPlus` by a particular `double` value, without actually having an `ImgPlus`
image available to pass to the builder request:

```java
var computer = ops.op("filter.gauss").inType(ImgPlus.class, Double.class).outType(ImgPlus.class).computer()
```

In this case, we *must* use the `computer()` terminal method of the builder: we
can only *create* the Op, not directly execute it, since the parameters have
not been concretely specified yet. This is very sensible when we want to re-use a computer many times.

We can also use the `.outType()` methods to add type safety to our `Function` calls:

```java
Img outImage = ops.op("filter.gauss").input(inImage, 2.0).outType(Img.class).apply()
```

## Common Pitfalls: Wildcards

Using [wildcards](https://docs.oracle.com/javase/tutorial/extra/generics/wildcards.html), such as `Img<?> inImage`, can make Op reuse difficult. For example, the following code segment will not compile in a Java runtime:

```java
Img<?> inImage = ArrayImgs.unsignedBytes(128, 128);
var gaussOp = ops.op("filter.gauss").input(inImage, 2.0).output(outImage).computer();
gaussOp.compute(inImage, 2.0, outImage);
```

### Solution 1: Use `compute` instead of `computer`

If you don't need to save the Op to a variable, *just [call it directly](#computing-with-compute)*:

```java
ops.op("filter.gauss").input(inImage, 2.0).output(outImage).compute()
```

Generally speaking, op requests are **cached**, meaning repeated OpBuilder calls that directly execute Ops will **not** significantly decrease performance.

### Solution 2: Avoid using wildcards

If you know that your `Img` will always contain unsigned byte values, for example, define your variable as an `Img<UnsignedByteType>` rather than using `Img<?>`.

### Solution 3: Use raw casts (not type-safe!)

```java
Img<?> inImage = ArrayImgs.unsignedBytes(128, 128);
var gaussOp = ops.op("filter.gauss").input(inImage, 2.0).output(outImage).computer();
gaussOp.compute((Img) inImage, 2.0, outImage);
```

This method should only be used as a last resort.
