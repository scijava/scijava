
package org.scijava.ops.image.filter.convolve;

import net.imglib2.*;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

import java.util.function.BiFunction;

/**
 * A collection of convolution Ops that operate purely in the physical (i.e. not
 * Fourier) domain.
 *
 * @author Brian Northan
 * @author Gabriel Selzer
 */
public class NaiveConvolutions {

	/**
	 * A simple implementation of convolution that operates purely in the physical
	 * domain.
	 *
	 * @param creator a Op that can create an output image
	 * @param input the input data
	 * @param kernel the kernel
	 * @param outOfBoundsFactory used to fill in out-of-bounds indices on the
	 *          input data
	 * @return the convolution of {@code input} and {@code kernel}
	 * @implNote op names="filter.convolve, filter.convolveNaive"
	 */
	public static <I extends RealType<I>, K extends RealType<K>>
		RandomAccessibleInterval<FloatType> convolveNaiveToFloats( //
			@OpDependency(
				name = "create.img") BiFunction<RandomAccessibleInterval<I>, FloatType, RandomAccessibleInterval<FloatType>> creator, //
			final RandomAccessibleInterval<I> input, //
			final RandomAccessibleInterval<K> kernel, //
			@Nullable OutOfBoundsFactory<I, RandomAccessibleInterval<I>> outOfBoundsFactory //
	) {
		return convolveNaive(creator, input, kernel, new FloatType(),
			outOfBoundsFactory);
	}

	/**
	 * A simple implementation of convolution that operates purely in the physical
	 * domain.
	 *
	 * @param creator a Op that can create an output image
	 * @param input the input data
	 * @param kernel the kernel
	 * @param outType the expected element type
	 * @param outOfBoundsFactory used to fill in out-of-bounds indices on the
	 *          input data
	 * @return the convolution of {@code input} and {@code kernel}
	 * @implNote op names="filter.convolve, filter.convolveNaive"
	 */
	public static <I extends RealType<I>, K extends RealType<K>, O extends RealType<O>>
		RandomAccessibleInterval<O> convolveNaive( //
			@OpDependency(
				name = "create.img") BiFunction<RandomAccessibleInterval<I>, O, RandomAccessibleInterval<O>> creator, //
			final RandomAccessibleInterval<I> input, //
			final RandomAccessibleInterval<K> kernel, //
			final O outType,
			@Nullable OutOfBoundsFactory<I, RandomAccessibleInterval<I>> outOfBoundsFactory //
	) {
		// create the output data structure
		var outImg = creator.apply(input, outType);

		// create the out of bounds factory
		if (outOfBoundsFactory == null) {
			var type = Util.getTypeFromInterval(input);
			outOfBoundsFactory = new OutOfBoundsConstantValueFactory<>(type
				.createVariable());
		}
		// convolve
		convolveNaive(Views.extend(input, outOfBoundsFactory), kernel, outImg);
		return outImg;
	}

	/**
	 * A simple implementation of convolution that operates purely in the physical
	 * domain.
	 *
	 * @param input the input data
	 * @param kernel the kernel
	 * @param output a pre-allocated output buffer
	 * @implNote op names="filter.convolve, filter.convolveNaive" type=Computer
	 */
	public static <I extends RealType<I>, K extends RealType<K>, O extends RealType<O>>
		void convolveNaive( //
			final RandomAccessible<I> input, //
			final RandomAccessibleInterval<K> kernel, //
			final RandomAccessibleInterval<O> output //
	) {
		// TODO: try a decomposition of the kernel into n 1-dim kernels
		final long[] min = new long[input.numDimensions()];
		final long[] max = new long[input.numDimensions()];

		for (int d = 0; d < kernel.numDimensions(); d++) {
			min[d] = -kernel.dimension(d);
			max[d] = kernel.dimension(d) + output.dimension(d);
		}

		final RandomAccess<I> inRA = input.randomAccess(new FinalInterval(min,
			max));

		final Cursor<K> kernelC = Views.iterable(kernel).localizingCursor();

		final Cursor<O> outC = Views.iterable(output).localizingCursor();

		final long[] pos = new long[input.numDimensions()];
		final long[] kernelRadius = new long[kernel.numDimensions()];
		for (int i = 0; i < kernelRadius.length; i++) {
			kernelRadius[i] = kernel.dimension(i) / 2;
		}

		float val;

		while (outC.hasNext()) {
			// image
			outC.fwd();
			outC.localize(pos);

			// kernel inlined version of the method convolve
			val = 0;
			inRA.setPosition(pos);

			kernelC.reset();
			while (kernelC.hasNext()) {
				kernelC.fwd();

				for (int i = 0; i < kernelRadius.length; i++) {
					// dimension can have zero extension e.g. vertical 1d kernel
					if (kernelRadius[i] > 0) {
						inRA.setPosition(pos[i] + kernelC.getLongPosition(i) -
							kernelRadius[i], i);
					}
				}

				val += inRA.get().getRealFloat() * kernelC.get().getRealFloat();
			}

			outC.get().setReal(val);
		}
	}

}
