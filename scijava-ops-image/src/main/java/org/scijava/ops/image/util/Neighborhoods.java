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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ConstantUtils;

/**
 * Utility methods for neighborhoods on flattened pixel grids, shared by the
 * morphology and segmentation ops of this library.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public final class Neighborhoods {

	private Neighborhoods() {
		// Prevent instantiation of static utility class.
	}

	/**
	 * Lists the offsets of a shape around the origin, made symmetric and
	 * without the origin itself.
	 *
	 * @param shape the neighborhood shape, or null for full connectivity (a
	 *          {@link RectangleShape} of span 1, i.e. 8-connectivity in 2D and
	 *          26-connectivity in 3D)
	 * @param n the number of dimensions
	 * @return the offsets, as arrays of length {@code n}; the negation of each
	 *         offset is included too
	 */
	public static int[][] neighborOffsets(final Shape shape, final int n) {
		final var s = shape == null ? new RectangleShape(1, false) : shape;
		final var source = ConstantUtils.constantRandomAccessible(new DoubleType(),
			n);
		final var ra = s.neighborhoodsRandomAccessible(source).randomAccess();
		ra.setPosition(new long[n]);
		final var set = new LinkedHashSet<List<Integer>>();
		final var c = ra.get().localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			final var o = new ArrayList<Integer>(n);
			final var neg = new ArrayList<Integer>(n);
			var zero = true;
			for (var d = 0; d < n; d++) {
				final var v = c.getIntPosition(d);
				o.add(v);
				neg.add(-v);
				if (v != 0) zero = false;
			}
			if (zero) continue;
			set.add(o);
			set.add(neg);
		}
		final var result = new int[set.size()][];
		var k = 0;
		for (final var o : set) {
			result[k++] = o.stream().mapToInt(Integer::intValue).toArray();
		}
		return result;
	}
}
