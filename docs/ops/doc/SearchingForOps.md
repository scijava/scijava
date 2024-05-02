# Searching for Ops in the Environment

As the `OpEnvironment` is fully extensible, different `OpEnvironment`s might contain different Ops, and it is important to be able to query an `OpEnvironment` about the available Ops.

The `OpEnvironment.help()` API allows you to query an `OpEnvironment` about the types of Ops it contains, as we show in the following sections. **Note that the example printouts from the help API may not reflect the Ops available in *your* Op environment**. 

## Searching for operations

The no-argument method `OpEnvironment.help()` is designed to give you a broad overview over the *categories* of Ops available within the `OpEnvironment`:

```groovy
import org.scijava.ops.api.OpEnvironment
ops = OpEnvironment.getEnvironment()

print(ops.help())
```

This gives the following printout:

```
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

## Interrogating a Namespace

You can choose one of the above namespaces, and `ops.help()` will give you information about the algorithms contained within:
```groovy
import org.scijava.ops.api.OpEnvironment
ops = OpEnvironment.getEnvironment()

print(ops.help("filter"))
```

This gives the following printout:

```
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

## Signatures for Op Names

Finally, you can use `OpEnvironment.help()` on any Op name to see the list of signatures:

```groovy
import org.scijava.ops.api.OpEnvironment
ops = OpEnvironment.getEnvironment()

print(ops.help("filter.gauss"))
```

```
filter.gauss:
	- (input, sigmas, @CONTAINER container1) -> None
	- (input, sigmas, outOfBounds = null, @CONTAINER container1) -> None
	- (input, sigma, @CONTAINER container1) -> None
	- (input, sigma, outOfBounds = null, @CONTAINER container1) -> None
```

Note that these descriptions are simple, and you can obtain more verbose descriptions by instead using the method `OpEnvironment.helpVerbose()`:

```groovy
import org.scijava.ops.api.OpEnvironment
ops = OpEnvironment.getEnvironment()

print(ops.helpVerbose("filter.gauss"))
```

```
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
