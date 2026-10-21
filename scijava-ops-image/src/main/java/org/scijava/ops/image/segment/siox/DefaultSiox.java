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

package org.scijava.ops.image.segment.siox;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Extracts the foreground object of an {@link ARGBType} color image using
 * SIOX (Simple Interactive Object Extraction), guided by a confidence matrix
 * (trimap).
 * <p>
 * The trimap marks each sample as known background (confidence of 0.1 or
 * less), known foreground (confidence of 0.8 or more) or unknown (anything in
 * between). Samples with a confidence of exactly 1 also mark connected
 * components of the result that are always kept. Unknown samples are
 * classified by comparing their CIELAB color to color signatures of the known
 * background and foreground, followed by smoothing and connected component
 * filtering. The image may have any number of dimensions; the original
 * algorithm is 2D.
 * </p>
 * <p>
 * SIOX is described in G. Friedland, K. Jantz and R. Rojas, "SIOX: Simple
 * Interactive Object Extraction in Still Images", Proceedings of the Seventh
 * IEEE International Symposium on Multimedia (ISM 2005), pp. 253&ndash;260,
 * <a href="https://doi.org/10.1109/ISM.2005.106">doi:10.1109/ISM.2005.106</a>.
 * This op is a translation of the SIOX reference implementation of the
 * <a href="https://github.com/fiji/Siox_Segmentation">Fiji
 * Siox_Segmentation</a> project, Copyright 2005, 2006 by Gerald Friedland,
 * Kristian Jantz and Lars Knipping, licensed under the Apache License, Version
 * 2.0; see {@link Siox} for details and changes.
 * </p>
 *
 * @author Gerald Friedland
 * @author Kristian Jantz
 * @author Lars Knipping
 * @author Ignacio Arganda-Carreras
 * @author Curtis Rueden
 * @param <R> type of the trimap
 * @implNote op names='segment.siox'
 */
public class DefaultSiox<R extends RealType<R>> implements
	Functions.Arity4<RandomAccessibleInterval<ARGBType>, RandomAccessibleInterval<R>, Integer, Double, RandomAccessibleInterval<BitType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, BitType, RandomAccessibleInterval<BitType>> imgCreator;

	/**
	 * Segments the input image using SIOX.
	 *
	 * @param image the color image to segment
	 * @param trimap the foreground confidence of each sample, with the same
	 *          dimensions as the image: at most 0.1 for known background, at
	 *          least 0.8 for known foreground (exactly 1 to also always keep
	 *          the connected component), anything else for unknown
	 * @param smoothness the number of smoothing steps in the post-processing
	 *          (default 6); larger values yield smoother outlines
	 * @param sizeFactorToKeep which connected components to keep (default 0):
	 *          if 0, only those containing samples of confidence 1; otherwise
	 *          also the largest one and every one whose size is at least the
	 *          largest size divided by sizeFactorToKeep (4 corresponds to the
	 *          multiple-components option of the Fiji plugin)
	 * @return the foreground mask
	 */
	@Override
	public RandomAccessibleInterval<BitType> apply( //
		final RandomAccessibleInterval<ARGBType> image, //
		final RandomAccessibleInterval<R> trimap, //
		@Nullable final Integer smoothness, //
		@Nullable final Double sizeFactorToKeep //
	) {
		final var output = imgCreator.apply(image, new BitType());
		Siox.segment(image, trimap, smoothness, sizeFactorToKeep, output);
		return output;
	}

}
