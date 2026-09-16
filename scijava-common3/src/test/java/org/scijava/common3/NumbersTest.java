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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link Numbers}.
 *
 * @author Curtis Rueden
 */
public class NumbersTest {

	@Test
	public void testMinimum() {
		assertEquals(Byte.MIN_VALUE, Numbers.minimum(Byte.class));
		assertEquals(Short.MIN_VALUE, Numbers.minimum(Short.class));
		assertEquals(Integer.MIN_VALUE, Numbers.minimum(Integer.class));
		assertEquals(Long.MIN_VALUE, Numbers.minimum(Long.class));
		assertEquals(-Float.MAX_VALUE, Numbers.minimum(Float.class));
		assertEquals(-Double.MAX_VALUE, Numbers.minimum(Double.class));
		// NB: Number's minimum is the smallest of all the above -> Double.
		assertEquals(-Double.MAX_VALUE, Numbers.minimum(Number.class));
		assertNull(Numbers.minimum(String.class));
	}

	@Test
	public void testMaximum() {
		assertEquals(Byte.MAX_VALUE, Numbers.maximum(Byte.class));
		assertEquals(Short.MAX_VALUE, Numbers.maximum(Short.class));
		assertEquals(Integer.MAX_VALUE, Numbers.maximum(Integer.class));
		assertEquals(Long.MAX_VALUE, Numbers.maximum(Long.class));
		assertEquals(Float.MAX_VALUE, Numbers.maximum(Float.class));
		assertEquals(Double.MAX_VALUE, Numbers.maximum(Double.class));
		// NB: Number's maximum is the largest of all the above -> Double.
		assertEquals(Double.MAX_VALUE, Numbers.maximum(Number.class));
		assertNull(Numbers.maximum(String.class));
	}

	@Test
	public void testToNumber() {
		assertEquals(5, Numbers.toNumber("5", Integer.class));
		assertEquals(5, Numbers.toNumber("5", int.class));
		assertEquals(5.5, Numbers.toNumber("5.5", Double.class));
		assertEquals((byte) 5, Numbers.toNumber(5.9, Byte.class));
		assertEquals(5L, Numbers.toNumber("5", Long.class));
		assertEquals(BigInteger.valueOf(5), Numbers.toNumber("5",
			BigInteger.class));
		assertEquals(BigDecimal.valueOf(5.5), Numbers.toNumber(5.5,
			BigDecimal.class));
	}

	@Test
	public void testToNumberInvalid() {
		assertNull(Numbers.toNumber(null, Integer.class));
		assertNull(Numbers.toNumber("", Integer.class));
		assertNull(Numbers.toNumber("not a number", Integer.class));
		assertNull(Numbers.toNumber("5", String.class));
	}

	@Test
	public void testBigDecimal() {
		// NB: routing a long through doubleValue() would lose accuracy.
		final long big = 9007199254740993L; // 2^53 + 1
		assertEquals(BigDecimal.valueOf(big), Numbers.bigDecimal(big));
		assertEquals(new BigDecimal(BigInteger.TEN), Numbers.bigDecimal(
			BigInteger.TEN));
	}

	@Test
	public void testBigInteger() {
		assertEquals(BigInteger.valueOf(5), Numbers.bigInteger(5));
		assertEquals(BigInteger.TEN, Numbers.bigInteger(BigInteger.TEN));
	}

	@Test
	public void testDefaultValue() {
		assertEquals(3, Numbers.defaultValue(3, 7, Integer.class));
		assertEquals(7, Numbers.defaultValue(null, 7, Integer.class));
		assertEquals(0, Numbers.defaultValue(null, null, Integer.class));
	}

	@Test
	public void testClamp() {
		assertEquals(5, Numbers.clamp(Integer.class, 5, 0, 10));
		assertEquals(0, Numbers.clamp(Integer.class, -1, 0, 10));
		assertEquals(10, Numbers.clamp(Integer.class, 11, 0, 10));
		assertEquals(0, Numbers.clamp(Integer.class, null, 0, 10));
	}

	@Test
	public void testSafeMultiply() {
		assertEquals(24, Numbers.safeMultiply32(2, 3, 4));
		assertEquals(1, Numbers.safeMultiply64());
		assertEquals(4611686018427387904L, Numbers.safeMultiply64(1L << 31,
			1L << 31));
		assertThrows(ArithmeticException.class, //
			() -> Numbers.safeMultiply32(1L << 31, 2));
		assertThrows(ArithmeticException.class, //
			() -> Numbers.safeMultiply64(1L << 62, 4));
	}
}
