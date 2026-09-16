/*
 * #%L
 * Locations and data handles: a uniform way to address and read bytes.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.io3;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ByteBufferByteBank}, in particular that an off-heap bank stays
 * off-heap - the reason to choose it over {@link ByteArrayByteBank}, since
 * direct bytes can reach native code and shared memory without a copy.
 *
 * @author Curtis Rueden
 */
public class ByteBufferByteBankTest {

	@Test
	public void testReadWrite() {
		final ByteBufferByteBank bank = new ByteBufferByteBank();
		final byte[] data = { 1, 2, 3, 4, 5 };
		bank.setBytes(0, data, 0, data.length);
		assertEquals(data.length, bank.size());
		final byte[] read = new byte[data.length];
		bank.getBytes(0, read);
		assertArrayEquals(data, read);
	}

	@Test
	public void testDirectIsOffHeap() {
		assertTrue(ByteBufferByteBank.direct().isDirect());
		assertTrue(ByteBufferByteBank.direct(64).isDirect());
	}

	/**
	 * Growing a direct bank must keep it direct. SciJava Common reallocated
	 * with ByteBuffer.allocate here, quietly moving the bytes onto the heap.
	 */
	@Test
	public void testDirectStaysDirectWhenGrown() {
		final ByteBufferByteBank bank = ByteBufferByteBank.direct(8);
		final byte[] data = new byte[1024];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte) i;

		// NB: far beyond the initial capacity, so the bank must grow.
		bank.setBytes(0, data, 0, data.length);
		assertEquals(data.length, bank.size());

		final byte[] read = new byte[data.length];
		bank.getBytes(0, read);
		assertArrayEquals(data, read);
		assertTrue(bank.isDirect(), "growing moved the bank back onto the heap");
	}

	@Test
	public void testHeapBankIsNotDirect() {
		final ByteBufferByteBank bank = new ByteBufferByteBank(8);
		bank.setBytes(0, new byte[1024], 0, 1024);
		assertTrue(!bank.isDirect());
	}

	@Test
	public void testCustomProvider() {
		final ByteBufferByteBank bank = new ByteBufferByteBank(
			ByteBuffer::allocateDirect, 16);
		bank.setBytes(0, new byte[] { 9 }, 0, 1);
		assertEquals(9, bank.getByte(0));
		assertTrue(bank.isDirect());
	}
}
