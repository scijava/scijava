/*
 * #%L
 * Shared test utilities for SciJava projects.
 * %%
 * Copyright (C) 2020 - 2024 SciJava developers.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;

/**
 * Assertions to be used when a relative delta should be used to compare the
 * actual and expected values.
 *
 * @author Gabriel Selzer
 * @see Assertions
 */
public final class AssertClose {

	private AssertClose() {
		// Prevent instantiation of static utility class
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal within
	 * the non-negative delta {@code |expected| * 10^deltaExp}.
	 *
	 * @param expected
	 * @param actual
	 * @param order - the order of magnitude of the delta
	 */
	public static void assertCloseEnough(double expected, double actual,
		int order)
	{
		double significand = Math.abs(expected);
		double exponential = Math.pow(10., order);
		double delta = significand * exponential;
		assertEquals(expected, actual, delta);
	}

	/**
	 * <em>Assert</em> that {@code expected} and {@code actual} are equal within
	 * the non-negative delta {@code |expected| * 10^deltaExp}.
	 *
	 * @param expected
	 * @param actual
	 * @param order - the order of magnitude of the delta
	 * @param message
	 */
	public static void assertCloseEnough(double expected, double actual,
		int order, String message)
	{
		double significand = Math.abs(expected);
		double exponential = Math.pow(10., order);
		double delta = significand * exponential;
		assertEquals(expected, actual, delta, message);
	}

}
