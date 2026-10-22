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
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Extracts the foreground object of a three-channel color image using
 * SIOX (Simple Interactive Object Extraction), guided by masks of known
 * foreground and known background samples.
 * <p>
 * Samples in neither mask are of unknown affiliation; the connected
 * components of the result containing known foreground samples are always
 * kept. This is equivalent to the trimap variant of {@code segment.siox}
 * with confidence 1 for foreground, 0 for background and 0.5 for unknown
 * samples. Unknown samples are classified by comparing their CIELAB color
 * to color signatures of the known background and foreground, followed by
 * smoothing and connected component filtering. The channels (red, green and
 * blue) lie along the last axis of the image, which may have any number of
 * spatial dimensions; the original algorithm is 2D. Samples of integer types
 * are scaled by the maximum value of their type (e.g. 255 for 8-bit), and
 * real samples are expected to lie in [0, 1]; values outside that range are
 * clamped.
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
 * <p>
 * The original SIOX license notice follows.
 * </p>
 *
 * <pre>
 * Copyright 2005, 2006 by Gerald Friedland, Kristian Jantz and Lars Knipping
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </pre>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Gerald Friedland
 * @author Kristian Jantz
 * @author Lars Knipping
 * @author Ignacio Arganda-Carreras
 * @param <T> type of the image channels
 * @param <B> type of the masks
 * @implNote op names='segment.siox'
 */
public class SioxChannelsMasks<T extends RealType<T>, B extends BooleanType<B>> implements
	Functions.Arity5<RandomAccessibleInterval<T>, RandomAccessibleInterval<B>, RandomAccessibleInterval<B>, Integer, Double, RandomAccessibleInterval<BitType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, BitType, RandomAccessibleInterval<BitType>> imgCreator;

	/**
	 * Segments the input image using SIOX.
	 *
	 * @param image the color image to segment, with three channels along its
	 *          last axis, without NaN
	 * @param foreground mask of the known foreground samples, with the spatial
	 *          dimensions of the image
	 * @param background mask of the known background samples, with the spatial
	 *          dimensions of the image, which must not overlap the foreground
	 * @param smoothness the number of smoothing steps in the post-processing
	 *          (default 6); larger values yield smoother outlines
	 * @param sizeFactorToKeep which connected components to keep (default 0):
	 *          if 0, only those containing known foreground; otherwise
	 *          also the largest one and every one whose size is at least the
	 *          largest size divided by sizeFactorToKeep (4 corresponds to the
	 *          multiple-components option of the Fiji plugin)
	 * @return the foreground mask
	 */
	@Override
	public RandomAccessibleInterval<BitType> apply( //
		final RandomAccessibleInterval<T> image, //
		final RandomAccessibleInterval<B> foreground, //
		final RandomAccessibleInterval<B> background, //
		@Nullable final Integer smoothness, //
		@Nullable final Double sizeFactorToKeep //
	) {
		final var output = imgCreator.apply(Siox.spatialDims(image),
			new BitType());
		Siox.segmentChannels(image, foreground, background, smoothness,
			sizeFactorToKeep, output);
		return output;
	}

}
