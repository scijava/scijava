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

package org.scijava.ops.image.geom.compactness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.imglib2.roi.geom.real.DefaultWritablePolygon2D;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DefaultCompactness}.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class CompactnessTest extends AbstractOpTest {

	private double feature(final String name, final Polygon2D p) {
		return ops.op(name).input(p).outType(DoubleType.class).apply().get();
	}

	private static Polygon2D ellipse(final double a, final double b) {
		final var n = 360;
		final var x = new double[n];
		final var y = new double[n];
		for (var i = 0; i < n; i++) {
			x[i] = 50 + a * Math.cos(2 * Math.PI * i / n);
			y[i] = 50 + b * Math.sin(2 * Math.PI * i / n);
		}
		return new DefaultWritablePolygon2D(x, y);
	}

	@Test
	public void testCircle() {
		assertEquals(1, feature("geom.compactness", ellipse(20, 20)), 1e-3);
	}

	@Test
	public void testEllipse() {
		// The more elongated the ellipse, the lower its compactness.
		final var c2 = feature("geom.compactness", ellipse(20, 10));
		final var c4 = feature("geom.compactness", ellipse(40, 10));
		assertTrue(c2 < 1);
		assertTrue(c4 < c2);
	}

	@Test
	public void testSquareRootOfRoundness() {
		final var shape = new DefaultWritablePolygon2D(new double[] { 0, 30, 30,
			10, 0 }, new double[] { 0, 0, 8, 20, 12 });
		final var compactness = feature("geom.compactness", shape);
		assertTrue(compactness < 1);
		assertEquals(Math.sqrt(feature("geom.roundness", shape)), compactness,
			1e-12);
	}
}
