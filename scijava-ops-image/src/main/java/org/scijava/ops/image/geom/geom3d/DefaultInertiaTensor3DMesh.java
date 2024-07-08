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

package org.scijava.ops.image.geom.geom3d;

import java.util.function.Function;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Triangle;
import net.imglib2.RealLocalizable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;

/**
 * This Op computes the 2nd multi variate of a {@link Mesh} (Label).
 *
 * @author Tim-Oliver Buchholz (University of Konstanz)
 * @implNote op names='geom.secondMoment'
 */
public class DefaultInertiaTensor3DMesh implements Function<Mesh, RealMatrix> {

	@OpDependency(name = "geom.centroid")
	private Function<Mesh, RealLocalizable> centroid;

	/**
	 * TODO
	 *
	 * @param input the {@link Mesh}
	 * @return the 2nd multi variate, stored within a {@link RealMatrix}
	 *         inertiaTensor
	 */
	@Override
	public RealMatrix apply(final Mesh input) {
		// ensure validity of inputs
		if (input == null) throw new IllegalArgumentException("Input is null!");
		final var cent = centroid.apply(input);
		final var originX = cent.getDoublePosition(0);
		final var originY = cent.getDoublePosition(1);
		final var originZ = cent.getDoublePosition(2);

        var tensor = new BlockRealMatrix(3, 3);
		for (final var triangle : input.triangles()) {
			final var x1 = triangle.v0x() - originX;
			final var y1 = triangle.v0y() - originY;
			final var z1 = triangle.v0z() - originZ;
			final var x2 = triangle.v1x() - originX;
			final var y2 = triangle.v1y() - originY;
			final var z2 = triangle.v1z() - originZ;
			final var x3 = triangle.v2x() - originX;
			final var y3 = triangle.v2y() - originY;
			final var z3 = triangle.v2z() - originZ;
			tensor = tensor.add(//
				tetrahedronInertiaTensor(x1, y1, z1, x2, y2, z2, x3, y3, z3));
		}

		return tensor;
	}

	/**
	 * The computations are based on <a href=
	 * "http://docsdrive.com/pdfs/sciencepublications/jmssp/2005/8-11.pdf">this
	 * paper</a>.
	 * <p>
	 * Note: In the paper b' and c' are swapped.
	 * </p>
	 *
	 * @param x1 X coordinate of first triangle vertex
	 * @param y1 Y coordinate of first triangle vertex
	 * @param z1 Z coordinate of first triangle vertex
	 * @param x2 X coordinate of second triangle vertex
	 * @param y2 Y coordinate of second triangle vertex
	 * @param z2 Z coordinate of second triangle vertex
	 * @param x3 X coordinate of third triangle vertex
	 * @param y3 Y coordinate of third triangle vertex
	 * @param z3 Z coordinate of third triangle vertex
	 * @return inertia tensor of this tetrahedron
	 */
	private BlockRealMatrix tetrahedronInertiaTensor(//
		final double x1, final double y1, final double z1, //
		final double x2, final double y2, final double z2, //
		final double x3, final double y3, final double z3)
	{
		final var volume = tetrahedronVolume(new Vector3D(x1, y1, z1),
			new Vector3D(x2, y2, z2), new Vector3D(x3, y3, z3));

		final var a = 6 * volume * (y1 * y1 + y1 * y2 + y2 * y2 + y1 * y3 + y2 *
			y3 + y3 * y3 + z1 * z1 + z1 * z2 + z2 * z2 + z1 * z3 + z2 * z3 + z3 *
				z3) / 60.0;
		final var b = 6 * volume * (x1 * x1 + x1 * x2 + x2 * x2 + x1 * x3 + x2 *
			x3 + x3 * x3 + z1 * z1 + z1 * z2 + z2 * z2 + z1 * z3 + z2 * z3 + z3 *
				z3) / 60.0;
		final var c = 6 * volume * (x1 * x1 + x1 * x2 + x2 * x2 + x1 * x3 + x2 *
			x3 + x3 * x3 + y1 * y1 + y1 * y2 + y2 * y2 + y1 * y3 + y2 * y3 + y3 *
				y3) / 60.0;
		final var aa = 6 * volume * (2 * y1 * z1 + y2 * z1 + y3 * z1 + y1 * z2 +
			2 * y2 * z2 + y3 * z2 + y1 * z3 + y2 * z3 + 2 * y3 * z3) / 120.0;

		final var bb = 6 * volume * (2 * x1 * y1 + x2 * y1 + x3 * y1 + x1 * y2 +
			2 * x2 * y2 + x3 * y2 + x1 * y3 + x2 * y3 + 2 * x3 * y3) / 120.0;

		final var cc = 6 * volume * (2 * x1 * z1 + x2 * z1 + x3 * z1 + x1 * z2 +
			2 * x2 * z2 + x3 * z2 + x1 * z3 + x2 * z3 + 2 * x3 * z3) / 120.0;

		final var t = new BlockRealMatrix(3, 3);
		t.setRow(0, new double[] { a, -bb, -cc });
		t.setRow(1, new double[] { -bb, b, -aa });
		t.setRow(2, new double[] { -cc, -aa, c });

		return t;
	}

	private double tetrahedronVolume(Vector3D a, Vector3D b, Vector3D c) {
		// https://en.wikipedia.org/wiki/Tetrahedron#Volume
		return Math.abs(a.dotProduct(b.crossProduct(c))) / 6.0;
	}

}
