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

package org.scijava.ops.image.geom;

import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.spi.Op;

/**
 * This Op computes the center of gravity of a {@link IterableRegion} (Label).
 *
 * @author Daniel Seebacher (University of Konstanz)
 * @implNote op names='geom.centerOfGravity'
 */
public class DefaultCenterOfGravity<T extends RealType<T>> implements
	Function<IterableInterval<T>, RealLocalizable>
{

	/**
	 * TODO
	 *
	 * @param input the input {@link IterableInterval}
	 * @return the center of gravity, as a {@link RealLocalizable}
	 */
	@Override
	public RealLocalizable apply(final IterableInterval<T> input) {
		final var numDimensions = input.numDimensions();

		final var output = new double[numDimensions];
		final var intensityValues = new double[numDimensions];

		final var c = input.localizingCursor();
		while (c.hasNext()) {
			c.fwd();
			for (var i = 0; i < output.length; i++) {
				output[i] += c.getDoublePosition(i) * c.get().getRealDouble();
				intensityValues[i] += c.get().getRealDouble();
			}
		}

		for (var i = 0; i < output.length; i++) {
			output[i] = output[i] / intensityValues[i];
		}

		return new RealPoint(output);
	}

}
