package net.imagej.ops.create;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.imagej.ImgPlus;
import net.imagej.ImgPlusMetadata;
import net.imagej.ops.create.img.Imgs;
import net.imagej.ops.create.kernel.DefaultCreateKernel2ndDerivBiGauss;
import net.imagej.ops.create.kernel.DefaultCreateKernelBiGauss;
import net.imagej.ops.create.kernel.DefaultCreateKernelGabor;
import net.imagej.ops.create.kernel.DefaultCreateKernelGauss;
import net.imagej.ops.create.kernel.DefaultCreateKernelGibsonLanni;
import net.imagej.ops.create.kernel.DefaultCreateKernelLog;
import net.imagej.ops.create.kernel.DefaultCreateKernelSobel;
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
import org.scijava.core.Priority;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.function.Function3;
import org.scijava.ops.core.function.Function9;
import org.scijava.ops.core.function.Source;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class Creators<N extends NativeType<N>, L, I extends IntegerType<I>, T extends Type<T>, C extends ComplexType<C>, W extends ComplexType<W> & NativeType<W>, B extends BooleanType<B>> {

	/* ImgFactories */

	@OpField(names = "create, create.imgFactory")
	@Parameter(key = "imgFactory", itemIO = ItemIO.OUTPUT)
	public final Source<ImgFactory<DoubleType>> factorySource = () -> new ArrayImgFactory(new DoubleType());

	// note that dims is not actually passed to the ImgFactory but instead is
	// inspected to determine which will be returned.
	@OpField(names = "create, create.imgFactory")
	@Parameter(key = "dimensions")
	@Parameter(key = "imgFactory", itemIO = ItemIO.OUTPUT)
	public final Function<Dimensions, ImgFactory<DoubleType>> factoryFromDims = (dims) -> Util
			.getSuitableImgFactory(dims, new DoubleType());

	@OpField(names = "create, create.imgFactory")
	@Parameter(key = "dimensions")
	@Parameter(key = "type")
	@Parameter(key = "imgFactory", itemIO = ItemIO.OUTPUT)
	public final BiFunction<Dimensions, L, ImgFactory<L>> factoryFromDimsAndType = Util::getSuitableImgFactory;

	@OpField(names = "create, create.imgFactory")
	@Parameter(key = "img")
	@Parameter(key = "factory", itemIO = ItemIO.OUTPUT)
	public final Function<Img<L>, ImgFactory<L>> factoryFromImg = (img) -> img.factory();

	/* Imgs */

	@OpField(names = "create, create.img")
	@Parameter(key = "dimensions")
	@Parameter(key = "type")
	@Parameter(key = "factory")
	@Parameter(key = "img", itemIO = ItemIO.OUTPUT)
	public final Function3<Dimensions, T, ImgFactory<T>, Img<T>> imgFromDimsTypeAndFactory = (dims, type,
			factory) -> Imgs.create(factory, dims, type);

	@OpField(names = "create, create.img")
	@Parameter(key = "dimensions")
	@Parameter(key = "type")
	@Parameter(key = "img", itemIO = ItemIO.OUTPUT)
	public final BiFunction<Dimensions, T, Img<T>> imgFromDimsAndType = (dims, type) -> {
		ImgFactory<T> factory = dims instanceof Img<?> ? ((Img<T>) dims).factory()
				: Util.getSuitableImgFactory(dims, type);
		return Imgs.create(factory, dims, type);
	};

	@OpField(names = "create, create.img")
	@Parameter(key = "intArray")
	@Parameter(key = "img", itemIO = ItemIO.OUTPUT)
	public final Function<int[], Img<DoubleType>> imgFromIntArray = (array) -> {
		FinalDimensions dims = new FinalDimensions(array);
		DoubleType type = new DoubleType();
		return Imgs.create(Util.getSuitableImgFactory(dims, type), dims, type);
	};

	@OpField(names = "create, create.img")
	@Parameter(key = "integerArray")
	@Parameter(key = "img", itemIO = ItemIO.OUTPUT)
	public final Function<Integer[], Img<DoubleType>> imgFromIntegerArray = (array) -> imgFromIntArray
			.apply(Arrays.stream(array).mapToInt(Integer::intValue).toArray());

	@OpField(names = "create, create.img")
	@Parameter(key = "longArray")
	@Parameter(key = "img", itemIO = ItemIO.OUTPUT)
	public final Function<long[], Img<DoubleType>> imgFromPrimitiveLongArray = (array) -> {
		FinalDimensions dims = new FinalDimensions(array);
		DoubleType type = new DoubleType();
		return Imgs.create(Util.getSuitableImgFactory(dims, type), dims, type);
	};

	@OpField(names = "create, create.img")
	@Parameter(key = "longArray")
	@Parameter(key = "img", itemIO = ItemIO.OUTPUT)
	public final Function<Long[], Img<DoubleType>> imgFromLongArray = (array) -> imgFromPrimitiveLongArray
			.apply(Arrays.stream(array).mapToLong(Long::longValue).toArray());

	@OpField(names = "create, create.img", priority = Priority.NORMAL)
	@Parameter(key = "iterableInterval")
	@Parameter(key = "img", itemIO = ItemIO.OUTPUT)
	public final Function<IterableInterval<T>, Img<T>> imgFromII = (ii) -> imgFromDimsAndType.apply(ii,
			ii.firstElement());

	@OpField(names = "create, create.img", priority = Priority.HIGH)
	@Parameter(key = "inputImg")
	@Parameter(key = "img", itemIO = ItemIO.OUTPUT)
	public final Function<Img<T>, Img<T>> imgFromImg = (img) -> Imgs.create(img.factory(), img, img.firstElement());

	@OpField(names = "create, create.img", priority = Priority.LOW)
	@Parameter(key = "interval")
	@Parameter(key = "img", itemIO = ItemIO.OUTPUT)
	public final Function<Interval, Img<DoubleType>> imgFromInterval = (interval) -> {
		DoubleType type = new DoubleType();
		return Imgs.create(Util.getSuitableImgFactory(interval, type), interval, type);
	};

	@OpField(names = "create, create.img", priority = Priority.NORMAL)
	@Parameter(key = "rai")
	@Parameter(key = "img", itemIO = ItemIO.OUTPUT)
	public final Function<RandomAccessibleInterval<T>, Img<T>> imgFromRAI = (rai) -> imgFromDimsAndType.apply(rai,
			Util.getTypeFromInterval(rai));

	/* IntegerType */

	@OpField(names = "create, create.integerType", priority = Priority.NORMAL)
	@Parameter(key = "integerType", itemIO = ItemIO.OUTPUT)
	public final Source<LongType> integerTypeSource = () -> new LongType();
	
	/* Type */

	@OpField(names = "create, create.type")
	@Parameter(key = "sampleType")
	@Parameter(key = "type", itemIO = ItemIO.OUTPUT)
	public final Function<T, T> typeFromSampleType = (sample) -> sample.createVariable();
	
	@OpField(names = "create, create.type", priority = Priority.LOW)
	@Parameter(key = "booleanType", itemIO = ItemIO.OUTPUT)
	public final Source<BitType> booleanTypeSource = () -> new BitType();

	/* ImgLabeling */

	@OpField(names = "create, create.imgLabeling")
	@Parameter(key = "img")
	@Parameter(key = "imgLabeling", itemIO = ItemIO.OUTPUT)
	public final Function<Img<I>, ImgLabeling<L, I>> imgLabelingFromImg = ImgLabeling::new;

	@OpField(names = "create, create.imgLabeling")
	@Parameter(key = "dimensions")
	@Parameter(key = "type")
	@Parameter(key = "factory")
	@Parameter(key = "imgLabeling", itemIO = ItemIO.OUTPUT)
	public final Function3<Dimensions, I, ImgFactory<I>, ImgLabeling<L, I>> imgLabelingFromDimsTypeAndFactory = (dims,
			type, factory) -> {
		Img<I> img = Imgs.create(factory, dims, type);
		return imgLabelingFromImg.apply(img);
	};

	@OpField(names = "create, create.imgLabeling")
	@Parameter(key = "dimensions")
	@Parameter(key = "type")
	@Parameter(key = "imgLabeling", itemIO = ItemIO.OUTPUT)
	public final BiFunction<Dimensions, I, ImgLabeling<L, I>> imgLabelingFromDimsAndType = (dims,
			type) -> imgLabelingFromDimsTypeAndFactory.apply(dims, type, Util.getSuitableImgFactory(dims, type));

	/* ImgPlus */

	@OpField(names = "create, create.imgPlus")
	@Parameter(key = "img")
	@Parameter(key = "imgPlus", itemIO = ItemIO.OUTPUT)
	public final Function<Img<T>, ImgPlus<T>> imgPlusFromImg = ImgPlus::new;

	@OpField(names = "create, create.imgPlus")
	@Parameter(key = "img")
	@Parameter(key = "imgPlusMetadata")
	@Parameter(key = "imgPlus", itemIO = ItemIO.OUTPUT)
	public final BiFunction<Img<T>, ImgPlusMetadata, ImgPlus<T>> imgPlusFromImgAndMetadata = ImgPlus::new;

	/* Kernel */

	@OpField(names = "create, create.kernel")
	@Parameter(key = "values")
	@Parameter(key = "type")
	@Parameter(key = "kernelRAI", itemIO = ItemIO.OUTPUT)
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
	@OpField(names = "create, create.kernel")
	@Parameter(key = "values")
	@Parameter(key = "kernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function<double[][], RandomAccessibleInterval<DoubleType>> kernel2DFromValues = (
			arr) -> (RandomAccessibleInterval<DoubleType>) kernel2DFromValuesAndType.apply(arr, (C) new DoubleType());

	/* Gaussian Kernel */

	@OpField(names = "create, create.kernelGauss")
	@Parameter(key = "numDims")
	@Parameter(key = "type")
	@Parameter(key = "gaussKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<double[], C, RandomAccessibleInterval<C>> kernelGauss = (numDims, type) -> {
		return DefaultCreateKernelGauss.createKernel(numDims, type, imgFromDimsAndType);
	};

	// TODO do we want to support this and if so is this the right way to do it?
	@OpField(names = "create, create.kernelGauss")
	@Parameter(key = "sigmas")
	@Parameter(key = "gaussKernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function<double[], RandomAccessibleInterval<DoubleType>> kernelGaussDoubleType = (
			sigmas) -> (RandomAccessibleInterval<DoubleType>) kernelGauss.apply(sigmas, (C) new DoubleType());

	@OpField(names = "create, create.kernelGauss")
	@Parameter(key = "sigma")
	@Parameter(key = "numDimensions")
	@Parameter(key = "outType")
	@Parameter(key = "gaussKernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function3<Double, Integer, C, RandomAccessibleInterval<C>> kernelGaussSymmetric = (sigma, numDims,
			type) -> {
		double[] sigmas = new double[numDims];
		Arrays.fill(sigmas, sigma);
		return kernelGauss.apply(sigmas, type);
	};

	// TODO is this cast safe?
	@OpField(names = "create, create.kernelGauss")
	@Parameter(key = "sigma")
	@Parameter(key = "numDimensions")
	@Parameter(key = "gaussKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<Double, Integer, RandomAccessibleInterval<DoubleType>> kernelGaussSymmetricDoubleType = (
			sigma, numDims) -> (RandomAccessibleInterval<DoubleType>) kernelGaussSymmetric.apply(sigma, numDims,
					(C) new DoubleType());

	/* Kernel Log */

	@OpField(names = "create, create.kernelLog")
	@Parameter(key = "sigmas")
	@Parameter(key = "outType")
	@Parameter(key = "logKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<double[], C, RandomAccessibleInterval<C>> kernelLog = (sigmas,
			type) -> DefaultCreateKernelLog.createKernel(sigmas, type, imgFromDimsAndType);

	@OpField(names = "create, create.kernelLog")
	@Parameter(key = "sigmas")
	@Parameter(key = "logKernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function<double[], RandomAccessibleInterval<DoubleType>> kernelLogDoubleType = (
			sigmas) -> (RandomAccessibleInterval<DoubleType>) kernelLog.apply(sigmas, (C) new DoubleType());

	@OpField(names = "create, create.kernelLog")
	@Parameter(key = "sigma")
	@Parameter(key = "numDimensions")
	@Parameter(key = "outType")
	@Parameter(key = "logKernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function3<Double, Integer, C, RandomAccessibleInterval<C>> kernelLogSymmetric = (sigma, numDims,
			type) -> {
		double[] sigmas = new double[numDims];
		Arrays.fill(sigmas, sigma);
		return kernelLog.apply(sigmas, type);
	};

	@OpField(names = "create, create.kernelLog")
	@Parameter(key = "sigma")
	@Parameter(key = "numDimensions")
	@Parameter(key = "logKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<Double, Integer, RandomAccessibleInterval<DoubleType>> kernelLogSymmetricDoubleType = (
			sigma, numDims) -> (RandomAccessibleInterval<DoubleType>) kernelLogSymmetric.apply(sigma, numDims,
					(C) new DoubleType());

	/* Kernel Diffraction */

	@OpField(names = "create, create.kernelDiffraction")
	@Parameter(key = "dimensions")
	@Parameter(key = "NA")
	@Parameter(key = "lambda")	
	@Parameter(key = "ns")
	@Parameter(key = "ni")
	@Parameter(key = "resLateral")
	@Parameter(key = "resAxial")
	@Parameter(key = "pZ")
	@Parameter(key = "type")
	@Parameter(key = "diffractionKernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function9<Dimensions, Double, Double, Double, Double, Double, Double, Double, W, Img<W>> kernelDiffraction = 
	(dims, NA, lambda, ns, ni, resLateral, resAxial, pZ, type) -> DefaultCreateKernelGibsonLanni.createKernel(dims, NA, lambda, ns, ni, resLateral, resAxial, pZ, type, imgFromDimsAndType);

	/* Kernel BiGauss */

	@OpField(names = "create, create.kernelBiGauss")
	@Parameter(key = "sigmas")
	@Parameter(key = "numDimensions")
	@Parameter(key = "outType")
	@Parameter(key = "biGaussKernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function3<double[], Integer, C, RandomAccessibleInterval<C>> kernelBiGauss = (sigmas, numDims,
			outType) -> DefaultCreateKernelBiGauss.createKernel(sigmas, numDims, outType, imgFromDimsAndType);

	@OpField(names = "create, create.kernelBiGauss")
	@Parameter(key = "sigmas")
	@Parameter(key = "numDimensions")
	@Parameter(key = "biGaussKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<double[], Integer, RandomAccessibleInterval<DoubleType>> kernelBiGaussDoubleType = (sigmas,
			numDims) -> (RandomAccessibleInterval<DoubleType>) kernelBiGauss.apply(sigmas, numDims,
					(C) new DoubleType());

	@OpField(names = "create, create.kernel2ndDerivBiGauss")
	@Parameter(key = "sigmas")
	@Parameter(key = "numDims")
	@Parameter(key = "outType")
	@Parameter(key = "biGaussKernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function3<double[], Integer, C, RandomAccessibleInterval<C>> kernel2ndDerivBiGauss = (sigmas, numDims,
			outType) -> DefaultCreateKernel2ndDerivBiGauss.createKernel(sigmas, numDims, outType, imgFromDimsAndType);

	@OpField(names = "create, create.kernel2ndDerivBiGauss")
	@Parameter(key = "sigmas")
	@Parameter(key = "numDimensions")
	@Parameter(key = "biGaussKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<double[], Integer, RandomAccessibleInterval<DoubleType>> kernel2ndDerivBiGaussDoubleType = (
			sigmas, numDims) -> (RandomAccessibleInterval<DoubleType>) kernel2ndDerivBiGauss.apply(sigmas, numDims,
					(C) new DoubleType());

	/* Kernel Gabor */

	@OpField(names = "create, create.kernelGabor")
	@Parameter(key = "sigmas")
	@Parameter(key = "periods")
	@Parameter(key = "outType")
	@Parameter(key = "gaborKernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function3<double[], double[], C, RandomAccessibleInterval<C>> kernelGabor = (sigmas, periods,
			outType) -> DefaultCreateKernelGabor.createKernel(sigmas, periods, outType, imgFromDimsAndType);

	@OpField(names = "create, create.kernelGabor")
	@Parameter(key = "sigmas")
	@Parameter(key = "periods")
	@Parameter(key = "gaborKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<double[], double[], RandomAccessibleInterval<DoubleType>> kernelGaborDouble = (sigmas,
			periods) -> (RandomAccessibleInterval<DoubleType>) kernelGabor.apply(sigmas, periods, (C) new DoubleType());

	@OpField(names = "create, create.kernelGabor")
	@Parameter(key = "sigmas")
	@Parameter(key = "periods")
	@Parameter(key = "gaborKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<double[], double[], RandomAccessibleInterval<FloatType>> kernelGaborFloat = (sigmas,
			periods) -> (RandomAccessibleInterval<FloatType>) kernelGabor.apply(sigmas, periods, (C) new FloatType());

	@OpField(names = "create, create.kernelGabor")
	@Parameter(key = "sigmas")
	@Parameter(key = "periods")
	@Parameter(key = "gaborKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<double[], double[], RandomAccessibleInterval<ComplexDoubleType>> kernelGaborComplexDouble = (
			sigmas, periods) -> (RandomAccessibleInterval<ComplexDoubleType>) kernelGabor.apply(sigmas, periods,
					(C) new ComplexDoubleType());

	@OpField(names = "create, create.kernelGabor")
	@Parameter(key = "sigmas")
	@Parameter(key = "periods")
	@Parameter(key = "gaborKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<double[], double[], RandomAccessibleInterval<ComplexFloatType>> kernelGaborComplexFloat = (
			sigmas, periods) -> (RandomAccessibleInterval<ComplexFloatType>) kernelGabor.apply(sigmas, periods,
					(C) new ComplexFloatType());

	@OpField(names = "create, create.kernelGabor")
	@Parameter(key = "sigmas")
	@Parameter(key = "periods")
	@Parameter(key = "outType")
	@Parameter(key = "gaborKernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function3<Double, double[], C, RandomAccessibleInterval<C>> kernelGaborSingleSigma = (sigma, periods,
			outType) -> {
		double[] sigmas = new double[periods.length];
		Arrays.fill(sigmas, sigma);
		return DefaultCreateKernelGabor.createKernel(sigmas, periods, outType, imgFromDimsAndType);
	};

	@OpField(names = "create, create.kernelGabor")
	@Parameter(key = "sigmas")
	@Parameter(key = "periods")
	@Parameter(key = "gaborKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<Double, double[], RandomAccessibleInterval<DoubleType>> kernelGaborDoubleSingleSigma = (
			sigma, periods) -> {
		double[] sigmas = new double[periods.length];
		Arrays.fill(sigmas, sigma);
		return (RandomAccessibleInterval<DoubleType>) kernelGabor.apply(sigmas, periods, (C) new DoubleType());
	};

	@OpField(names = "create, create.kernelGabor")
	@Parameter(key = "sigmas")
	@Parameter(key = "periods")
	@Parameter(key = "gaborKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<Double, double[], RandomAccessibleInterval<FloatType>> kernelGaborFloatSingleSigma = (sigma,
			periods) -> {
		double[] sigmas = new double[periods.length];
		Arrays.fill(sigmas, sigma);
		return (RandomAccessibleInterval<FloatType>) kernelGabor.apply(sigmas, periods, (C) new FloatType());
	};

	@OpField(names = "create, create.kernelGabor")
	@Parameter(key = "sigmas")
	@Parameter(key = "periods")
	@Parameter(key = "gaborKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<Double, double[], RandomAccessibleInterval<ComplexDoubleType>> kernelGaborComplexDoubleSingleSigma = (
			sigma, periods) -> {
		double[] sigmas = new double[periods.length];
		Arrays.fill(sigmas, sigma);
		return (RandomAccessibleInterval<ComplexDoubleType>) kernelGabor.apply(sigmas, periods,
				(C) new ComplexDoubleType());
	};

	@OpField(names = "create, create.kernelGabor")
	@Parameter(key = "sigmas")
	@Parameter(key = "periods")
	@Parameter(key = "gaborKernelRAI", itemIO = ItemIO.OUTPUT)
	public final BiFunction<Double, double[], RandomAccessibleInterval<ComplexFloatType>> kernelGaborComplexFloatSingleSigma = (
			sigma, periods) -> {
		double[] sigmas = new double[periods.length];
		Arrays.fill(sigmas, sigma);
		return (RandomAccessibleInterval<ComplexFloatType>) kernelGabor.apply(sigmas, periods,
				(C) new ComplexFloatType());
	};

	/* Kernel Sobel */

	@OpField(names = "create, create.kernelSobel")
	@Parameter(key = "outType")
	@Parameter(key = "sobelKernelRAI", itemIO = ItemIO.OUTPUT)
	public final Function<C, RandomAccessibleInterval<C>> kernelSobel = (outType) -> DefaultCreateKernelSobel
			.createKernel(outType, imgFromDimsAndType);

	/* Labeling Mapping */

	// NOTE: We are returning an empty LabelingMapping, and because it is empty that
	// L can be anything. So in this case it is safe to return an object with an
	// unbounded type variable because the caller has to restrict it in the
	// declaration.
	@OpField(names = "create, create.labelingMapping")
	@Parameter(key = "labelingMapping", itemIO = ItemIO.OUTPUT)
	public final Source<LabelingMapping<L>> labelingMappingSource = () -> new LabelingMapping<>(
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

	@OpField(names = "create, create.labelingMapping")
	@Parameter(key = "maxNumSets")
	@Parameter(key = "labelingMapping", itemIO = ItemIO.OUTPUT)
	public final Function<Integer, LabelingMapping<L>> labelingMapping = (maxNumSets) -> new LabelingMapping<>(
			integerTypeFromLong.apply(maxNumSets.longValue()));

	/* Object */

	@OpField(names = "create, create.object")
	@Parameter(key = "class")
	@Parameter(key = "object", itemIO = ItemIO.OUTPUT)
	public final Function<Class<L>, L> object=(clazz)->{try{return clazz.newInstance();}catch(
	final InstantiationException exc)
	{
		throw new IllegalArgumentException(exc);
	}catch(
	final IllegalAccessException exc)
	{
		throw new IllegalArgumentException(exc);
	}};

	/* NativeType */

	@OpField(names = "create, create.nativeType", priority = Priority.HIGH)
	@Parameter(key = "nativeType", itemIO = ItemIO.OUTPUT)
	public final Source<DoubleType> defaultNativeType = () -> new DoubleType();

	// TODO is this a safe cast?
	@OpField(names = "create, create.nativeType")
	@Parameter(key = "type")
	@Parameter(key = "nativeType", itemIO = ItemIO.OUTPUT)
	public final Function<Class<N>, N> nativeTypeFromClass = (clazz) -> (N) object.apply((Class<L>) clazz);

	@OpField(names = "create, create.vector")
	@Parameter(key = "vector3d", itemIO = ItemIO.OUTPUT)
	public final Source<Vector3d> defaultVector3d = () -> new Vector3d();

	@OpField(names = "create, create.vector")
	@Parameter(key = "vector3f", itemIO = ItemIO.OUTPUT)
	public final Source<Vector3f> defaultVector3f = () -> new Vector3f();
}
