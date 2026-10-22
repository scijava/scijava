/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.util;

import java.util.Arrays;

/**
 * Utility methods for pixel spacing (calibration) parameters, shared by the
 * ops of this library.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class Calibrations {

	private Calibrations() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Validates an optional pixel spacing parameter, defaulting to 1 along
	 * every dimension.
	 *
	 * @param spacing the pixel spacing along each dimension, or null
	 * @param n the number of dimensions
	 * @param name the name of the parameter, for error messages
	 * @return {@code spacing}, or an array of {@code n} ones if it is null
	 * @throws IllegalArgumentException if {@code spacing} does not have
	 *           {@code n} values, or if any of them is not positive and finite
	 */
	public static double[] spacing(final double[] spacing, final int n,
		final String name)
	{
		if (spacing == null) {
			final var ones = new double[n];
			Arrays.fill(ones, 1);
			return ones;
		}
		if (spacing.length != n) {
			throw new IllegalArgumentException("Expected " + n + " " + name +
				" values but got " + spacing.length);
		}
		for (final var s : spacing) {
			if (!(s > 0) || Double.isInfinite(s)) {
				throw new IllegalArgumentException("The " + name +
					" values must be positive and finite: " + Arrays.toString(spacing));
			}
		}
		return spacing;
	}
}
