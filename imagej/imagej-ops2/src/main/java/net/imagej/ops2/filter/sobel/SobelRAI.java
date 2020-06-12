/* #%L
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

package net.imagej.ops2.filter.sobel;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Sobel filter implementation using separated sobel kernel.
 * 
 * @author Eike Heinz, University of Konstanz
 *
 * @param <T>
 *            type of input
 */

@Plugin(type = Op.class, name = "filter.sobel")
@Parameter(key = "input")
@Parameter(key = "output", itemIO = ItemIO.BOTH)
public class SobelRAI<T extends RealType<T>>
		implements Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {
	
	@OpDependency(name = "create.img")
	private Function<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> createRAI;

	@OpDependency(name = "math.sqr")
	private Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> squareMapOp;

	@OpDependency(name = "math.sqrt")
	private Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> sqrtMapOp;

	@OpDependency(name = "math.add")
	private Computers.Arity2<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> addOp;

	@OpDependency(name = "filter.partialDerivative")
	private Computers.Arity2<RandomAccessibleInterval<T>, Integer, RandomAccessibleInterval<T>> derivativeComputer;
	
	@Override
	public void compute(RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output) {

		for (int i = 0; i < input.numDimensions(); i++) {
			RandomAccessibleInterval<T> derivative = createRAI.apply(input);
			derivativeComputer.compute(input, i, derivative);
			squareMapOp.compute(derivative, derivative);
			addOp.compute(output, derivative, output);
		}
		sqrtMapOp.compute(output, output);
	}

}
