/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.labeling;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.IntegerType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * Merges the labels of two {@link ImgLabeling} within a defined mask (if
 * provided). Outside of the mask, labels will be empty.
 *
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op names='labeling.merge'
 */
public class MergeLabeling<L, I extends IntegerType<I>, B extends BooleanType<B>>
	implements
	Functions.Arity3<ImgLabeling<L, I>, ImgLabeling<L, I>, RandomAccessibleInterval<B>, ImgLabeling<L, I>>
{

	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, I, ImgLabeling<L, I>> imgLabelingCreator;

	@OpDependency(name = "engine.adapt")
	private Function<Computers.Arity2<LabelingType<L>, LabelingType<L>, LabelingType<L>>, Computers.Arity2<Iterable<LabelingType<L>>, Iterable<LabelingType<L>>, Iterable<LabelingType<L>>>> adaptor;

	/**
	 * TODO
	 *
	 * @param input1
	 * @param input2
	 * @param mask
	 * @return an {@link ImgLabeling} that combines the labels of {@code input1}
	 *         and {@code input2}
	 */
	@Override
	public ImgLabeling<L, I> apply( //
		final ImgLabeling<L, I> input1, //
		final ImgLabeling<L, I> input2, //
		@Nullable final RandomAccessibleInterval<B> mask //
	) {
		final var output = imgLabelingCreator.apply(input1, input1.getSource().firstElement());
		if (mask != null) {
			final IterableRegion<B> iterable = Regions.iterable(mask);
			final var sample = Regions.sample(iterable.inside(), output);
			final var randomAccess = input1.randomAccess();
			final var randomAccess2 = input2.randomAccess();
			final var cursor = sample.cursor();
			while (cursor.hasNext()) {
				final var outLabeling = cursor.next();
				randomAccess.setPosition(cursor);
				outLabeling.addAll(randomAccess.get());
				randomAccess2.setPosition(cursor);
				outLabeling.addAll(randomAccess2.get());
			}
		}
		else {
            var adapted =
				adaptor.apply(
					new Computers.Arity2<LabelingType<L>, LabelingType<L>, LabelingType<L>>()
					{

						@Override
						public void compute(final LabelingType<L> input1,
							final LabelingType<L> input2, final LabelingType<L> output)
					{
							output.addAll(input1);
							output.addAll(input2);
						}
					});
			adapted.compute(input1, input2, output);
		}

		return output;
	}
}
