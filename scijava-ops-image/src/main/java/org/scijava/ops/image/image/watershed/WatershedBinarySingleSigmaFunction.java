/*-
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

package org.scijava.ops.image.image.watershed;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * The Watershed algorithm segments and labels a grayscale image analogous to a
 * heightmap. In short, a drop of water following the gradient of an image flows
 * along a path to finally reach a local minimum.
 * <p>
 * This Op wraps {@link WatershedBinarySingleSigma} as a Function for
 * convenience.
 * </p>
 *
 * @author Gabriel Selzer
 * @implNote op names='image.watershed'
 */
public class WatershedBinarySingleSigmaFunction<T extends RealType<T>, B extends BooleanType<B>>
	implements
	Functions.Arity5<RandomAccessibleInterval<T>, Boolean, Boolean, Double, RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>>
{

	@OpDependency(name = "image.watershed")
	private Computers.Arity5<RandomAccessibleInterval<T>, Boolean, Boolean, Double, RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>> watershedOp;
	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	/**
	 * TODO
	 *
	 * @param in
	 * @param useEightConnectivity
	 * @param drawWatersheds
	 * @param sigma
	 * @param mask
	 * @return the outputLabeling
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply( //
		RandomAccessibleInterval<T> in, //
		Boolean useEightConnectivity, //
		Boolean drawWatersheds, //
		Double sigma, //
		@Nullable RandomAccessibleInterval<B> mask //
	) {
        var outputLabeling = labelingCreator.apply(in,
			new IntType());
		watershedOp.compute(in, useEightConnectivity, drawWatersheds, sigma, mask,
			outputLabeling);
		return outputLabeling;
	}
}
