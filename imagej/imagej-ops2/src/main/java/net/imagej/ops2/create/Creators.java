/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imagej.ops2.create;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.BooleanType;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.scijava.function.Functions;
import org.scijava.function.Producer;

public class Creators<N extends NativeType<N>, L, I extends IntegerType<I>, T extends Type<T>, C extends ComplexType<C>, W extends ComplexType<W> & NativeType<W>, B extends BooleanType<B>> {

	/* ImgFactories */

	String iF = "imgFactory";

	/**
	 * @output imgFactory
	 * @implNote op names='create, create.imgFactory'
	 */
	public final Producer<ImgFactory<DoubleType>> factorySource = () -> new ArrayImgFactory(new DoubleType());

	// note that dims is not actually passed to the ImgFactory but instead is
	// inspected to determine which will be returned.
	/**
	 * @input dimensions
	 * @output imgFactory
	 * @implNote op names='create, create.imgFactory'
	 */
	public final Function<Dimensions, ImgFactory<DoubleType>> factoryFromDims = (dims) -> Util
			.getSuitableImgFactory(dims, new DoubleType());

	/**
	 * @input dimensions
	 * @input type
	 * @output imgFactory
	 * @implNote op names='create, create.imgFactory'
	 */
	public final BiFunction<Dimensions, L, ImgFactory<L>> factoryFromDimsAndType = Util::getSuitableImgFactory;

	/**
	 * @input img
	 * @output imgFactory
	 * @implNote op names='create, create.imgFactory'
	 */
	public final Function<Img<L>, ImgFactory<L>> factoryFromImg = (img) -> img.factory();

	/* Imgs */

	/**
	 * @input dimensions
	 * @input type
	 * @input factory
	 * @output img
	 * @implNote op names='create, create.img'
	 */
	public final Functions.Arity3<Dimensions, T, ImgFactory<T>, Img<T>> imgFromDimsTypeAndFactory = (dims, type,
			factory) -> Imgs.create(factory, dims, type);

	/**
	 * @input dimensions
	 * @input type
	 * @output img
	 * @implNote op names='create, create.img'
	 */
	public final BiFunction<Dimensions, T, Img<T>> imgFromDimsAndType = (dims, type) -> {
		ImgFactory<T> factory = dims instanceof Img<?> ? ((Img<T>) dims).factory()
				: Util.getSuitableImgFactory(dims, type);
		return Imgs.create(factory, dims, type);
	};

	/**
	 * @input intArray
	 * @output img
	 * @implNote op names='create, create.img'
	 */
	public final Function<int[], Img<DoubleType>> imgFromIntArray = (array) -> {
		FinalDimensions dims = new FinalDimensions(array);
		DoubleType type = new DoubleType();
		return Imgs.create(Util.getSuitableImgFactory(dims, type), dims, type);
	};

	/**
	 * @input integerArray
	 * @output img
	 * @implNote op names='create, create.img'
	 */
	public final Function<Integer[], Img<DoubleType>> imgFromIntegerArray = (array) -> imgFromIntArray
			.apply(Arrays.stream(array).mapToInt(Integer::intValue).toArray());

	/**
	 * @input longArray
	 * @output img
	 * @implNote op names='create, create.img'
	 */
	public final Function<long[], Img<DoubleType>> imgFromPrimitiveLongArray = (array) -> {
		FinalDimensions dims = new FinalDimensions(array);
		DoubleType type = new DoubleType();
		return Imgs.create(Util.getSuitableImgFactory(dims, type), dims, type);
	};

	/**
	 * @input longArray
	 * @output img
	 * @implNote op names='create, create.img'
	 */
	public final Function<Long[], Img<DoubleType>> imgFromLongArray = (array) -> imgFromPrimitiveLongArray
			.apply(Arrays.stream(array).mapToLong(Long::longValue).toArray());

	/**
	 * @input ii
	 * @output img
	 * @implNote op names='create, create.img', priority='0.'
	 */
	public final Function<IterableInterval<T>, Img<T>> imgFromII = (ii) -> imgFromDimsAndType.apply(ii,
			ii.firstElement());

	/**
	 * @input inputImg
	 * @output img
	 * @implNote op names='create, create.img', priority='100.'
	 */
	public final Function<Img<T>, Img<T>> imgFromImg = (img) -> Imgs.create(img.factory(), img, img.firstElement());

	/**
	 * @input interval
	 * @output img
	 * @implNote op names='create, create.img', priority='-100.'
	 */
	public final Function<Interval, Img<DoubleType>> imgFromInterval = (interval) -> {
		DoubleType type = new DoubleType();
		return Imgs.create(Util.getSuitableImgFactory(interval, type), interval, type);
	};

	/**
	 * @input rai
	 * @output img
	 * @implNote op names='create, create.img', priority='0.'
	 */
	public final Function<RandomAccessibleInterval<T>, Img<T>> imgFromRAI = (rai) -> imgFromDimsAndType.apply(rai,
			Util.getTypeFromInterval(rai));

	/* IntegerType */

	/**
	 * @output integerType
	 * @implNote op names='create, create.integerType', priority='0.'
	 */
	public final Producer<LongType> integerTypeSource = () -> new LongType();

	/* Type */

	/**
	 * @input sampleType
	 * @output type
	 * @implNote op names='create, create.type'
	 */
	public final Function<T, T> typeFromSampleType = (sample) -> sample.createVariable();

	/**
	 * @output booleanType
	 * @implNote op names='create, create.type', priority='-100.'
	 */
	public final Producer<BitType> booleanTypeSource = () -> new BitType();

	/* ImgLabeling */

	/**
	 * @input img
	 * @output imgLabeling
	 * @implNote op names='create, create.imgLabeling'
	 */
	public final Function<Img<I>, ImgLabeling<L, I>> imgLabelingFromImg = ImgLabeling::new;

	/**
	 * @input dimensions
	 * @input type
	 * @input factory
	 * @output imgLabeling
	 * @implNote op names='create, create.imgLabeling'
	 */
	public final Functions.Arity3<Dimensions, I, ImgFactory<I>, ImgLabeling<L, I>> imgLabelingFromDimsTypeAndFactory = (dims,
			type, factory) -> {
		Img<I> img = Imgs.create(factory, dims, type);
		return imgLabelingFromImg.apply(img);
	};

	/**
	 * @input dimensions
	 * @input type
	 * @output imgLabeling
	 * @implNote op names='create, create.imgLabeling'
	 */
	public final BiFunction<Dimensions, I, ImgLabeling<L, I>> imgLabelingFromDimsAndType = (dims,
			type) -> imgLabelingFromDimsTypeAndFactory.apply(dims, type, Util.getSuitableImgFactory(dims, type));

	/* Kernel */

	/**
	 * @input values
	 * @input type
	 * @output kernelRAI
	 * @implNote op names='create, create.kernel'
	 */
	public final BiFunction<double[][], C, RandomAccessibleInterval<C>> kernel2DFromValuesAndType = (arr, type) -> {
		FinalDimensions dims = new FinalDimensions(new long[] { arr.length, arr[0].length });
		RandomAccessibleInterval<C> rai = (RandomAccessibleInterval<C>) imgFromDimsAndType.apply(dims, (T) type);
		Cursor<C> cursor = Views.iterable(rai).cursor();
		for (int j = 0; j < arr.length; j++) {
			for (int k = 0; k < arr[j].length; k++) {
				cursor.fwd();
				cursor.get().setReal(arr[j][k]);
			}
		}

		return rai;
	};

	// TODO do we want to support this and if so is this the right way to do it?
	/**
	 * @input values
	 * @output kernelRAI
	 * @implNote op names='create, create.kernel'
	 */
	public final Function<double[][], RandomAccessibleInterval<DoubleType>> kernel2DFromValues = (
			arr) -> (RandomAccessibleInterval<DoubleType>) kernel2DFromValuesAndType.apply(arr, (C) new DoubleType());

	/* Gaussian Kernel */

	/**
	 * @input numDims
	 * @input type
	 * @output gaussKernelRAI
	 * @implNote op names='create, create.kernelGauss'
	 */
	public final BiFunction<double[], C, RandomAccessibleInterval<C>> kernelGauss = (numDims, type) -> {
		return DefaultCreateKernelGauss.createKernel(numDims, type, imgFromDimsAndType);
	};

	/**
	 * @input sigmas
	 * @output gaussKernelRAI
	 */
	// TODO do we want to support this and if so is this the right way to do it?
	/**
	 * @implNote op names='create, create.kernelGauss'
	 */
	public final Function<double[], RandomAccessibleInterval<DoubleType>> kernelGaussDoubleType = (
			sigmas) -> (RandomAccessibleInterval<DoubleType>) kernelGauss.apply(sigmas, (C) new DoubleType());

	/**
	 * @input sigma
	 * @input numDimensions
	 * @input outType
	 * @output gaussKernelRAI
	 * @implNote op names='create, create.kernelGauss'
	 */
	public final Functions.Arity3<Double, Integer, C, RandomAccessibleInterval<C>> kernelGaussSymmetric = (sigma, numDims,
			type) -> {
		double[] sigmas = new double[numDims];
		Arrays.fill(sigmas, sigma);
		return kernelGauss.apply(sigmas, type);
	};

	// TODO is this cast safe?
	/**
	 * @input sigma
	 * @input numDimensions
	 * @output gaussKernelRAI
	 * @implNote op names='create, create.kernelGauss'
	 */
	public final BiFunction<Double, Integer, RandomAccessibleInterval<DoubleType>> kernelGaussSymmetricDoubleType = (
			sigma, numDims) -> (RandomAccessibleInterval<DoubleType>) kernelGaussSymmetric.apply(sigma, numDims,
					(C) new DoubleType());

	/* Kernel Log */

	/**
	 * @input sigmas
	 * @input outType
	 * @output logKernelRAI
	 * @implNote op names='create, create.kernelLog'
	 */
	public final BiFunction<double[], C, RandomAccessibleInterval<C>> kernelLog = (sigmas,
			type) -> DefaultCreateKernelLog.createKernel(sigmas, type, imgFromDimsAndType);

	/**
	 * @input sigmas
	 * @output logKernelRAI
	 * @implNote op names='create, create.kernelLog'
	 */
	public final Function<double[], RandomAccessibleInterval<DoubleType>> kernelLogDoubleType = (
			sigmas) -> (RandomAccessibleInterval<DoubleType>) kernelLog.apply(sigmas, (C) new DoubleType());

	/**
	 * @input sigma
	 * @input numDimensions
	 * @input outType
	 * @output logKernelRAI
	 * @implNote op names='create, create.kernelLog'
	 */
	public final Functions.Arity3<Double, Integer, C, RandomAccessibleInterval<C>> kernelLogSymmetric = (sigma, numDims,
			type) -> {
		double[] sigmas = new double[numDims];
		Arrays.fill(sigmas, sigma);
		return kernelLog.apply(sigmas, type);
	};

	/**
	 * @input sigma
	 * @input numDimensions
	 * @output logKernelRAI
	 * @implNote op names='create, create.kernelLog'
	 */
	public final BiFunction<Double, Integer, RandomAccessibleInterval<DoubleType>> kernelLogSymmetricDoubleType = (
			sigma, numDims) -> (RandomAccessibleInterval<DoubleType>) kernelLogSymmetric.apply(sigma, numDims,
					(C) new DoubleType());

	/* Kernel Diffraction */

	/**
	 * @input dimensions
	 * @input NA
	 * @input lambda
	 * @input ns
	 * @input ni
	 * @input resLateral
	 * @input resAxial
	 * @input pZ
	 * @input type
	 * @output diffractionKernelRAI
	 * @implNote op names='create, create.kernelDiffraction'
	 */
	public final Functions.Arity9<Dimensions, Double, Double, Double, Double, Double, Double, Double, W, Img<W>> kernelDiffraction = (
			dims, NA, lambda, ns, ni, resLateral, resAxial, pZ, type) -> DefaultCreateKernelGibsonLanni
					.createKernel(dims, NA, lambda, ns, ni, resLateral, resAxial, pZ, type, imgFromDimsAndType);

	/* Kernel BiGauss */

	/**
	 * @input sigmas
	 * @input numDimensions
	 * @input outType
	 * @output biGaussKernelRAI
	 * @implNote op names='create, create.kernelBiGauss'
	 */
	public final Functions.Arity3<double[], Integer, C, RandomAccessibleInterval<C>> kernelBiGauss = (sigmas, numDims,
			outType) -> DefaultCreateKernelBiGauss.createKernel(sigmas, numDims, outType, imgFromDimsAndType);

	/**
	 * @input sigmas
	 * @input numDimensions
	 * @output biGaussKernelRAI
	 * @implNote op names='create, create.kernelBiGauss'
	 */
	public final BiFunction<double[], Integer, RandomAccessibleInterval<DoubleType>> kernelBiGaussDoubleType = (sigmas,
			numDims) -> (RandomAccessibleInterval<DoubleType>) kernelBiGauss.apply(sigmas, numDims,
					(C) new DoubleType());

	/**
	 * @input sigmas
	 * @input numDims
	 * @input outType
	 * @output biGaussKernelRAI
	 * @implNote op names='create, create.kernel2ndDerivBiGauss'
	 */
	public final Functions.Arity3<double[], Integer, C, RandomAccessibleInterval<C>> kernel2ndDerivBiGauss = (sigmas, numDims,
			outType) -> DefaultCreateKernel2ndDerivBiGauss.createKernel(sigmas, numDims, outType, imgFromDimsAndType);

	/**
	 * @input sigmas
	 * @input numDims
	 * @output biGaussKernelRAI
	 * @implNote op names='create, create.kernel2ndDerivBiGauss'
	 */
	public final BiFunction<double[], Integer, RandomAccessibleInterval<DoubleType>> kernel2ndDerivBiGaussDoubleType = (
			sigmas, numDims) -> (RandomAccessibleInterval<DoubleType>) kernel2ndDerivBiGauss.apply(sigmas, numDims,
					(C) new DoubleType());

	/* Kernel Gabor */

	/**
	 * @input sigmas
	 * @input periods
	 * @input outType
	 * @output gaborKernelRAI
	 * @implNote op names='create, create.kernelGabor'
	 */
	public final Functions.Arity3<double[], double[], C, RandomAccessibleInterval<C>> kernelGabor = (sigmas, periods,
			outType) -> DefaultCreateKernelGabor.createKernel(sigmas, periods, outType, imgFromDimsAndType);

	/**
	 * @input sigmas
	 * @input periods
	 * @output gaborKernelRAI
	 * @implNote op names='create, create.kernelGabor'
	 */
	public final BiFunction<double[], double[], RandomAccessibleInterval<DoubleType>> kernelGaborDouble = (sigmas,
			periods) -> (RandomAccessibleInterval<DoubleType>) kernelGabor.apply(sigmas, periods, (C) new DoubleType());

	/**
	 * @input sigmas
	 * @input periods
	 * @output gaborKernelRAI
	 * @implNote op names='create, create.kernelGabor'
	 */
	public final BiFunction<double[], double[], RandomAccessibleInterval<FloatType>> kernelGaborFloat = (sigmas,
			periods) -> (RandomAccessibleInterval<FloatType>) kernelGabor.apply(sigmas, periods, (C) new FloatType());

	/**
	 * @input sigmas
	 * @input periods
	 * @output gaborKernelRAI
	 * @implNote op names='create, create.kernelGabor'
	 */
	public final BiFunction<double[], double[], RandomAccessibleInterval<ComplexDoubleType>> kernelGaborComplexDouble = (
			sigmas, periods) -> (RandomAccessibleInterval<ComplexDoubleType>) kernelGabor.apply(sigmas, periods,
					(C) new ComplexDoubleType());

	/**
	 * @input sigmas
	 * @input periods
	 * @output gaborKernelRAI
	 * @implNote op names='create, create.kernelGabor'
	 */
	public final BiFunction<double[], double[], RandomAccessibleInterval<ComplexFloatType>> kernelGaborComplexFloat = (
			sigmas, periods) -> (RandomAccessibleInterval<ComplexFloatType>) kernelGabor.apply(sigmas, periods,
					(C) new ComplexFloatType());

	/**
	 * @input sigmas
	 * @input periods
	 * @input outType
	 * @output gaborKernelRAI
	 * @implNote op names='create, create.kernelGabor'
	 */
	public final Functions.Arity3<Double, double[], C, RandomAccessibleInterval<C>> kernelGaborSingleSigma = (sigma, periods,
			outType) -> {
		double[] sigmas = new double[periods.length];
		Arrays.fill(sigmas, sigma);
		return DefaultCreateKernelGabor.createKernel(sigmas, periods, outType, imgFromDimsAndType);
	};

	/**
	 * @input sigmas
	 * @input periods
	 * @output gaborKernelRAI
	 * @implNote op names='create, create.kernelGabor'
	 */
	public final BiFunction<Double, double[], RandomAccessibleInterval<DoubleType>> kernelGaborDoubleSingleSigma = (
			sigma, periods) -> {
		double[] sigmas = new double[periods.length];
		Arrays.fill(sigmas, sigma);
		return (RandomAccessibleInterval<DoubleType>) kernelGabor.apply(sigmas, periods, (C) new DoubleType());
	};

	/**
	 * @input sigmas
	 * @input periods
	 * @output gaborKernelRAI
	 * @implNote op names='create, create.kernelGabor'
	 */
	public final BiFunction<Double, double[], RandomAccessibleInterval<FloatType>> kernelGaborFloatSingleSigma = (sigma,
			periods) -> {
		double[] sigmas = new double[periods.length];
		Arrays.fill(sigmas, sigma);
		return (RandomAccessibleInterval<FloatType>) kernelGabor.apply(sigmas, periods, (C) new FloatType());
	};

	/**
	 * @input sigmas
	 * @input periods
	 * @output gaborKernelRAI
	 * @implNote op names='create, create.kernelGabor'
	 */
	public final BiFunction<Double, double[], RandomAccessibleInterval<ComplexDoubleType>> kernelGaborComplexDoubleSingleSigma = (
			sigma, periods) -> {
		double[] sigmas = new double[periods.length];
		Arrays.fill(sigmas, sigma);
		return (RandomAccessibleInterval<ComplexDoubleType>) kernelGabor.apply(sigmas, periods,
				(C) new ComplexDoubleType());
	};

	/**
	 * @input sigmas
	 * @input periods
	 * @output gaborKernelRAI
	 * @implNote op names='create, create.kernelGabor'
	 */
	public final BiFunction<Double, double[], RandomAccessibleInterval<ComplexFloatType>> kernelGaborComplexFloatSingleSigma = (
			sigma, periods) -> {
		double[] sigmas = new double[periods.length];
		Arrays.fill(sigmas, sigma);
		return (RandomAccessibleInterval<ComplexFloatType>) kernelGabor.apply(sigmas, periods,
				(C) new ComplexFloatType());
	};

	/* Kernel Sobel */

	/**
	 * @input outType
	 * @output sobelKernelRAI
	 * @implNote op names='create, create.kernelSobel'
	 */
	public final Function<C, RandomAccessibleInterval<C>> kernelSobel = (outType) -> DefaultCreateKernelSobel
			.createKernel(outType, imgFromDimsAndType);

	/* Labeling Mapping */

	// NOTE: We are returning an empty LabelingMapping, and because it is empty that
	// L can be anything. So in this case it is safe to return an object with an
	// unbounded type variable because the caller has to restrict it in the
	// declaration.
	/**
	 * @output labelingMapping
	 * @implNote op names='create, create.labelingMapping'
	 */
	public final Producer<LabelingMapping<L>> labelingMappingSource = () -> new LabelingMapping<>(
			integerTypeSource.create());

	public final Function<Long, IntegerType> integerTypeFromLong = (maxValue) -> {
		if (maxValue <= 0L)
			return new IntType();
		if (maxValue <= 1L)
			return new BitType();
		if (maxValue <= 0x7fL)
			return new ByteType();
		if (maxValue <= 0xffL)
			return new UnsignedByteType();
		if (maxValue <= 0x7fffL)
			return new ShortType();
		if (maxValue <= 0xffffL)
			return new UnsignedShortType();
		if (maxValue <= 0x7fffffffL)
			return new IntType();
		if (maxValue <= 0xffffffffL)
			return new UnsignedIntType();
		return new LongType();
	};

	/**
	 * @input maxNumSets
	 * @output labelingMapping
	 * @implNote op names='create, create.labelingMapping'
	 */
	public final Function<Integer, LabelingMapping<L>> labelingMapping = (maxNumSets) -> new LabelingMapping<>(
			integerTypeFromLong.apply(maxNumSets.longValue()));

	/* Object */

	/**
	 * @input class
	 * @output object
	 * @implNote op names='create, create.object'
	 */
	public final Function<Class<L>, L> object = (clazz) -> {
		try {
			return clazz.newInstance();
		} catch (final InstantiationException exc) {
			throw new IllegalArgumentException(exc);
		} catch (final IllegalAccessException exc) {
			throw new IllegalArgumentException(exc);
		}
	};

	/* NativeType */

	/**
	 * @output nativeType
	 * @implNote op names='create, create.nativeType', priority='100.'
	 */
	public final Producer<DoubleType> defaultNativeType = () -> new DoubleType();

	// TODO is this a safe cast?
	/**
	 * @input type
	 * @output nativeType
	 * @implNote op names='create, create.nativeType'
	 */
	public final Function<Class<N>, N> nativeTypeFromClass = (clazz) -> (N) object.apply((Class<L>) clazz);

	/**
	 * @output vector3d
	 * @implNote op names='create, create.vector'
	 */
	public final Producer<Vector3d> defaultVector3d = () -> new Vector3d();

	/**
	 * @output vector3f
	 * @implNote op names='create, create.vector'
	 */
	public final Producer<Vector3f> defaultVector3f = () -> new Vector3f();
}
