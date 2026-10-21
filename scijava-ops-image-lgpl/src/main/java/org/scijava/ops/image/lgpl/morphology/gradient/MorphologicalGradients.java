/*-
 * #%L
 * LGPL-licensed image processing operations for SciJava Ops, translated from LGPL ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.lgpl.morphology.gradient;

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
