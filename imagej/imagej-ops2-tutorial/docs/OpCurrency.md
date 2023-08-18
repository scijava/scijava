# Writing Small, Easy, Modular Ops

SciJava Ops is designed for modularity, extensibility, and granularity - by writing your Ops on the smallest functional elements possible, you'll allow SciJava Ops to lift your Op to new image types, enable parallelism, and more without any additional effort!

## Element-wise Ops

Simple pixel-wise operations like addition, inversion, and more can be written on a single pixel (i.e. `RealType`) - therefore, ImageJ Ops2 takes care to automagically adapt pixel-wise Ops across a wide variety of image types. If you would like to write a pixel-wise Op, we recommend the following structure.

```java

/**
 * A simple pixelwise Op
 * 
 * @implNote op names="my.op"
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

ops.op("my.op").arity2().input(in1, in2).output(out).compute();
```

A similar vein of thought works for simple `List`s and `Array`s - if you have an Op that produces a `Double` from another `Double`, there's no need to write a wrapper to work on `Double[]`s - Ops will do that for you!

```java

/**
 * A simple pixelwise Op
 *
 * @implNote op names="my.op"
 */
class MyPixelwiseOp implements Function<Double, Double> {
    @Override
    public Double apply(final Double input) {
        ... pixelwise computation here ...
    }
}
```

If you then have SciJava Ops, the following Op call will match on your arrays, `List`s, `Set`s, and more - an example is shown below:

```java
List<Double> inList = ...
List<Double> outList = ops.op("my.op2").arity1().input(in1).apply();
```

## Lifting to Neighborhoods

A slightly more complicated class of algorithms operate on local regions around each input pixel. For this class of algorithms, we recommend writing Ops on an imglib2-algorithm `net.imglib2.algorithm.neighborhood.Neighborhood` object. 

```java

/**
 * A simple neighborhood-based Op
 *
 * @implNote op names="my.op"
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

ops.op("my.op").arity2().input(input, shape).output(output).compute()
```

## Using dependencies

Even when your Op must be run on entire Images, you can use Op Dependencies to promote extensibility, reusability and flexibility.

For example, if your Op must iterate over some algorithm n times, consider internalizing the algorithm within a separate Op, and make it a depedency, like shown below:

```java
import org.scijava.ops.spi.OpDependency;


/**
 * An Op that needs to run on the whole image
 *
 * @implNote op names="my.op"
 */
class<I, O> ComplicatedOp implements Computers.Arity1<Img<I>,Img<O>>{
    @OpDependency(name = "my.internalOp")
    private final Computers.Arity1<Img<I>, Img<O>> iteration;

    @Override
    public void compute(final Img<I> input, final Img<O> container){
        for(int i=0; i < numItrs; i++){
            iteration.compute(input, container);
		}
    }
}

/**
 * An Op that executes a single iteration of some iterative process
 * @implNote op names="my.internalOp"
 */
class<I, O> ComplicatedOp implements Computers.Arity1<Img<I>,Img<O>>{
    @Override
    public void compute(final Img<I> input, final Img<O> container){
        ...perform an iteration...
    }
}
```








