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

package net.imagej.ops.convert;

import static org.junit.Assert.assertEquals;

import net.imagej.ops.AbstractOpTest;
import net.imagej.ops.convert.imageType.ConvertIIs;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.ShortType;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link ConvertIIs} + {@link RealTypeConverter} ops.
 * 
 * @author Curtis Rueden
 */
public class ConvertIIsTest extends AbstractOpTest {

	private IterableInterval<ShortType> in;
	private Img<ByteType> out;

	@Before
	public void createImages() {
		final FinalDimensions dims = FinalDimensions.wrap(new long[] {10, 10});
		in = (IterableInterval<ShortType>) ops.run("create.img", dims, new ShortType());
		addNoise(in);
		out = (Img<ByteType>) ops.run("create.img", dims, new ByteType());
	}

	@Test
	public void testClip() {
		ops.run("convert.clip", in, out);

		final Cursor<ShortType> c = in.localizingCursor();
		final RandomAccess<ByteType> ra = out.randomAccess();
		while (c.hasNext()) {
			final short value = c.next().get();
			ra.setPosition(c);
			assertEquals(clip(value), ra.get().get());
		}
	}

	@Test
	public void testCopy() {
		ops.run("convert.copy", in, out);

		final Cursor<ShortType> c = in.localizingCursor();
		final RandomAccess<ByteType> ra = out.randomAccess();
		while (c.hasNext()) {
			final short value = c.next().get();
			ra.setPosition(c);
			assertEquals(copy(value), ra.get().get());
		}
	}
	
	// -- Helper methods --

	private void addNoise(final IterableInterval<ShortType> image) {
		IterableInterval<ShortType> copy = (IterableInterval<ShortType>) ops.run("copy.img", image);
		ops.run("filter.addNoise", copy, -32768., 32767., 10000., image);
	}

	private byte clip(final short value) {
		if (value < -128) return -128;
		if (value > 127) return 127;
		return (byte) value;
	}

	private byte copy(final short value) {
		return (byte) value;
	}

}
