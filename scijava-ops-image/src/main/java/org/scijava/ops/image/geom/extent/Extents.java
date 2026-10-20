/*
 * #%L
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

package org.scijava.ops.image.geom.extent;

import java.util.function.Function;

import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;

import org.scijava.ops.spi.OpDependency;

/**
 * Ops computing the extent of a shape: the ratio of its size to the size of
 * its axis-aligned bounding box. The extent is 1 for an axis-aligned
 * rectangle (or box) and decreases for shapes that fill their bounding box
 * less completely. Unlike {@code geom.boxivity}, which uses the smallest
 * (possibly rotated) enclosing rectangle, the extent depends on the
 * orientation of the shape.
 * <p>
 * Based on the "Extent" shape descriptor of the {@code Extended_Particle_Analyzer}
 * plugin of the BioVoxxel Toolbox by Jan Brocher / BioVoxxel (BSD 3-Clause
 * license, Copyright (C) Jan Brocher / BioVoxxel), generalized to n-D
 * regions.
 * </p>
 *
 * @author Jan Brocher (BioVoxxel)
 * @author Curtis Rueden
 * @see <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 *      Toolbox</a>
 */
public final class Extents {

	private Extents() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Computes the extent of a polygon: its area divided by the area of its
	 * axis-aligned bounding box.
	 *
	 * @param sizeOp the Op computing the area of the polygon
	 * @param input the polygon
	 * @param output the extent
	 * @implNote op names='geom.extent', label='Geometric (2D): Extent',
	 *           type=Computer
	 */
	public static void polygon( //
		@OpDependency(name = "geom.size") Function<Polygon2D, DoubleType> sizeOp, //
		final Polygon2D input, //
		final DoubleType output //
	) {
		final var boxArea = (input.realMax(0) - input.realMin(0)) * (input
			.realMax(1) - input.realMin(1));
		output.set(sizeOp.apply(input).get() / boxArea);
	}

	/**
	 * Computes the extent of an n-dimensional region, such as a
	 * {@link net.imglib2.roi.labeling.LabelRegion}: its number of elements
	 * divided by the number of elements of its bounding interval.
	 *
	 * @param input the region
	 * @param output the extent
	 * @param <B> the boolean type of the region
	 * @implNote op names='geom.extent', label='Geometric: Extent',
	 *           type=Computer
	 */
	public static <B extends BooleanType<B>> void region(
		final IterableRegion<B> input, final DoubleType output)
	{
		output.set((double) input.inside().size() / Intervals.numElements(input));
	}
}
