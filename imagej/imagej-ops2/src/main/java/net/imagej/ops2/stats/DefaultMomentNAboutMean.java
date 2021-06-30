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

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * {@link Op} to calculate the {@code stats.momentNAboutMean} using
 * {@code stats.mean} and {@code stats.size}. N can be bounded to any positive {@link Integer}
 * 
 * @author Gabriel Selzer
 * @param <I>
 *            input type
 * @param <O>
 *            output type
 */
@Plugin(type = Op.class, name = "stats.momentNAboutMean", priority = Priority.HIGH)
public class DefaultMomentNAboutMean<I extends RealType<I>, O extends RealType<O>> implements Computers.Arity2<RandomAccessibleInterval<I>, Integer, O> {

	@OpDependency(name = "stats.mean")
	private Computers.Arity1<RandomAccessibleInterval<I>, DoubleType> meanComputer;
	@OpDependency(name = "stats.size")
	private Computers.Arity1<RandomAccessibleInterval<I>, DoubleType> sizeComputer;
	@OpDependency(name = "math.power")
	private Computers.Arity2<DoubleType, Integer, DoubleType> powOp;

	/**
	 * TODO
	 *
	 * @param iterableInput
	 * @param moment3AboutMean
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> input, final Integer n, final O output) {
		final DoubleType mean = new DoubleType();
		meanComputer.compute(input, mean);
		final DoubleType size = new DoubleType();
		sizeComputer.compute(input, size);

		List<DoubleType> chunkSums = LoopBuilder.setImages(input).multiThreaded().forEachChunk(chunk -> {
			DoubleType chunkSum = new DoubleType(0);
			DoubleType difference = new DoubleType();
			DoubleType product = new DoubleType();
			chunk.forEachPixel(pixel -> {
				difference.set(pixel.getRealDouble());
				difference.sub(mean);
				powOp.compute(difference, n, product);
				chunkSum.add(product);
			});
			return chunkSum;
		});

		DoubleType sum = new DoubleType(0);
		for(DoubleType chunkSum : chunkSums) sum.add(chunkSum);
		sum.div(size);

		output.setReal(sum.getRealDouble());
	}
}
