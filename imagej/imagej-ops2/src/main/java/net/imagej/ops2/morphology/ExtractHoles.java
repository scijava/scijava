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

package net.imagej.ops2.morphology;

import java.util.function.BiConsumer;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.BooleanType;

import org.scijava.function.Computers;
import org.scijava.ops.Op;
import org.scijava.ops.OpDependency;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Extracts the holes from a binary image.
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Daniel Seebacher (University of Konstanz)
 */
@Plugin(type = Op.class, name = "morphology.extractHoles")
public class ExtractHoles<T extends BooleanType<T>> implements
	Computers.Arity2<RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<T>>
{

	private BiConsumer<T, T> xor = (in, out) -> out.set(in.get() ^ out.get());

	@OpDependency(name = "morphology.fillHoles")
	private Computers.Arity2<RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<T>> fillHolesComp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param structElement
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input,
		final Shape structElement, final RandomAccessibleInterval<T> output)
	{
		fillHolesComp.compute(input, structElement, output);
		LoopBuilder.setImages(input, output).forEachPixel(xor);
	}

}

@Plugin(type = Op.class, name = "morphology.extractHoles")
class SimpleExtractHolesComputer<T extends BooleanType<T>> implements
	Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
{
	@OpDependency(name = "morphology.extractHoles")
	private Computers.Arity2<RandomAccessibleInterval<T>, Shape, RandomAccessibleInterval<T>> extractOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param output
	 */
	@Override
	public void compute(RandomAccessibleInterval<T> in1, RandomAccessibleInterval<T> out) {
		Shape defaultStructElement = new RectangleShape(1, false);
		extractOp.compute(in1, defaultStructElement, out);
	}

}
