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

package org.scijava.ops.image.filter.tensor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link TensorUtils}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class TensorUtilsTest {

	@Test
	public void testSymmetricEigen() {
		final var rnd = new Random(7);
		for (var n = 1; n <= 4; n++) {
			final var a = new double[n][n];
			for (var r = 0; r < n; r++)
				for (var s = r; s < n; s++)
					a[r][s] = a[s][r] = rnd.nextGaussian();
			final var copy = new double[n][];
			for (var r = 0; r < n; r++)
				copy[r] = a[r].clone();
			final var vals = new double[n];
			final var vecs = new double[n][n];
			TensorUtils.symmetricEigen(copy, vals, vecs);
			for (var k = 0; k < n; k++) {
				for (var r = 0; r < n; r++) {
					var av = 0.0;
					for (var s = 0; s < n; s++)
						av += a[r][s] * vecs[s][k];
					assertEquals(vals[k] * vecs[r][k], av, 1e-10);
				}
			}
		}
	}
}
