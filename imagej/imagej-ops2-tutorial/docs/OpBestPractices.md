# Best Practices When Writing Ops

SciJava Ops is designed for modularity, extensibility, and granularity - you can exploit these aspects by adhering to the following guidelines when writing Ops:

## Using Dependencies

If you are writing an Op that performs many intermediate operations, there's a good chance someone else has written (and even optimized) some or all of them.

If not there's a great chance that others would benefit from you writing them separately so that they can reuse your work!

To accomodate this separation, Ops can reuse other Ops by declaring Op dependencies. When Ops returns an instance of your Op, it will also find and instantiate all of your dependencies!

To illustrate how this might work, here's a trivial example of an Op that computes the mean of a `double[]`:

```java

import org.scijava.ops.spi.OpDependency;

/**
 * A simple mean calculator
 *
 * @implNote op names="stats.mean"
 */
class DoubleMeanOp implements Function<double[], Double> {

    @OpDependency(name="stats.sum")
    public Function<double[], Double> sumOp;

    @OpDependency(name="stats.size")
    public Function<double[], Double> sizeOp;
		
    public Double apply(final double[] inArray) {
        final Double sum = sumOp.apply(inArray);
        final Double size = sizeOp.apply(inArray);
        return sum / size;
    }
}
```
In this example, the two `OpDependency` Ops already exist within the SciJava Ops Engine module - but if they didn't, you'd  define  them just like any other Op:

```java
/**
 * A simple summer
 *
 * @implNote op names="stats.sum"
 */
class DoubleSumOp implements Function<double[], Double> {
	public Double apply(final double[] inArray) {
		double sum = 0;
		for (double v : inArray) {
			sum += v;
		}
		return i;
	}
}

/**
 * A simple size calculator
 *
 * @implNote op names="stats.size"
 */
class DoubleSizeOp implements Function<double[], Double> {
	public Double apply(final double[] inArray) {
		return inArray.length;
	}
}

```

## Element-wise Ops

Simple pixel-wise operations like addition, inversion, and more can be written on a single pixel (i.e. `RealType`) - therefore, ImageJ Ops2 takes care to automagically adapt pixel-wise Ops across a wide variety of image types. If you would like to write a pixel-wise Op, we recommend the following structure.

```java
/**
 * A simple pixelwise Op
 * 
 * @implNote op names="pixel.op"
 */
class <T extends RealType<T>> MyPixelwiseOp implements Computers.Arity2<T, T, T> {
	
	@Override
    public void compute(final T in1, final T in2, final T out) {
        ... pixelwise computation here ...
    }
}
```

If you then have ImageJ Ops2, the following Op call will match your pixel-wise Op using ImageJ Ops2's lifting mechanisms:

```java
ArrayImg<UnsignedByteType> in1 = ...
ArrayImg<UnsignedByteType> in2 = ...
ArrayImg<UnsignedByteType> out = ...

ops.op("pixel.op").arity2().input(in1, in2).output(out).compute();
```

A similar vein of thought works for simple `List`s and `Array`s - if you have an Op that produces a `Double` from another `Double`, there's no need to write a wrapper to work on `Double[]`s - Ops will do that for you!

```java
/**
 * An element-wise Op
 *
 * @implNote op names="element.op"
 */
class MyElementOp implements Function<Double, Double> {
    @Override
    public Double apply(final Double input) {
        ... pixelwise computation here ...
    }
}
```

If you then have SciJava Ops, the following Op call will match on your arrays, `List`s, `Set`s, and more - an example is shown below:

```java
List<Double> inList = ...
List<Double> outList = ops.op("element.op").arity1().input(in1).apply();
```

## Neighborhood-wise Ops

A slightly more complicated class of algorithms operate on local regions around each input pixel. For this class of algorithms, we recommend writing Ops on an imglib2-algorithm `net.imglib2.algorithm.neighborhood.Neighborhood` object. 

```java

/**
 * A simple neighborhood-based Op
 *
 * @implNote op names="neighborhood.op"
 */
class <T extends RealType<T>> MyNeighborhoodOp implements Computers.Arity1<Neighborhood<T>, T> {
	@Override
	public void compute(final Neighborhood<T> input, final T output) {
        ... pixelwise computation here ...
	}
}
```

ImageJ Ops2 will then lift this algorithm to operate on images in parallel, requiring users to only define the shape of the neighborhoods to be used:

```java
ArrayImg<DoubleType> input = ...
Shape neighborhoodShape = ...
ArrayImg<DoubleType> output = ...

ops.op("neighborhood.op").arity2().input(input, shape).output(output).compute()
```
