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

package net.imagej.ops2.copy;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Copying {@link ImgLabeling} into another {@link ImgLabeling}
 * 
 * @author Christian Dietz (University of Konstanz)
 * @param <T>
 */
@Plugin(type = Op.class, name = "copy, copy.imgLabeling")
@Parameter(key = "input")
@Parameter(key = "output")
public class CopyImgLabeling<T extends IntegerType<T> & NativeType<T>, L>
		implements Computers.Arity1<ImgLabeling<L, T>, ImgLabeling<L, T>> {

	@OpDependency(name = "copy.rai")
	private Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> raiCopyOp;
	@OpDependency(name = "copy.labelingMapping")
	private Computers.Arity1<LabelingMapping<L>, LabelingMapping<L>> mappingCopyOp;

	@Override
	public void compute(final ImgLabeling<L, T> input, final ImgLabeling<L, T> output) {
		if (!Intervals.equalDimensions(input, output))
			throw new IllegalArgumentException("input and output must be of the same size!");
		if (Util.getTypeFromInterval(input.getIndexImg()).getClass() != Util.getTypeFromInterval(output.getIndexImg())
				.getClass())
			throw new IllegalArgumentException("input and output index images must be of the same type!");
		raiCopyOp.compute(input.getIndexImg(), output.getIndexImg());
		mappingCopyOp.compute(input.getMapping(), output.getMapping());
	}

}
