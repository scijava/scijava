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

package net.imagej.ops2.stats;

import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * {@link Op} to calculate the {@code stats.variance} using the
 * {@code stats.stdDev} using the two-pass algorithm.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 * @author Christian Dietz (University of Konstanz)
 * @param <I>
 *            input type
 * @param <O>
 *            output type
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Two-pass_algorithm">
 *      Wikipedia </a>
 */
@Plugin(type = Op.class, name = "stats.variance")
@Parameter(key = "iterableInput")
@Parameter(key = "variance", itemIO = ItemIO.BOTH)
public class DefaultVariance<I extends RealType<I>, O extends RealType<O>> implements Computers.Arity1<Iterable<I>, O> {

	@OpDependency(name = "stats.mean")
	private Computers.Arity1<Iterable<I>, O> meanOp;

	@Override
	public void compute(final Iterable<I> input, final O output) {

		final O mean = output.createVariable();
		meanOp.compute(input, mean);

		int n = 0;
		double sum = 0d;

		for (I in : input) {
			n++;
			double x = in.getRealDouble();
			sum += (x - mean.getRealDouble()) * (x - mean.getRealDouble());
		}

		output.setReal(sum / (n - 1));
	}
}