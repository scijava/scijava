# Reasoning about Ops

The declarative nature of Ops is convenient for matching and execution, but sometimes it is necessary to introspect an Op to learn about its structure (e.g. its input and output types) and provenance (i.e. where it comes from). This page describes the tools provided by SciJava Ops to perform such queries.

## Obtaining an Op reference

First, let's find an Op to work with. We'll look up a `filter.gauss` Op that blurs an image using a provided sigma, and writes the result into an existing output container. The following code should accomplish this, assuming the SciJava Ops Image library is part of our environment:

```java
import org.scijava.ops.api.OpEnvironment;
import net.imglib2.img.array.ArrayImgs;

var ops = OpEnvironment.build();

var img = ArrayImgs.unsignedBytes(256, 256);
var out = img.copy();
var sigma = 5.0;

// By calling the computer() method, we save a reference
// to the matched Op itself, rather than executing it.
var op = ops.op("filter.gauss").input(img, sigma).output(out).computer();
```

## Op Infos

Each Op is generated from an `OpInfo` object, describing an Op's source, version, and other associated metadata. If you've matched an Op `op` as above, you can obtain its `OpInfo` using the method `Ops.info(op)`:

```java
import org.scijava.ops.api.Ops;
var info = Ops.info(op);
```

This `OpInfo` object contains several useful accessor methods. Here is a demonstration using [JShell](https://docs.oracle.com/en/java/javase/21/jshell/):

```java
jshell> info.names()
$3 ==> [filter.gauss]

jshell> System.out.println(info.description())
 Gaussian filter which can be called with single sigma, i.e. the sigma is
 the same in each dimension.

jshell> info.priority()
$5 ==> 0.0

jshell> info.version()
$6 ==> "1.0.0"

jshell> info.opType()
$7 ==> org.scijava.function.Computers.Arity2<net.imglib2.RandomAccessibleInterval<I extends net.imglib2.type.numeric.NumericType<I>>, java.lang.Double, net.imglib2.RandomAccessibleInterval<O extends net.imglib2.type.numeric.NumericType<O>>>

jshell> info.inputs()
$8 ==> [input: net.imglib2.RandomAccessibleInterval<I> [INPUT] {the input image}, sigma: java.lang.Double [INPUT] {the sigmas for the Gaussian}, output: net.imglib2.RandomAccessibleInterval<O> [CONTAINER] {the preallocated output image}]

jshell> info.inputTypes()
$9 ==> [net.imglib2.RandomAccessibleInterval<I>, class java.lang.Double, net.imglib2.RandomAccessibleInterval<O>]

jshell> info.output()
$10 ==> output: net.imglib2.RandomAccessibleInterval<O> [CONTAINER] {the preallocated output image}

jshell> info.outputType()
$11 ==> net.imglib2.RandomAccessibleInterval<O>

jshell> import org.scijava.struct.Structs

jshell> System.out.println(Structs.toString(info.struct()))
input: net.imglib2.RandomAccessibleInterval<I> [INPUT] {the input image}
sigma: java.lang.Double [INPUT] {the sigmas for the Gaussian}
output: net.imglib2.RandomAccessibleInterval<O> [CONTAINER] {the preallocated output image}

jshell> info.declaredHints()
$14 ==> [reduction.FORBIDDEN]
```

See the javadoc of these various methods for further details.

If `op` utilizes dependencies or other framework features, there is also a `RichOp` wrapper providing access to additional metadata, with methods including `.env()`, `.name()`, `.hints()`, and `.infoTree()`.

## Implementations and Versions

For reproducibility, it is important to know not only the underlying implementations, or discrete lines of code, backing each executed Op, but additionally the *version* of that implementation if it changes over time. Fortunately, SciJava Ops provides mechanisms to determine both!

A unique identifier to an Op's implementation can be obtained from its `OpInfo` using the method `OpInfo.implementationName()`. For example, the following

```java
System.out.println(Ops.info(op).implementationName());
```

might print out (if you're using `scijava-ops-image` version `1.0.0`):

```{code-block}
:class: no-copybutton
org.scijava.ops.image.filter.gauss.Gaussians.gaussRAISingleSigma(net.imglib2.RandomAccessibleInterval<I>,double,net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>>,net.imglib2.RandomAccessibleInterval<O>)Reduction1
```

Note that in many cases, including the case above, the implementation name may not itself be an explicit piece of code, however it must always explain how an explicit piece of code became the Op you've matched.

Similarly, the method `OpInfo.version()` describes **the version of the Op**, which is set by whichever component declares the Op. This version might change due to:
* An update in the version of the underlying implementation (such as a bump in the version of `scijava-ops-image`)
* An update to the Op itself (such as the inclusion of an additional alias, or a change in the Op priority)

SciJava Ops guarantees identical outputs from an Op execution given identical inputs **and** an identical `OpEnvironment`. Your results may differ if you:
* Add new Ops into your `OpEnvironment`, if a new Op takes precedence over an existing Op
* Change the version of an Op within the `OpEnvironment` by updating a component
* Pass different inputs to the Op

## Op Signatures

While SciJava Ops requires the aforementioned constraints to ensure reproducible Op *matches*, the concept of **Op Signatures** enables SciJava Ops to **reproducibly reconstruct prior matches in new environments** (assuming all required Ops are still available).

Given a matched Op `op`, its signature can be obtained using the static method `Ops.signature(op)`, which distills all Op implementation names and versions into a single string. For example, the following

```java
System.out.println(Ops.signature(op));
```

might print out (again using `scijava-ops-image` version `1.0.0`):

```{code-block}
:class: no-copybutton
|Reduction:|ParamsReduced:1|OriginalInfo:|Info:org.scijava.ops.image.filter.gauss.Gaussians.gaussRAISingleSigma(net.imglib2.RandomAccessibleInterval<I>,double,net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>>,net.imglib2.RandomAccessibleInterval<O>)@1.0.0{}
```

**Assuming all utilized Ops and Op features are still available**, the Op can then be reconstructed using the method `OpEnvironment.opFromSignature(signature, opType)`:

```java
// The value of signature could be saved within a script for future reconstruction
String signature = Ops.signature(op);

...

// NB: This generic type construct is needed for the OpEnvironment to provide a Computer Op.
var opType = new Nil<Computers.Arity2<Img<DoubleType>, Double, Img<DoubleType>>>() {};
// Reconstruct the Op
var sameOp = ops.opFromSignature(signature, opType);
// We could then call this Op with our input image, real-valued sigma, and output image container
sameOp.compute(img, sigma, out);
```

There still exist limitations with reconstructive features, including:
* [scijava/scijava#220](https://github.com/scijava/scijava/issues/220) - The inability to reconstruct an Op using simpler `opType`s.
* [scijava/scijava#219](https://github.com/scijava/scijava/issues/219) - The inability to reconstruct Op matches with omitted optional parameters.

## Info Trees

Ops often utilize Op dependencies, which may have dependencies themselves. As such, the Ops returned by SciJava Ops are best thought of as a [tree](https://en.wikipedia.org/wiki/Tree_(data_structure)). While an Op's signature, described above, represents the entire Tree, it is often more convenient to interrogate dependencies using the `InfoTree` data structure, obtained using the method `Ops.infoTree(op)`:

```java
var tree = Ops.infoTree(op);
```

The `InfoTree` API allows users to interrogate:
* The root `OpInfo`, accessible using `InfoTree.info()`
* All dependencies, which are each themselves an `InfoTree`, using `InfoTree.dependencies()`.

## Op History

It is often useful to determine the list of Ops responsible for the current state of a given Object. To aid in this task, each `OpEnvironment` records Op executions within an `OpHistory` object, accessible using the method `OpEnvironment.history()`.

```java
// OpEnvironment.history() provides access to a OpHistory object
var history = ops.history();

// Since an Op was responsible for the results written into the "out" object, it is listed in OpHistory.executionsUpon()
var usedOps = history.executionsUpon(out);
```

Using the `OpHistory` object, we can see that our gaussian blur Op `op` is at the end of the list, since it wrote to `out` last. If `out` was additionally created using SciJava Ops, the Op(s) responsible for creating the image would also be included.
