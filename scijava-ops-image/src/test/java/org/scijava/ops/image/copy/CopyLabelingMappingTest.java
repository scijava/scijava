/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.copy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Test @link {@link CopyLabelingMapping}.
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
public class CopyLabelingMappingTest extends AbstractOpTest {

	private LabelingMapping<String> input;

	@BeforeEach
	public void createData() {
		final ImgLabeling<String, IntType> imgL = ops.op("create.imgLabeling")
			.input(new FinalDimensions(10, 10), new IntType()) //
			.outType(new Nil<ImgLabeling<String, IntType>>()
			{}) //
			.apply();

		final Cursor<LabelingType<String>> inc = imgL.cursor();

		while (inc.hasNext()) {
			inc.next().add(Math.random() > 0.5 ? "A" : "B");
		}

		// and another loop to construct some ABs
		while (inc.hasNext()) {
			inc.next().add(Math.random() > 0.5 ? "A" : "B");
		}

		input = imgL.getMapping();
	}

	@Test
	public void testCopyLabelingWithoutOutput() {

		LabelingMapping<String> out = ops.op("copy.labelingMapping").input(input)
			.outType(new Nil<LabelingMapping<String>>()
			{}).apply();

		Iterator<String> outIt = out.getLabels().iterator();

		for (String l : input.getLabels()) {
			assertEquals(l, outIt.next());
		}
	}

	@Test
	public void testCopyLabelingWithOutput() {

		LabelingMapping<String> out = ops.op("create.labelingMapping").outType(
			new Nil<LabelingMapping<String>>()
			{}).create();

		ops.op("copy.labelingMapping").input(input).output(out).compute();

		Iterator<String> outIt = out.getLabels().iterator();

		for (String l : input.getLabels()) {
			assertEquals(l, outIt.next());
		}
	}
}
