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

package org.scijava.ops.image.geom.geom2d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.scijava.ops.image.geom.GeomUtils;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.DefaultWritablePolygon2D;
import net.imglib2.roi.geom.real.Polygon2D;

/**
 * Generic implementation of {@code geom.boundingBox}.
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @implNote op names='geom.boundingBox'
 */
public class DefaultBoundingBox implements Function<Polygon2D, Polygon2D> {

	/**
	 * TODO
	 *
	 * @param input
	 * @return the output
	 */
	@Override
	public Polygon2D apply(final Polygon2D input) {
        var min_x = Double.POSITIVE_INFINITY;
        var max_x = Double.NEGATIVE_INFINITY;
        var min_y = Double.POSITIVE_INFINITY;
        var max_y = Double.NEGATIVE_INFINITY;

		for (final var rl : GeomUtils.vertices(input)) {
			if (rl.getDoublePosition(0) < min_x) {
				min_x = rl.getDoublePosition(0);
			}
			if (rl.getDoublePosition(0) > max_x) {
				max_x = rl.getDoublePosition(0);
			}
			if (rl.getDoublePosition(1) < min_y) {
				min_y = rl.getDoublePosition(1);
			}
			if (rl.getDoublePosition(1) > max_y) {
				max_y = rl.getDoublePosition(1);
			}
		}

		final List<RealLocalizable> bounds = new ArrayList<>();

		bounds.add(new RealPoint(min_x, min_y));
		bounds.add(new RealPoint(min_x, max_y));
		bounds.add(new RealPoint(max_x, max_y));
		bounds.add(new RealPoint(max_x, min_y));

		return new DefaultWritablePolygon2D(bounds);
	}

}
