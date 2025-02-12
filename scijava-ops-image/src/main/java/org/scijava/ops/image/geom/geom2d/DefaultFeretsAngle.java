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

import net.imglib2.RealLocalizable;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;

import org.scijava.function.Computers;

/**
 * Generic implementation of {@code geom.feretsAngle}.
 *
 * @author Tim-Oliver Buchholz, University of Konstanz
 * @implNote op names='geom.feretsAngle', label='Geometric (2D): Ferets Angle'
 */
public class DefaultFeretsAngle implements
	Computers.Arity1<Pair<RealLocalizable, RealLocalizable>, DoubleType>
{

	/**
	 * TODO
	 *
	 * @param points
	 * @param feretsAngle
	 */
	@Override
	public void compute(final Pair<RealLocalizable, RealLocalizable> points,
		final DoubleType feretsAngle)
	{

		final var p1 = points.getA();
		final var p2 = points.getB();

		final var degree = Math.atan2(p2.getDoublePosition(1) - p1
			.getDoublePosition(1), p2.getDoublePosition(0) - p1.getDoublePosition(
				0)) * (180.0 / Math.PI);

		feretsAngle.set(degree % 180);
	}

}
