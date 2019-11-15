/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops.threshold.localMedian;

import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Local threshold method using median.
 *
 * @author Jonathan Hale
 * @author Stefan Helfrich (University of Konstanz)
 */
@Plugin(type = Op.class, name = "threshold.localMedian")
@Parameter(key = "inputNeighborhood")
@Parameter(key = "inputCenterPixel")
@Parameter(key = "c")
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class ComputeLocalMedianThreshold<T extends RealType<T>> implements
	Computers.Arity3<Iterable<T>, T, Double, BitType>
{

	@OpDependency(name = "stats.median")
	private Computers.Arity1<Iterable<T>, DoubleType> medianOp;

	@Override
	public void compute(final Iterable<T> inputNeighborhood,
		final T inputCenterPixel, final Double c, @Mutable final BitType output)
	{
		compute(inputNeighborhood, inputCenterPixel, c, medianOp, output);
	}

	public static <T extends RealType<T>> void compute(
		final Iterable<T> inputNeighborhood, final T inputCenterPixel,
		final Double c, final Computers.Arity1<Iterable<T>, DoubleType> medianOp,
		@Mutable final BitType output)
	{
		final DoubleType m = new DoubleType();
		medianOp.compute(inputNeighborhood, m);
		output.set(inputCenterPixel.getRealDouble() > m.getRealDouble() - c);
	}

}
