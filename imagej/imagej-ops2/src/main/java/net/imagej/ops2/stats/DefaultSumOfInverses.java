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

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.functions.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * {@link Op} to calculate the {@code stats.sumOfInverses}.
 * 
 * @author Gabriel Selzer
 * @param <I>
 *            input type
 * @param <O>
 *            output type
 */
@Plugin(type = Op.class, name = "stats.sumOfInverses", priority = Priority.HIGH)
@Parameter(key = "raiInput")
@Parameter(key = "sumOfInverses")
public class DefaultSumOfInverses<I extends RealType<I>, O extends RealType<O>> implements Computers.Arity2<RandomAccessibleInterval<I>, O, O> {
	
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, O, RandomAccessibleInterval<O>> imgCreator;
	
	//TODO: Can we lift this? Would require making a RAI of Doubles.
	@OpDependency(name = "math.reciprocal")
	private Computers.Arity2<I, Double, O> reciprocalOp;
	
	@OpDependency(name = "stats.sum")
	private Computers.Arity1<RandomAccessibleInterval<O>, O> sumOp;

	@Override
	public void compute(final RandomAccessibleInterval<I> input, final O dbzValue, final O output) {
		RandomAccessibleInterval<O> tmpImg = imgCreator.apply(input, output);
		//TODO: Can we lift this? Would require making a RAI of Doubles.
		LoopBuilder.setImages(input, tmpImg).multiThreaded().forEachPixel((inPixel, outPixel) -> {
			reciprocalOp.compute(inPixel, dbzValue.getRealDouble(), outPixel);
		});
		sumOp.compute(tmpImg, output);
	}
}
