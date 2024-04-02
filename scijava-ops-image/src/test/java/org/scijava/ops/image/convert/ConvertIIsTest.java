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

package org.scijava.ops.image.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.scijava.ops.image.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.ShortType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Tests {@code ConvertIIs} + {@link RealTypeConverter} ops.
 *
 * @author Curtis Rueden
 */
public class ConvertIIsTest extends AbstractOpTest {

	private IterableInterval<ShortType> in;
	private Img<ByteType> out;

	@BeforeEach
	public void createImages() {
		final FinalDimensions dims = FinalDimensions.wrap(new long[] { 10, 10 });
		in = ops.op("create.img").arity2().input(dims, new ShortType()).outType(
			new Nil<IterableInterval<ShortType>>()
			{}).apply();
		addNoise(in);
		out = ops.op("create.img").arity2().input(dims, new ByteType()).outType(
			new Nil<Img<ByteType>>()
			{}).apply();
	}

	@Test
	public void testClip() {
		ops.op("convert.clip").arity1().input(in).output(out).compute();

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
		ops.op("convert.copy").arity1().input(in).output(out).compute();

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
		IterableInterval<ShortType> copy = ops.op("copy.img").arity1().input(image)
			.outType(new Nil<IterableInterval<ShortType>>()
			{}).apply();
		ops.op("filter.addNoise").arity4().input(copy, -32768., 32767., 10000.)
			.output(image).compute();
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
