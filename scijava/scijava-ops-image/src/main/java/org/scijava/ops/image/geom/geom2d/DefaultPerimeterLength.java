/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

import java.util.List;

import org.scijava.ops.image.geom.GeomUtils;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;

/**
 * Generic implementation of {@code geom.boundarySize}.
 * 
 * @author Daniel Seebacher (University of Konstanz)
 * @implNote op names='geom.boundarySize', label='Geometric (2D): Perimeter'
 */
public class DefaultPerimeterLength implements Computers.Arity1<Polygon2D, DoubleType> {

	/**
	 * TODO
	 *
	 * @param input
	 * @param boundarySize
	 */
	@Override
	public void compute(final Polygon2D input, final DoubleType output) {
		double perimeter = 0;
		final List<? extends RealLocalizable> vertices = GeomUtils.vertices(input);
		final int size = vertices.size();
		for (int i = 0; i < size; i++) {
			final int nexti = (i + 1) % size;

			final double dx2 = vertices.get(nexti).getDoublePosition(0) - vertices.get(i).getDoublePosition(0);
			final double dy2 = vertices.get(nexti).getDoublePosition(1) - vertices.get(i).getDoublePosition(1);

			perimeter += Math.sqrt(dx2 * dx2 + dy2 * dy2);
		}

		output.set(perimeter);
	}

}
