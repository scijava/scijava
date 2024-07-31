# Searching for Ops in the Environment

The first step when workin with Ops is always to obtain an `OpEnvironment`: your gateway to all Ops functionality.

If you're working in a [Fiji script](ScriptingInFiji) then this is done with a script parameter:

```
#@ OpEnvironment ops
```

Otherwise we can import and build one ourselves:

```
import org.scijava.ops.api.OpEnvironment
ops = OpEnvironment.build()
```

Typically we would only want to do this once per application, to avoid diverging environments and recurring the performance cost of the build. All code examples in this section will assume we have created an `OpEnvironment` named `ops`.

As the `OpEnvironment` is fully extensible, different `OpEnvironment`s might contain different Ops, so it is important to be able to query an `OpEnvironment` about its available Ops. We also need to be able to get information about the usage of these Ops, to know what parameters may be required.

The `OpEnvironment.help()` API is your window into the `OpEnvironment`. In the following sections we cover the different types of information that can be obtained. **Note that the exact printouts from the help API may be different from the Ops available in *your* environment**. 

## Listing Namespaces

The no-argument method `OpEnvironment.help()` is designed to give you a broad overview over the *categories* (namespaces) of Ops available within the `OpEnvironment`:

```
print(ops.help())
```

Might print output such as:

```text
Namespaces:
	> coloc
	> convert
	> copy
	> create
	> deconvolve
	> expression
	> features
	> filter
	> geom
	> image
	> imageMoments
	> labeling
	> linalg
	> logic
	> map
	> math
	> morphology
	> segment
	> stats
	> thread
	> threshold
	> topology
	> transform
	> types
```

These namespace categories can then be interrogated further to explore the particular Ops in each.

## Querying a Namespace

You can choose one of the above namespaces, and `ops.help()` will give you information about the algorithms contained within:
```
print(ops.help("filter"))
```

Prints the current list of `filter` ops in the `OpEnvironment`:

```text
Names:
	> filter.dog
	> filter.addNoise
	> filter.addPoissonNoise
	> filter.addUniformNoise
	> filter.applyCenterAware
	> filter.bilateral
	> filter.convolve
	> filter.convolveNaive
	> filter.correlate
	> filter.createFFTOutput
	> filter.derivativeGauss
	> filter.fft
	> filter.fftSize
	> filter.frangiVesselness
	> filter.gauss
	> filter.hessian
	> filter.ifft
	> filter.linearFilter
	> filter.max
	> filter.mean
	> filter.median
	> filter.min
	> filter.padInput
	> filter.padInputFFT
	> filter.padInputFFTMethods
	> filter.padIntervalCentered
	> filter.padIntervalOrigin
	> filter.padShiftFFTKernel
	> filter.padShiftKernel
	> filter.padShiftKernelFFTMethods
	> filter.partialDerivative
	> filter.sigma
	> filter.sobel
	> filter.tubeness
	> filter.variance
```

## Querying Op Signatures

Finally, you can use `OpEnvironment.help()` on any Op name to see the list of signatures:

```
print(ops.help("filter.gauss"))
```

```text
filter.gauss:
	- (input, sigmas, @CONTAINER container1) -> None
	- (input, sigmas, outOfBounds = null, @CONTAINER container1) -> None
	- (input, sigma, @CONTAINER container1) -> None
	- (input, sigma, outOfBounds = null, @CONTAINER container1) -> None
```

## In-depth Op Information

The basic descriptions from `OpEnvironment.help()` are intentionally simplified to avoid providing overwhelming amounts of information. However, you can obtain more complete descriptions, including documentation (if available), from `OpEnvironment.helpVerbose()`:

```
print(ops.helpVerbose("filter.gauss"))
```

Gives us actual typing and usage notes for the parameters:

```text
filter.gauss:
	- org.scijava.ops.image.filter.gauss.Gaussians.defaultGaussRAI(net.imglib2.RandomAccessibleInterval<I>,double[],net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>>,net.imglib2.RandomAccessibleInterval<O>)
		> input : net.imglib2.RandomAccessibleInterval<I>
			the input image
		> sigmas : double[]
			the sigmas for the gaussian
		> outOfBounds (optional) : net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>>
			the {@link OutOfBoundsFactory} that defines how the
			calculation is affected outside the input bounds.
		> container1 : @CONTAINER net.imglib2.RandomAccessibleInterval<O>
			the output image
	- org.scijava.ops.image.filter.gauss.Gaussians.gaussRAISingleSigma(net.imglib2.RandomAccessibleInterval<I>,double,net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>>,net.imglib2.RandomAccessibleInterval<O>)
		> input : net.imglib2.RandomAccessibleInterval<I>
			the input image
		> sigma : java.lang.Double
			the sigmas for the Gaussian
		> outOfBounds (optional) : net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>>
			the {@link OutOfBoundsFactory} that defines how the
			calculation is affected outside the input bounds.
		> container1 : @CONTAINER net.imglib2.RandomAccessibleInterval<O>
			the preallocated output image
```

`OpEnvironment.helpVerbose()` can be used interchangeably whenever you would use `OpEnvironment.help()`, as needed.
