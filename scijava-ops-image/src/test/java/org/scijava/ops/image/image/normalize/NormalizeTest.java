/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.image.image.normalize;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * @author Martin Horn (University of Konstanz)
 */
public class NormalizeTest extends AbstractOpTest {

	@Test
	public void testNormalize() {

		// TODO these method calls are not right - need lazy normalization parameters
		// Ops

		Img<ByteType> in = TestImgGeneration.byteArray(true, 5, 5);
		Img<ByteType> out = in.factory().create(in, new ByteType());

		ops.op("image.normalize").arity1().input(in).output(out).compute();

		final Pair<ByteType, ByteType> minMax = ops.op("stats.minMax").arity1().input(in)
				.outType(new Nil<Pair<ByteType, ByteType>>() {}).apply();
		final Pair<ByteType, ByteType> minMax2 = ops.op("stats.minMax").arity1().input(out)
				.outType(new Nil<Pair<ByteType, ByteType>>() {}).apply();

		assertEquals(minMax2.getA().get(), Byte.MIN_VALUE);
		assertEquals(minMax2.getB().get(), Byte.MAX_VALUE);

		final ByteType min = new ByteType((byte) in.firstElement().getMinValue());
		final ByteType max = new ByteType((byte) in.firstElement().getMaxValue());

		final RandomAccessibleInterval<ByteType> lazyOut = ops.op("image.normalize").arity1().input(in)
				.outType(new Nil<RandomAccessibleInterval<ByteType>>() {}).apply();
		final RandomAccessibleInterval<ByteType> notLazyOut = ops.op("image.normalize")
				.arity5().input(in, minMax.getA(), minMax.getB(), min, max).outType(new Nil<RandomAccessibleInterval<ByteType>>() {})
				.apply();

		final Cursor<ByteType> outCursor = out.cursor();
		final Cursor<ByteType> lazyCursor = Views.flatIterable(lazyOut).cursor();
		final Cursor<ByteType> notLazyCursor = Views.flatIterable(notLazyOut).cursor();
		while (outCursor.hasNext()) {
			assertEquals(outCursor.next().get(), lazyCursor.next().get());
			assertEquals(outCursor.get().get(), notLazyCursor.next().get());
		}
	}
}
