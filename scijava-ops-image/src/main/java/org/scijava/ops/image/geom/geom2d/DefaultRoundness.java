/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.geom.geom2d;

import java.util.function.Function;

import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Generic implementation of {@code geom.roundness}. Based on
 * https://imagej.nih.gov/ij/plugins/descriptors.html.
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @implNote op names='geom.roundness', label='Geometric (2D): Roundness'
 */
public class DefaultRoundness implements
	Computers.Arity1<Polygon2D, DoubleType>
{

	@OpDependency(name = "geom.size")
	private Function<Polygon2D, DoubleType> areaFunc;
	@OpDependency(name = "geom.majorAxis")
	private Function<Polygon2D, DoubleType> majorAxisFunc;

	/**
	 * TODO
	 *
	 * @param input
	 * @param roundness
	 */
	@Override
	public void compute(final Polygon2D input, final DoubleType output) {
		output.set(4 * (areaFunc.apply(input).getRealDouble() / (Math.PI * Math.pow(
			majorAxisFunc.apply(input).getRealDouble(), 2))));
	}

}
