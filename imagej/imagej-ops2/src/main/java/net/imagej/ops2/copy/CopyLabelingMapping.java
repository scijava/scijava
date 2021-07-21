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

import java.util.List;
import java.util.Set;

import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.roi.labeling.LabelingMapping.SerialisationAccess;

import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.api.Op;
import org.scijava.plugin.Plugin;

/**
 * Copies a {@link LabelingMapping} into another {@link LabelingMapping}
 * 
 * @author Christian Dietz (University of Konstanz)
 * @param <L>
 */
@Plugin(type = Op.class, name = "copy, copy.labelingMapping", priority = Priority.VERY_HIGH)
public class CopyLabelingMapping<L> implements Computers.Arity1<LabelingMapping<L>, LabelingMapping<L>> {

	/**
	 * TODO
	 *
	 * @param input
	 * @param output
	 */
	@Override
	public void compute(final LabelingMapping<L> input, final LabelingMapping<L> output) {

		final LabelingMappingSerializationAccess<L> access = new LabelingMappingSerializationAccess<>(output);
		access.setLabelSets(new LabelingMappingSerializationAccess<>(input).getLabelSets());
	}

}

//@Plugin(type = Op.class, name = "copy.labelingMapping", priority = Priority.VERY_HIGH)
//class CopyLabelingMappingFunction<L> implements Function<LabelingMapping<L>, LabelingMapping<L>> {
//
//	@OpDependency(name = "copy.labelingMapping")
//	Computers.Arity1<LabelingMapping<L>, LabelingMapping<L>> copyOp;
//	@OpDependency(name = "create.labelingMapping")
//	private Function<Integer, LabelingMapping<L>> outputCreator;
//
//	@Override
//	public LabelingMapping<L> apply(LabelingMapping<L> input) {
//		LabelingMapping<L> output = outputCreator.apply(input.numSets());
//		copyOp.compute(input, output);
//		return output;
//	}
//}

/*
 * Access to LabelingMapping
 */
final class LabelingMappingSerializationAccess<T> extends SerialisationAccess<T> {

	protected LabelingMappingSerializationAccess(final LabelingMapping<T> labelingMapping) {
		super(labelingMapping);
	}

	@Override
	protected void setLabelSets(List<Set<T>> labelSets) {
		super.setLabelSets(labelSets);
	}

	@Override
	protected List<Set<T>> getLabelSets() {
		return super.getLabelSets();
	}

}
