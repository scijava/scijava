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

package org.scijava.ops.image.geom.feretAspectRatio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imglib2.roi.geom.real.DefaultWritablePolygon2D;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

/**
 * Tests {@link DefaultFeretAspectRatio}.
 *
 * @author Curtis Rueden
 */
public class FeretAspectRatioTest extends AbstractOpTest {

	private double ratio(final Polygon2D p) {
		return ops.op("geom.feretAspectRatio").input(p).outType(DoubleType.class)
			.apply().get();
	}

	@Test
	public void testRectangle() {
		// The maximum Feret diameter is the diagonal, the minimum the width.
		final var rectangle = new DefaultWritablePolygon2D(new double[] { 0, 10, 10,
			0 }, new double[] { 0, 0, 4, 4 });
		assertEquals(Math.sqrt(116) / 4, ratio(rectangle), 1e-9);
	}

	@Test
	public void testRegularPolygon() {
		// A regular polygon with many vertices is nearly a circle.
		final var n = 720;
		final var x = new double[n];
		final var y = new double[n];
		for (var i = 0; i < n; i++) {
			x[i] = 30 * Math.cos(2 * Math.PI * i / n);
			y[i] = 30 * Math.sin(2 * Math.PI * i / n);
		}
		assertEquals(1, ratio(new DefaultWritablePolygon2D(x, y)), 1e-3);
	}
}
