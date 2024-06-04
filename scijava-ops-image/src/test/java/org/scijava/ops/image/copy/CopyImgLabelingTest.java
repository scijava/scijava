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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.imglib2.type.Type;
import net.imglib2.type.numeric.IntegerType;
import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.IntType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.types.Nil;

/**
 * Test
 * {@link Copiers#copyImgLabeling(Computers.Arity1, Computers.Arity1, ImgLabeling, ImgLabeling)}
 *
 * @author Christian Dietz (University of Konstanz)
 */
public class CopyImgLabelingTest<T extends Type<T>, L extends Type<L>, I extends IntegerType<I>>
	extends AbstractOpTest
{

	private ImgLabeling<String, IntType> input;
	private ImgLabeling<String, IntType> copy;

	@BeforeEach
	public void createData() {
		input = ops.op("create.imgLabeling").input(new FinalDimensions(10, 10),
			new IntType()).outType(new Nil<ImgLabeling<String, IntType>>()
		{}).apply();
		copy = ops.op("create.imgLabeling").input(new FinalDimensions(10, 10),
			new IntType()).outType(new Nil<ImgLabeling<String, IntType>>()
		{}).apply();

		final Cursor<LabelingType<String>> inc = input.cursor();

		while (inc.hasNext()) {
			inc.next().add(Math.random() > 0.5 ? "A" : "B");
		}

		// and another loop to construct some ABs
		while (inc.hasNext()) {
			inc.next().add(Math.random() > 0.5 ? "A" : "B");
		}

	}

	@Test
	public void testCopyImgLabeling() {
		ops.op("copy.imgLabeling").input(input).output(copy).compute();
		assertNotNull(copy);

		Cursor<LabelingType<String>> inCursor = input.cursor();
		for (final LabelingType<String> type : copy) {
			assertEquals(inCursor.next(), type);
		}
	}
}
