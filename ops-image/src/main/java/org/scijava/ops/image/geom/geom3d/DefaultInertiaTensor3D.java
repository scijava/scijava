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

package org.scijava.ops.image.geom.geom3d;

import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.BooleanType;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;

/**
 * This Op computes the 2nd multi variate of a {@link IterableRegion} (Label).
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 * @param <B> BooleanType
 * @implNote op names='geom.secondMoment'
 */
public class DefaultInertiaTensor3D<B extends BooleanType<B>> implements
	Function<IterableRegion<B>, RealMatrix>
{

	@OpDependency(name = "geom.centroid")
	private Function<IterableRegion<B>, RealLocalizable> centroid;

	/**
	 * TODO
	 *
	 * @param input an {@link IterableRegion} input
	 * @return the second multi variate
	 */
	@Override
	public RealMatrix apply(final IterableRegion<B> input) {
		// ensure validity of inputs
		if (input.numDimensions() != 3) throw new IllegalArgumentException(
			"Only three-dimensional inputs allowed!");

		final var output = new BlockRealMatrix(3, 3);
        var c = input.inside().localizingCursor();
        var pos = new double[3];
        var computedCentroid = new double[3];
		centroid.apply(input).localize(computedCentroid);
		final var mX = computedCentroid[0];
		final var mY = computedCentroid[1];
		final var mZ = computedCentroid[2];
		while (c.hasNext()) {
			c.fwd();
			c.localize(pos);
			output.setEntry(0, 0, output.getEntry(0, 0) + (pos[0] - mX) * (pos[0] -
				mX));
			output.setEntry(1, 1, output.getEntry(1, 1) + (pos[1] - mX) * (pos[1] -
				mY));
			output.setEntry(2, 2, output.getEntry(2, 2) + (pos[2] - mX) * (pos[2] -
				mZ));
			output.setEntry(0, 1, output.getEntry(0, 1) + (pos[0] - mY) * (pos[1] -
				mY));
			output.setEntry(1, 0, output.getEntry(0, 1));
			output.setEntry(0, 2, output.getEntry(0, 2) + (pos[0] - mY) * (pos[2] -
				mZ));
			output.setEntry(2, 0, output.getEntry(0, 2));
			output.setEntry(1, 2, output.getEntry(1, 2) + (pos[1] - mZ) * (pos[2] -
				mZ));
			output.setEntry(2, 1, output.getEntry(1, 2));
		}

		final double size = input.inside().size();
		output.setEntry(0, 0, output.getEntry(0, 0) / size);
		output.setEntry(0, 1, output.getEntry(0, 1) / size);
		output.setEntry(0, 2, output.getEntry(0, 2) / size);
		output.setEntry(1, 0, output.getEntry(1, 0) / size);
		output.setEntry(1, 1, output.getEntry(1, 1) / size);
		output.setEntry(1, 2, output.getEntry(1, 2) / size);
		output.setEntry(2, 0, output.getEntry(2, 0) / size);
		output.setEntry(2, 1, output.getEntry(2, 1) / size);
		output.setEntry(2, 2, output.getEntry(2, 2) / size);

		return output;
	}

}
