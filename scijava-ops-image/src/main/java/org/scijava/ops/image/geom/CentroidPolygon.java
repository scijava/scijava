/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.geom;

import java.util.function.Function;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ops.spi.OpDependency;

/**
 * Generic implementation of {@code geom.centroid}.
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @implNote op names='geom.centroid', label='Geometric: Center of Gravity'
 */
public class CentroidPolygon implements Function<Polygon2D, RealLocalizable> {

	@OpDependency(name = "geom.size")
	private Function<Polygon2D, DoubleType> sizeFunc;

	/**
	 * TODO
	 *
	 * @param input
	 * @return the centroid, as a {@link RealLocalizable}
	 */
	@Override
	public RealLocalizable apply(final Polygon2D input) {

		double area = sizeFunc.apply(input).get();
		double cx = 0;
		double cy = 0;
		for (int i = 0; i < input.numVertices(); i++) {
			RealLocalizable p0 = input.vertex(i);
			RealLocalizable p1 = input.vertex((i + 1) % input.numVertices());

			double p0_x = p0.getDoublePosition(0);
			double p0_y = p0.getDoublePosition(1);
			double p1_x = p1.getDoublePosition(0);
			double p1_y = p1.getDoublePosition(1);

			cx += (p0_x + p1_x) * (p0_x * p1_y - p1_x * p0_y);
			cy += (p0_y + p1_y) * (p0_x * p1_y - p1_x * p0_y);
		}

		return new RealPoint(cx / (area * 6), cy / (area * 6));
	}

}
