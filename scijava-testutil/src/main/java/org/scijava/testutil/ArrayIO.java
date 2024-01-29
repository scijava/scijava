/*-
 * #%L
 * Shared test utilities for SciJava projects.
 * %%
 * Copyright (C) 2020 - 2023 SciJava developers.
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

package org.scijava.testutil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * Utility functions for reading and writing arrays simply.
 *
 * @author Curtis Rueden
 */
public final class ArrayIO {

	private ArrayIO() {
		// NB: Prevent instantiation of utility class.
	}

	public static byte[] bytes(final InputStream is) throws IOException {
		return is.readAllBytes();
	}

	public static double[] doubles(final InputStream is) throws IOException {
		final ByteBuffer bb = ByteBuffer.wrap(is.readAllBytes());
		final DoubleBuffer view = bb.asDoubleBuffer();
		return DoubleBuffer.allocate(view.limit()).put(view).array();
	}

	public static float[] floats(final InputStream is) throws IOException {
		final ByteBuffer bb = ByteBuffer.wrap(is.readAllBytes());
		final FloatBuffer view = bb.asFloatBuffer();
		return FloatBuffer.allocate(view.limit()).put(view).array();
	}

	public static int[] ints(final InputStream is) throws IOException {
		final ByteBuffer bb = ByteBuffer.wrap(is.readAllBytes());
		final IntBuffer view = bb.asIntBuffer();
		return IntBuffer.allocate(view.limit()).put(view).array();
	}

	public static long[] longs(final InputStream is) throws IOException {
		final ByteBuffer bb = ByteBuffer.wrap(is.readAllBytes());
		final LongBuffer view = bb.asLongBuffer();
		return LongBuffer.allocate(view.limit()).put(view).array();
	}

	public static short[] shorts(final InputStream is) throws IOException {
		final ByteBuffer bb = ByteBuffer.wrap(is.readAllBytes());
		final ShortBuffer view = bb.asShortBuffer();
		return ShortBuffer.allocate(view.limit()).put(view).array();
	}

	public static void writeBytes(final byte[] data, final OutputStream os)
		throws IOException
	{
		os.write(data);
	}

	public static void writeDoubles(final double[] data, final OutputStream os)
		throws IOException
	{
		final ByteBuffer bb = ByteBuffer.allocate(4 * data.length);
		bb.asDoubleBuffer().put(DoubleBuffer.wrap(data));
		os.write(bb.array());
	}

	public static void writeFloats(final float[] data, final OutputStream os)
		throws IOException
	{
		final ByteBuffer bb = ByteBuffer.allocate(4 * data.length);
		bb.asFloatBuffer().put(FloatBuffer.wrap(data));
		os.write(bb.array());
	}

	public static void writeInts(final int[] data, final OutputStream os)
		throws IOException
	{
		final ByteBuffer bb = ByteBuffer.allocate(4 * data.length);
		bb.asIntBuffer().put(IntBuffer.wrap(data));
		os.write(bb.array());
	}

	public static void writeLongs(final long[] data, final OutputStream os)
		throws IOException
	{
		final ByteBuffer bb = ByteBuffer.allocate(8 * data.length);
		bb.asLongBuffer().put(LongBuffer.wrap(data));
		os.write(bb.array());
	}

	public static void writeShorts(final short[] data, final OutputStream os)
		throws IOException
	{
		final ByteBuffer bb = ByteBuffer.allocate(4 * data.length);
		bb.asShortBuffer().put(ShortBuffer.wrap(data));
		os.write(bb.array());
	}
}
