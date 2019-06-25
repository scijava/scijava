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

package net.imagej.ops.image.normalize;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Pair;

import org.junit.Test;

/**
 * @author Martin Horn (University of Konstanz)
 */
public class NormalizeTest extends AbstractOpTest {

	@Test
	public void testNormalize() {
		
		//TODO these method calls are not right - need lazy normalization parameters Ops

		Img<ByteType> in = generateByteArrayTestImg(true, 5, 5);
		Img<ByteType> out = in.factory().create(in, new ByteType());

		ops.run("image.normalize", in, out);

		final Pair<ByteType, ByteType> minMax = (Pair<ByteType, ByteType>) ops.run("stats.minMax", in);
		final Pair<ByteType, ByteType> minMax2 = (Pair<ByteType, ByteType>) ops.run("stats.minMax", out);

		assertEquals(minMax2.getA().get(), Byte.MIN_VALUE);
		assertEquals(minMax2.getB().get(), Byte.MAX_VALUE);

		final ByteType min = new ByteType((byte) in.firstElement().getMinValue());
		final ByteType max = new ByteType((byte) in.firstElement().getMaxValue());

		final IterableInterval<ByteType> lazyOut = (IterableInterval<ByteType>) ops.run("image.normalize", in);
		final IterableInterval<ByteType> notLazyOut = (IterableInterval<ByteType>) ops.run("image.normalize", in, minMax.getA(),
				minMax.getB(), min, max);

		final Cursor<ByteType> outCursor = out.cursor();
		final Cursor<ByteType> lazyCursor = lazyOut.cursor();
		final Cursor<ByteType> notLazyCursor = notLazyOut.cursor();
		while (outCursor.hasNext()) {
			assertEquals(outCursor.next().get(), lazyCursor.next().get());
			assertEquals(outCursor.get().get(), notLazyCursor.next().get());
		}
	}
}