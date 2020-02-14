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

package net.imagej.ops.labeling;

import java.util.function.BiFunction;

import net.imagej.ops.map.Maps;
import net.imagej.ops.special.computer.AbstractBinaryComputerOp;
import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.function.Function3;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Merges the labels of two {@link ImgLabeling} within a defined mask (if
 * provided). Outside of the mask, labels will be empty.
 *
 * @author Stefan Helfrich (University of Konstanz)
 */
@Plugin(type = Op.class, name = "labeling.merge")
@Parameter(key = "labeling1")
@Parameter(key = "labeling2")
@Parameter(key = "mask")
@Parameter(key = "combinedLabeling", itemIO = ItemIO.OUTPUT)
public class MergeLabeling<L, I extends IntegerType<I>, B extends BooleanType<B>>
		implements Function3<ImgLabeling<L, I>, ImgLabeling<L, I>, RandomAccessibleInterval<B>, ImgLabeling<L, I>> {

	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, I, ImgLabeling<L, I>> imgLabelingCreator;

	@SuppressWarnings({ "unchecked", "rawtypes", "hiding" })
	@Override
	public ImgLabeling<L, I> apply(final ImgLabeling<L, I> input1, final ImgLabeling<L, I> input2,
			final RandomAccessibleInterval<B> mask) {
		final ImgLabeling<L, I> output = imgLabelingCreator.apply(input1,
				Views.iterable(input1.getSource()).firstElement());
		if (mask != null) {
			final IterableRegion iterable = Regions.iterable(mask);
			final IterableInterval<LabelingType<L>> sample = Regions.sample(iterable, output);
			final RandomAccess<LabelingType<L>> randomAccess = input1.randomAccess();
			final RandomAccess<LabelingType<L>> randomAccess2 = input2.randomAccess();
			final Cursor<LabelingType<L>> cursor = sample.cursor();
			while (cursor.hasNext()) {
				final LabelingType<L> outLabeling = cursor.next();
				randomAccess.setPosition(cursor);
				outLabeling.addAll(randomAccess.get());
				randomAccess2.setPosition(cursor);
				outLabeling.addAll(randomAccess2.get());
			}
		} else {
			Maps.map((IterableInterval) input1, (IterableInterval) input2, (IterableInterval) output,
					new AbstractBinaryComputerOp<LabelingType<L>, LabelingType<L>, LabelingType<L>>() {

						@Override
						public void compute(final LabelingType<L> input1, final LabelingType<L> input2,
								final LabelingType<L> output) {
							output.addAll(input1);
							output.addAll(input2);
						}
					});
		}

		return output;
	}
}

@Plugin(type = Op.class, name = "labeling.merge")
@Parameter(key = "labeling1")
@Parameter(key = "labeling2")
@Parameter(key = "combinedLabeling", itemIO = ItemIO.OUTPUT)
class MergeLabelingMaskless<L, I extends IntegerType<I>, B extends BooleanType<B>>
		implements BiFunction<ImgLabeling<L, I>, ImgLabeling<L, I>, ImgLabeling<L, I>> {

	@OpDependency(name = "labeling.merge")
	private Function3<ImgLabeling<L, I>, ImgLabeling<L, I>, RandomAccessibleInterval<B>, ImgLabeling<L, I>> mergeOp;

	@Override
	public ImgLabeling<L, I> apply(ImgLabeling<L, I> t, ImgLabeling<L, I> u) {
		return mergeOp.apply(t, u, null);
	}

}
