/* #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.morphology.gradient;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Morphological gradients and Laplacian of grayscale images, in any number of
 * dimensions, combining the dilation and erosion by a structuring element.
 * <p>
 * With {@code dil} and {@code ero} the dilation and erosion of the input
 * {@code f} by the given {@link Shape}, the ops compute:
 * </p>
 * <ul>
 * <li>{@code morphology.gradient}: {@code dil - ero} (the Beucher
 * gradient),</li>
 * <li>{@code morphology.internalGradient}: {@code f - ero},</li>
 * <li>{@code morphology.externalGradient}: {@code dil - f},</li>
 * <li>{@code morphology.laplacian}: {@code dil + ero - 2 f}, i.e. the
 * external minus the internal gradient. It is negative on the bright side of
 * edges, so use a signed or floating-point output type.</li>
 * </ul>
 * <p>
 * Pixels outside the image are ignored by the dilation and erosion. The
 * arithmetic is done in double precision and written to the output with
 * {@code setReal}.
 * </p>
 * <p>
 * See P. Soille, <em>Morphological Image Analysis</em>, 2nd ed., Springer,
 * 2003, and L. J. van Vliet, I. T. Young and G. L. Beckers, "A nonlinear
 * Laplace operator as edge detector in noisy images", <i>Computer Vision,
 * Graphics, and Image Processing</i> 45(2): 167&ndash;195, 1989. These ops correspond to the
 * gradient and Laplacian filters of MorphoLibJ, whose Laplacian is half of
 * the one computed here.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class MorphologicalGradients {

	private MorphologicalGradients() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the morphological (Beucher) gradient: the dilation minus the
	 * erosion.
	 *
	 * @param createOp an op to create temporary images
	 * @param dilateOp the dilation op
	 * @param erodeOp the erosion op
	 * @param input the image to process
	 * @param shape the structuring element, such as a {@code RectangleShape} or
	 *          {@code HyperSphereShape}
	 * @param output the gradient, of the same dimensions as {@code input}
	 * @param <T> input type
	 * @param <R> output type
	 * @implNote op names='morphology.gradient', type=Computer
	 */
	public static <T extends RealType<T>, R extends RealType<R>> void gradient(
		@OpDependency(
			name = "create.img") BiFunction<Dimensions, T, Img<T>> createOp,
		@OpDependency(
			name = "morphology.dilate") Computers.Arity3<RandomAccessible<T>, Shape, Integer, IterableInterval<T>> dilateOp,
		@OpDependency(
			name = "morphology.erode") Computers.Arity3<RandomAccessible<T>, Shape, Integer, IterableInterval<T>> erodeOp,
		final RandomAccessibleInterval<T> input, final Shape shape,
		final RandomAccessibleInterval<R> output)
	{
		check(input, output);
		final var dil = dilate(createOp, dilateOp, input, shape);
		final var ero = erode(createOp, erodeOp, input, shape);
		LoopBuilder.setImages(dil, ero, output).multiThreaded().forEachPixel((d,
			e, o) -> o.setReal(d.getRealDouble() - e.getRealDouble()));
	}

	/**
	 * Computes the internal morphological gradient: the image minus its
	 * erosion.
	 *
	 * @param createOp an op to create temporary images
	 * @param erodeOp the erosion op
	 * @param input the image to process
	 * @param shape the structuring element, such as a {@code RectangleShape} or
	 *          {@code HyperSphereShape}
	 * @param output the gradient, of the same dimensions as {@code input}
	 * @param <T> input type
	 * @param <R> output type
	 * @implNote op names='morphology.internalGradient', type=Computer
	 */
	public static <T extends RealType<T>, R extends RealType<R>> void
		internalGradient(
			@OpDependency(
				name = "create.img") BiFunction<Dimensions, T, Img<T>> createOp,
			@OpDependency(
				name = "morphology.erode") Computers.Arity3<RandomAccessible<T>, Shape, Integer, IterableInterval<T>> erodeOp,
			final RandomAccessibleInterval<T> input, final Shape shape,
			final RandomAccessibleInterval<R> output)
	{
		check(input, output);
		final var ero = erode(createOp, erodeOp, input, shape);
		LoopBuilder.setImages(input, ero, output).multiThreaded().forEachPixel((f,
			e, o) -> o.setReal(f.getRealDouble() - e.getRealDouble()));
	}

	/**
	 * Computes the external morphological gradient: the dilation minus the
	 * image.
	 *
	 * @param createOp an op to create temporary images
	 * @param dilateOp the dilation op
	 * @param input the image to process
	 * @param shape the structuring element, such as a {@code RectangleShape} or
	 *          {@code HyperSphereShape}
	 * @param output the gradient, of the same dimensions as {@code input}
	 * @param <T> input type
	 * @param <R> output type
	 * @implNote op names='morphology.externalGradient', type=Computer
	 */
	public static <T extends RealType<T>, R extends RealType<R>> void
		externalGradient(
			@OpDependency(
				name = "create.img") BiFunction<Dimensions, T, Img<T>> createOp,
			@OpDependency(
				name = "morphology.dilate") Computers.Arity3<RandomAccessible<T>, Shape, Integer, IterableInterval<T>> dilateOp,
			final RandomAccessibleInterval<T> input, final Shape shape,
			final RandomAccessibleInterval<R> output)
	{
		check(input, output);
		final var dil = dilate(createOp, dilateOp, input, shape);
		LoopBuilder.setImages(input, dil, output).multiThreaded().forEachPixel((f,
			d, o) -> o.setReal(d.getRealDouble() - f.getRealDouble()));
	}

	/**
	 * Computes the morphological Laplacian: the dilation plus the erosion minus
	 * twice the image, i.e. the external minus the internal gradient.
	 *
	 * @param createOp an op to create temporary images
	 * @param dilateOp the dilation op
	 * @param erodeOp the erosion op
	 * @param input the image to process
	 * @param shape the structuring element, such as a {@code RectangleShape} or
	 *          {@code HyperSphereShape}
	 * @param output the Laplacian, of the same dimensions as {@code input};
	 *          should be of a signed type since the Laplacian can be negative
	 * @param <T> input type
	 * @param <R> output type
	 * @implNote op names='morphology.laplacian', type=Computer
	 */
	public static <T extends RealType<T>, R extends RealType<R>> void laplacian(
		@OpDependency(
			name = "create.img") BiFunction<Dimensions, T, Img<T>> createOp,
		@OpDependency(
			name = "morphology.dilate") Computers.Arity3<RandomAccessible<T>, Shape, Integer, IterableInterval<T>> dilateOp,
		@OpDependency(
			name = "morphology.erode") Computers.Arity3<RandomAccessible<T>, Shape, Integer, IterableInterval<T>> erodeOp,
		final RandomAccessibleInterval<T> input, final Shape shape,
		final RandomAccessibleInterval<R> output)
	{
		check(input, output);
		final var dil = dilate(createOp, dilateOp, input, shape);
		final var ero = erode(createOp, erodeOp, input, shape);
		LoopBuilder.setImages(input, dil, ero, output).multiThreaded()
			.forEachPixel((f, d, e, o) -> o.setReal(d.getRealDouble() + e
				.getRealDouble() - 2 * f.getRealDouble()));
	}

	// -- Helper methods --

	private static void check(final RandomAccessibleInterval<?> input,
		final RandomAccessibleInterval<?> output)
	{
		if (!Intervals.equalDimensions(input, output)) {
			throw new IllegalArgumentException(
				"Input and output dimensions differ");
		}
	}

	private static <T extends RealType<T>> RandomAccessibleInterval<T> dilate(
		final BiFunction<Dimensions, T, Img<T>> createOp,
		final Computers.Arity3<RandomAccessible<T>, Shape, Integer, IterableInterval<T>> dilateOp,
		final RandomAccessibleInterval<T> input, final Shape shape)
	{
		final var type = input.getType().createVariable();
		type.setReal(type.getMinValue());
		final var result = temp(createOp, input);
		dilateOp.compute(Views.extendValue(input, type), shape, threads(), Views
			.iterable(result));
		return result;
	}

	private static <T extends RealType<T>> RandomAccessibleInterval<T> erode(
		final BiFunction<Dimensions, T, Img<T>> createOp,
		final Computers.Arity3<RandomAccessible<T>, Shape, Integer, IterableInterval<T>> erodeOp,
		final RandomAccessibleInterval<T> input, final Shape shape)
	{
		final var type = input.getType().createVariable();
		type.setReal(type.getMaxValue());
		final var result = temp(createOp, input);
		erodeOp.compute(Views.extendValue(input, type), shape, threads(), Views
			.iterable(result));
		return result;
	}

	private static <T extends RealType<T>> RandomAccessibleInterval<T> temp(
		final BiFunction<Dimensions, T, Img<T>> createOp,
		final RandomAccessibleInterval<T> input)
	{
		final Img<T> img = createOp.apply(input, input.getType().createVariable());
		return Views.isZeroMin(input) ? img : Views.translate(img, input
			.minAsLongArray());
	}

	private static int threads() {
		return Runtime.getRuntime().availableProcessors();
	}
}
