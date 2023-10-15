/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2016 - 2023 ImageJ2 developers.
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

package net.imagej.mesh2;

import net.imglib2.RealLocalizable;

/**
 * One vertex of a {@link Vertices} collection.
 *
 * @author Curtis Rueden
 * @see Vertices
 */
public interface Vertex extends RealLocalizable {

	/**
	 * The mesh to which the vertex belongs.
	 * 
	 * @see Mesh#vertices()
	 */
	Mesh mesh();

	/** Index into the mesh's list of vertices. */
	long index();

	/** X position of vertex, as a float. */
	default float xf() {
		return mesh().vertices().xf(index());
	}

	/** Y position of vertex, as a float. */
	default float yf() {
		return mesh().vertices().yf(index());
	}

	/** Z position of vertex, as a float. */
	default float zf() {
		return mesh().vertices().zf(index());
	}

	/** X coordinate of vertex normal, as a float. */
	default float nxf() {
		return mesh().vertices().nxf(index());
	}

	/** Y coordinate of vertex normal, as a float. */
	default float nyf() {
		return mesh().vertices().nyf(index());
	}

	/** Z coordinate of vertex normal, as a float. */
	default float nzf() {
		return mesh().vertices().nzf(index());
	}

	/** U value of vertex texture coordinate, as a float. */
	default float uf() {
		return mesh().vertices().uf(index());
	}

	/** V value of vertex texture coordinate, as a float. */
	default float vf() {
		return mesh().vertices().vf(index());
	}

	/** X position of vertex, as a double. */
	default double x() {
		return mesh().vertices().x(index());
	}

	/** Y position of vertex, as a double. */
	default double y() {
		return mesh().vertices().y(index());
	}

	/** Z position of vertex, as a double. */
	default double z() {
		return mesh().vertices().z(index());
	}

	/** X coordinate of vertex normal, as a double. */
	default double nx() {
		return mesh().vertices().nx(index());
	}

	/** Y coordinate of vertex normal, as a double. */
	default double ny() {
		return mesh().vertices().ny(index());
	}

	/** Z coordinate of vertex normal, as a double. */
	default double nz() {
		return mesh().vertices().nz(index());
	}

	/** U value of vertex texture coordinate, as a double. */
	default double u() {
		return mesh().vertices().u(index());
	}

	/** V value of vertex texture coordinate, as a double. */
	default double v() {
		return mesh().vertices().v(index());
	}

	// -- RealLocalizable methods --

	@Override
	default void localize(final float[] position) {
		position[0] = xf();
		position[1] = yf();
		position[2] = zf();
	}

	@Override
	default void localize(final double[] position) {
		position[0] = x();
		position[1] = y();
		position[2] = z();
	}

	@Override
	default float getFloatPosition(final int d) {
		switch (d) {
			case 0:
				return xf();
			case 1:
				return yf();
			case 2:
				return zf();
		}
		throw new IndexOutOfBoundsException("" + d);
	}

	@Override
	default double getDoublePosition(final int d) {
		switch (d) {
			case 0:
				return x();
			case 1:
				return y();
			case 2:
				return z();
		}
		throw new IndexOutOfBoundsException("" + d);
	}

	// -- EuclideanSpace methods --

	@Override
	default int numDimensions() {
		return 3;
	}
}
