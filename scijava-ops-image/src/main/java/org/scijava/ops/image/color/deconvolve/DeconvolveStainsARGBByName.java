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

package org.scijava.ops.image.color.deconvolve;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Separates the stains of a packed {@link ARGBType} brightfield image by
 * colour deconvolution (Ruifrok and Johnston, 2001), using a named set of
 * published stain vectors from {@link StainVectors}. The alpha channel is
 * ignored. See {@link DeconvolveStains} for details.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @see <a href="https://pubmed.ncbi.nlm.nih.gov/11531144/">A. C. Ruifrok and
 *      D. A. Johnston, "Quantification of histochemical staining by color
 *      deconvolution", Anal. Quant. Cytol. Histol. 23(4):291-299, 2001</a>
 * @implNote op names='color.deconvolveStains'
 */
public class DeconvolveStainsARGBByName implements
	Functions.Arity3<RandomAccessibleInterval<ARGBType>, String, Double, RandomAccessibleInterval<DoubleType>>
{

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> createOp;

	/**
	 * Deconvolves the stains of an ARGB image.
	 *
	 * @param input the ARGB image
	 * @param stainName the name of the stain combination, e.g. "H&amp;E", "H
	 *          DAB" or "H&amp;E DAB" (see {@link StainVectors})
	 * @param i0 the incident light intensity, i.e. the intensity of unstained
	 *          background (default: 255)
	 * @return the stain amounts in OD units, with one more dimension than the
	 *         input; index k along the last dimension holds stain k
	 */
	@Override
	public RandomAccessibleInterval<DoubleType> apply(
		final RandomAccessibleInterval<ARGBType> input, final String stainName,
		@Nullable final Double i0)
	{
		return DeconvolveStainsARGB.deconvolve(createOp, input, StainVectors
			.fromName(stainName).vectors(), i0);
	}
}
