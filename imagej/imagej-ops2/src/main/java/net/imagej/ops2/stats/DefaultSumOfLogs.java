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
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.api.Op;
import org.scijava.ops.api.OpDependency;
import org.scijava.ops.api.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * {@link Op} to calculate the {@code stats.sumOfLogs}.
 * 
 * @author Gabriel Selzer
 * @param <I>
 *            input type
 * @param <O>
 *            output type
 */
@Plugin(type = Op.class, name = "stats.sumOfLogs", priority = Priority.HIGH)
public class DefaultSumOfLogs<I extends RealType<I>, O extends RealType<O>> implements Computers.Arity1<RandomAccessibleInterval<I>, O> {
	
	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, DoubleType, RandomAccessibleInterval<DoubleType>> imgCreator;
	
	@OpDependency(name = "math.log")
	private Computers.Arity1<RandomAccessibleInterval<I>, RandomAccessibleInterval<DoubleType>> logOp;
	
	@OpDependency(name = "stats.sum")
	private Computers.Arity1<RandomAccessibleInterval<DoubleType>, O> sumOp;

	/**
	 * TODO
	 *
	 * @param raiInput
	 * @param sumOfLogs
	 */
	@Override
	public void compute(final RandomAccessibleInterval<I> input, final O output) {
		RandomAccessibleInterval<DoubleType> logImg = imgCreator.apply(input, new DoubleType());
		logOp.compute(input, logImg);
		sumOp.compute(logImg, output);
	}
}
