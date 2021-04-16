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

package net.imagej.ops2.labeling.cca;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.functions.Functions;
import org.scijava.functions.Functions;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Default Implementation wrapping {@link ConnectedComponents} of
 * ImgLib2-algorithms.
 * 
 * @author Christian Dietz (University of Konstanz)
 */
@Plugin(type = Op.class, name = "labeling.cca", priority = 1.0)
@Parameter(key = "input")
@Parameter(key = "executorService")
@Parameter(key = "structuringElement")
@Parameter(key = "labelGenerator")
@Parameter(key = "labeling")
public class DefaultCCA<T extends IntegerType<T>, L, I extends IntegerType<I>> implements
		Functions.Arity4<RandomAccessibleInterval<T>, ExecutorService, StructuringElement, Iterator<Integer>, ImgLabeling<Integer, IntType>> {

	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<L, IntType>> imgLabelingCreator;

	@SuppressWarnings("unchecked")
	@Override
	public ImgLabeling<Integer, IntType> apply(final RandomAccessibleInterval<T> input, ExecutorService es,
			StructuringElement se, Iterator<Integer> labelGenerator) {
		ImgLabeling<Integer, IntType> output = (ImgLabeling<Integer, IntType>) imgLabelingCreator.apply(input,
				new IntType());
		ConnectedComponents.labelAllConnectedComponents(input, output, labelGenerator, se, es);
		return output;
	}

}

@Plugin(type = Op.class, name = "labeling.cca", priority = 1.0)
@Parameter(key = "input")
@Parameter(key = "executorService")
@Parameter(key = "structuringElement")
@Parameter(key = "labeling")
class SimpleCCA<T extends IntegerType<T>, L, I extends IntegerType<I>> implements
		Functions.Arity3<RandomAccessibleInterval<T>, ExecutorService, StructuringElement, ImgLabeling<Integer, IntType>> {
	@OpDependency(name = "labeling.cca")
	private Functions.Arity4<RandomAccessibleInterval<T>, ExecutorService, StructuringElement, Iterator<Integer>, ImgLabeling<Integer, IntType>> labeler;

	@SuppressWarnings("unchecked")
	@Override
	public ImgLabeling<Integer, IntType> apply(RandomAccessibleInterval<T> input, ExecutorService es,
			StructuringElement structuringElement) {
		DefaultLabelIterator defaultLabelIterator = new DefaultLabelIterator();
		// note the case to Iterator<L> is okay because L is a vacuous type parameter,
		// meaning that any Object can go there.
		return labeler.apply(input, es, structuringElement, defaultLabelIterator);
	}

}

/*
 * Simple Default LabelIterator providing integer labels, starting from zero.
 */
class DefaultLabelIterator implements Iterator<Integer> {

	private Integer i = 0;

	@Override
	public boolean hasNext() {
		return i < Integer.MAX_VALUE;
	}

	@Override
	public Integer next() {
		return i++;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
