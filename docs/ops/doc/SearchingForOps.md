# Searching for Ops in the Environment

As the `OpEnvironment` is fully extensible, different `OpEnvironment`s might contain different Ops, and it is important to be able to query an `OpEnvironment` about the available Ops.

The `OpEnvironment.help()` API allows you to query an `OpEnvironment` about the types of Ops it contains, as we show in the following sections: 

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
	> adapt
	> coloc
	> convert
	> copy
	> cp
	> create
	> deconvolve
	> eval
	> features
	> filter
	> focus
	> geom
	> identify
	> identity
	> image
	> imageMoments
	> labeling
	> linalg
	> logic
	> lossReporter
	> map
	> math
	> morphology
	> project
	> segment
	> simplify
	> slice
	> source
	> src
	> stats
	> test
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
Namespaces:
	> filter.DoG
	> filter.addNoise
	> filter.addPoissonNoise
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
Ops:
	> filter.gauss(
		 Inputs:
			RandomAccessibleInterval<NumericType> null -> the input image
			ObjectArray<Number> null -> the sigmas for the gaussian
		 Containers (I/O):
			RandomAccessibleInterval<NumericType> container1 -> the output image
	)
	
	> filter.gauss(
		 Inputs:
			RandomAccessibleInterval<NumericType> null -> the input image
			ObjectArray<Number> null -> the sigmas for the gaussian
			OutOfBoundsFactory<NumericType, RandomAccessibleInterval<NumericType>> null? -> the {@link OutOfBoundsFactory} that defines how the
	          calculation is affected outside the input bounds. (required =
	          false)
		 Containers (I/O):
			RandomAccessibleInterval<NumericType> container1 -> the output image
	)
	
	> filter.gauss(
		 Inputs:
			RandomAccessibleInterval<NumericType> null -> the input image
			Number null -> the sigmas for the Gaussian
		 Containers (I/O):
			RandomAccessibleInterval<NumericType> container1 -> the preallocated output image
	)
	
	> filter.gauss(
		 Inputs:
			RandomAccessibleInterval<NumericType> null -> the input image
			Number null -> the sigmas for the Gaussian
			OutOfBoundsFactory<NumericType, RandomAccessibleInterval<NumericType>> null? -> the {@link OutOfBoundsFactory} that defines how the
	          calculation is affected outside the input bounds. (required =
	          false)
		 Containers (I/O):
			RandomAccessibleInterval<NumericType> container1 -> the preallocated output image
	)
```

Note that these descriptions are simple, and you can obtain more verbose descriptions by instead using the method `OpEnvironment.helpVerbose()`:

```groovy
import org.scijava.ops.api.OpEnvironment
ops = OpEnvironment.getEnvironment()

print(ops.helpVerbose("filter.gauss"))
```

```
Ops:
	> filter.gauss(
		 Inputs:
			net.imglib2.RandomAccessibleInterval<I> null -> the input image
			double[] null -> the sigmas for the gaussian
			net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>> null? -> the {@link OutOfBoundsFactory} that defines how the
	          calculation is affected outside the input bounds. (required =
	          false)
		 Containers (I/O):
			net.imglib2.RandomAccessibleInterval<O> container1 -> the output image
	)
	
	> filter.gauss(
		 Inputs:
			net.imglib2.RandomAccessibleInterval<I> null -> the input image
			double[] null -> the sigmas for the gaussian
		 Containers (I/O):
			net.imglib2.RandomAccessibleInterval<O> container1 -> the output image
	)
	
	> filter.gauss(
		 Inputs:
			net.imglib2.RandomAccessibleInterval<I> null -> the input image
			java.lang.Double null -> the sigmas for the Gaussian
			net.imglib2.outofbounds.OutOfBoundsFactory<I, net.imglib2.RandomAccessibleInterval<I>> null? -> the {@link OutOfBoundsFactory} that defines how the
	          calculation is affected outside the input bounds. (required =
	          false)
		 Containers (I/O):
			net.imglib2.RandomAccessibleInterval<O> container1 -> the preallocated output image
	)
	
	> filter.gauss(
		 Inputs:
			net.imglib2.RandomAccessibleInterval<I> null -> the input image
			java.lang.Double null -> the sigmas for the Gaussian
		 Containers (I/O):
			net.imglib2.RandomAccessibleInterval<O> container1 -> the preallocated output image
	)

```
