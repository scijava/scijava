# Reasoning about Ops

The declarative nature of Ops is convenient for matching and execution, however there are times where it is necessary to understand where an Op comes from. This page describes the tools provided by SciJava Ops to understand the code underneath.

Naturally, to understand where an Op came from, you first need an Op:

```java
var img = ...;
var out = ...;
var sigma = 5.0;
// This Op call finds a "filter.gauss Op" that blurs an image using a provided
// sigma, placing the result in out.
var op = ops.op("filter.gauss").input(img, sigma).output(out).computer();
```

## Introduction

Each Op is generated from an `OpInfo` object, describing an Op's source code, version, and other associated metadata. If you've matched an Op `op`, you can obtain its `OpInfo` using the method `Ops.info`:

```java
import org.scijava.ops.api.Ops;

var info = Ops.info(op);
```

The `OpInfo` often provides all needed information, but if `op` utilizes dependencies or other framework features, you may wish to introspect the "rich" Op instead, using the method `Ops.rich`.

```java
import org.scijava.ops.api.Ops;

var richOp = Ops.rich(op);
```

These two methods provide gateways into learning more about where your Op came from!

## Code and Versions

For reproducibility, it is important to know not only the underlying lines of code backing each executed Op, but additionally the *version* of that line of code, as different versions of the same codebase can produce different results. Fortunately, SciJava Ops provides mechanisms to determine both!

A unique identifier to the lines of code can be obtained from each Op's `OpInfo`, using the method `OpInfo.implementationName()`. For example, the following

```java
System.out.println(Ops.info(op).implementationName());
```

might print out (if you're using `scijava-ops-image`):

```
> org.scijava.ops.image.filter.gauss.Gaussians.gaussRAISingleSigma(net.imglib2.RandomAccessibleInterval<I>,double,net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>>,net.imglib2.RandomAccessibleInterval<O>)Reduction1
```

Note that in many cases, including the case above, the implementation name may not itself be an explicit piece of code, however it must always explain how an explicit piece of code became the Op you've matched.

Similarly, the method `OpInfo.version()` describes **the version of the Op**, which is set by whichever component declares the Op. This version might change due to:
* An update in the version of the underlying implementation (such as a bump in the version of OpenCV)
* An update to the Op itself (such as the inclusion of an additional alias, or a change in the Op priority)

SciJava Ops guarantees users identical outputs from an Op execution given identical Op inputs **and** an identical `OpEnvironment`. Notably, your results may differ slightly if you:
* Add new Ops into your `OpEnvironment`, usually by adding new Ops. This might cause a new Op to take precedence over an existing Op.
* Change the version of an Op within the `OpEnvironment`
* Pass different inputs to the Op.

## Op Signatures

While SciJava Ops requires the aforementioned constraints to ensure reproducible Op *matches*, the concept of **Op Signatures** enables SciJava Ops to **reproducibly reconstruct prior matches** in *new environments* (assuming the prior Op is still available). Op signatures allow users to enforce reproducibility when e.g. adding new Ops to the `OpEnvironment`.

Given an matched Op `op`, its signature can be obtained using the static method `Ops.signature()`, which *distills* all Op implementation names and versions into a single string. For example, the following

```java
System.out.println(Ops.signature(op));
```

might print out (again using `scijava-ops-image`):

```
|Reduction:|ParamsReduced:1|OriginalInfo:|Info:org.scijava.ops.image.filter.gauss.Gaussians.gaussRAISingleSigma(net.imglib2.RandomAccessibleInterval<I>,double,net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>>,net.imglib2.RandomAccessibleInterval<O>)@1.0.0{}
```

**Assuming all utilized Ops and Op features are still available**, the Op can then be reconstructed using the method `OpEnvironment.opFromSignature()`:

```java
// The value of signature could be saved within a script for future reconstruction
String signature = Ops.signature(op);

...

// NB This generic type construct is needed for the OpEnvironment to provide a Computer Op.
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

Often, Ops contain Op dependencies, which may have dependencies themselves. As such, the Ops returned by SciJava Ops are best thought of as a [Tree](https://en.wikipedia.org/wiki/Tree_(data_structure)). While an Op's signature, described above, describes the entire Tree, it is often more convenient to interrogate dependencies using the `InfoTree` data structure, obtained using the method `Ops.infoTree()`:

```java
var tree = Ops.infoTree(op);
```

The `InfoTree` API allows users to interrogate:
* The root `OpInfo`, accessible using `InfoTree.info()`
* All dependencies, which are each themselves an `InfoTree`, using `InfoTree.dependencies()`.

The `InfoTree` allows users to answer the question of which Ops are being used as dependencies, providing access to an otherwise unavailable feature of SciJava Ops.

## Op History

A final task essential to Op reproducibility is determining the list of Ops responsible for the current state of a given Object, which is helpful when many Ops contribute to some final state. To answer this question, each `OpEnvironment` records Op executions within an `OpHistory` object, accessible using the method `OpEnvironment.history()`.

```java
// OpEnvironment.history() provides access to a OpHistory object
var history = ops.history();

// Since an Op was responsible for the results in out, it is listed in OpHistory.executionsUpon()
var usedOps = history.executionsUpon(out);
```

Using the `OpHistory` object, we can see that our gaussian blur Op `op` is at the end of the list, since it mutated `out` last. If `out` was additionally created using SciJava Ops, the Op(s) responsible for creating the image would also be included.