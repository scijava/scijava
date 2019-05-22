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

package net.imagej.ops.threshold.localNiblack;

import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.computer.Computer;
import org.scijava.ops.core.computer.Computer4;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * LocalThresholdMethod using Niblack's thresholding method.
 *
 * @author Jonathan Hale
 * @author Stefan Helfrich (University of Konstanz)
 */
@Plugin(type = Op.class, name = "threshold.localNiblack",
	priority = Priority.LOW)
@Parameter(key = "inputNeighborhood")
@Parameter(key = "inputCenterPixel")
@Parameter(key = "c")
@Parameter(key = "k")
@Parameter(key = "output", type = ItemIO.BOTH)
public class LocalNiblackThreshold<T extends RealType<T>> implements
	Computer4<Iterable<T>, T, Double, Double, BitType>
{

	@OpDependency(name = "stats.mean")
	private Computer<Iterable<T>, DoubleType> meanOp;

	@OpDependency(name = "stats.stdDev")
	private Computer<Iterable<T>, DoubleType> stdDeviationOp;

	@Override
	public void compute(final Iterable<T> inputNeighborhood,
		final T inputCenterPixel, final Double c, final Double k,
		@Mutable final BitType output)
	{
		compute(inputNeighborhood, inputCenterPixel, c, k, meanOp, stdDeviationOp,
			output);
	}

	public static <T extends RealType<T>> void compute(
		final Iterable<T> inputNeighborhood, final T inputCenterPixel,
		final Double c, final Double k,
		final Computer<Iterable<T>, DoubleType> meanOp,
		final Computer<Iterable<T>, DoubleType> stdDeviationOp,
		@Mutable final BitType output)
	{
		final DoubleType m = new DoubleType();
		meanOp.compute(inputNeighborhood, m);

		final DoubleType stdDev = new DoubleType();
		stdDeviationOp.compute(inputNeighborhood, stdDev);

		output.set(inputCenterPixel.getRealDouble() > m.getRealDouble() + k * stdDev
			.getRealDouble() - c);
	}

	// TODO: How to port old Contingent code?:
	// final RectangleShape rect = getShape() instanceof RectangleShape
	// ? (RectangleShape) getShape() : null;
	// if (rect == null) {
	// return true;
	// }
	// return rect.getSpan()<=2;

}
