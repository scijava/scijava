/*-
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2025 SciJava developers.
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

package org.scijava.common3;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Digests}.
 *
 * @author Curtis Rueden
 */
public class DigestsTest {

	private static final byte[] COFFEE_SHA1 = { -71, 2, 27, -126, -23, -70, -89,
		35, -65, -15, -108, 66, 72, 113, 29, -32, -12, -42, -49, 6 };

	private static final byte[] HELLO_WORLD_SHA1 = { 123, 80, 44, 58, 31, 72,
		-56, 96, -102, -30, 18, -51, -5, 99, -99, -18, 57, 103, 63, 94 };

	private static final String HELLO_WORLD_SHA1_HEX =
		"7b502c3a1f48c8609ae212cdfb639dee39673f5e";

	private static final String COFFEE_SHA1_HEX =
		"b9021b82e9baa723bff1944248711de0f4d6cf06";

	private static final String HELLO_WORLD_SHA1_BASE64 =
		"e1AsOh9IyGCa4hLN+2Od7jlnP14=";

	private static final String COFFEE_SHA1_BASE64 =
		"uQIbgum6pyO/8ZRCSHEd4PTWzwY=";

	@Test
	public void testBytesString() {
		final byte[] expected = { 72, 101, 108, 108, 111, 32, 119, 111, 114, 108,
			100 };
		assertArrayEquals(expected, Digests.bytes("Hello world"));
	}

	@Test
	public void testBytesInt() {
		assertArrayEquals(new byte[] { 0, -64, -1, -18 }, Digests.bytes(0xc0ffee));
	}

	@Test
	public void testString() {
		assertEquals("Hello world", Digests.string(Digests.bytes("Hello world")));
	}

	@Test
	public void testHex() {
		assertEquals("00c0ffee", Digests.hex(Digests.bytes(0xc0ffee)));
		assertEquals("deadbeef", Digests.hex(Digests.bytes(0xdeadbeef)));
		assertEquals("00000000", Digests.hex(Digests.bytes(0x00000000)));
		assertEquals("ffffffff", Digests.hex(Digests.bytes(0xffffffff)));
	}

	@Test
	public void testBase64() {
		assertEquals("AMD/7g==", Digests.base64(Digests.bytes(0xc0ffee)));
		assertEquals("3q2+7w==", Digests.base64(Digests.bytes(0xdeadbeef)));
		assertEquals("AAAAAA==", Digests.base64(Digests.bytes(0x00000000)));
		assertEquals("/////w==", Digests.base64(Digests.bytes(0xffffffff)));
	}

	@Test
	public void testHashString() {
		assertArrayEquals(new byte[] { -50, 89, -118, -92 }, Digests.hash(
			"Hello world"));
	}

	@Test
	public void testDigest() {
		final byte[] bytes = Digests.bytes(0xc0ffee);
		assertArrayEquals(Digests.sha1(bytes), Digests.digest("SHA-1", bytes));
		assertArrayEquals(Digests.md5(bytes), Digests.digest("MD5", bytes));
	}

	/**
	 * SciJava Common returned {@code null} for an unknown algorithm, which
	 * surfaced as a {@link NullPointerException} somewhere else entirely.
	 */
	@Test
	public void testDigestUnknownAlgorithm() {
		assertThrows(IllegalArgumentException.class, //
			() -> Digests.digest("NO-SUCH-ALGORITHM", Digests.bytes("x")));
	}

	@Test
	public void testBest() {
		assertArrayEquals(HELLO_WORLD_SHA1, Digests.best("Hello world"));
		assertArrayEquals(COFFEE_SHA1, Digests.best(Digests.bytes(0xc0ffee)));
	}

	@Test
	public void testBestHex() {
		assertEquals(HELLO_WORLD_SHA1_HEX, Digests.bestHex("Hello world"));
		assertEquals(COFFEE_SHA1_HEX, Digests.bestHex(Digests.bytes(0xc0ffee)));
	}

	@Test
	public void testBestBase64() {
		assertEquals(HELLO_WORLD_SHA1_BASE64, Digests.bestBase64("Hello world"));
		assertEquals(COFFEE_SHA1_BASE64, Digests.bestBase64(Digests.bytes(
			0xc0ffee)));
	}
}
