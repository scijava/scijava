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

/**
 * One triangle of a {@link Triangles} collection.
 *
 * @author Curtis Rueden
 * @see Triangles
 */
public interface Triangle {

	/**
	 * The mesh to which the triangle belongs.
	 * 
	 * @see Mesh#triangles()
	 */
	Mesh mesh();

	/** Index into the mesh's list of triangles. */
	long index();

	/** <strong>Index</strong> of first vertex in the triangle. */
	default long vertex0() {
		return mesh().triangles().vertex0(index());
	}

	/** <strong>Index</strong> of second vertex in the triangle. */
	default long vertex1() {
		return mesh().triangles().vertex1(index());
	}

	/** <strong>Index</strong> of third vertex in the triangle. */
	default long vertex2() {
		return mesh().triangles().vertex2(index());
	}

	/** X coordinate of triangle's first vertex, as a float. */
	default float v0xf() {
		final long vIndex = mesh().triangles().vertex0(index());
		return mesh().vertices().xf(vIndex);
	}

	/** Y coordinate of triangle's first vertex, as a float. */
	default float v0yf() {
		final long vIndex = mesh().triangles().vertex0(index());
		return mesh().vertices().yf(vIndex);
	}

	/** Z coordinate of triangle's first vertex, as a float. */
	default float v0zf() {
		final long vIndex = mesh().triangles().vertex0(index());
		return mesh().vertices().zf(vIndex);
	}

	/** X coordinate of triangle's second vertex, as a float. */
	default float v1xf() {
		final long vIndex = mesh().triangles().vertex1(index());
		return mesh().vertices().xf(vIndex);
	}

	/** Y coordinate of triangle's second vertex, as a float. */
	default float v1yf() {
		final long vIndex = mesh().triangles().vertex1(index());
		return mesh().vertices().yf(vIndex);
	}

	/** Z coordinate of triangle's second vertex, as a float. */
	default float v1zf() {
		final long vIndex = mesh().triangles().vertex1(index());
		return mesh().vertices().zf(vIndex);
	}

	/** X coordinate of triangle's third vertex, as a float. */
	default float v2xf() {
		final long vIndex = mesh().triangles().vertex2(index());
		return mesh().vertices().xf(vIndex);
	}

	/** Y coordinate of triangle's third vertex, as a float. */
	default float v2yf() {
		final long vIndex = mesh().triangles().vertex2(index());
		return mesh().vertices().yf(vIndex);
	}

	/** Z coordinate of triangle's third vertex, as a float. */
	default float v2zf() {
		final long vIndex = mesh().triangles().vertex2(index());
		return mesh().vertices().zf(vIndex);
	}

	/** X coordinate of triangle's normal, as a float. */
	default float nxf() {
		return mesh().triangles().nxf(index());
	}

	/** Y coordinate of triangle's normal, as a float. */
	default float nyf() {
		return mesh().triangles().nyf(index());
	}

	/** Z coordinate of triangle's normal, as a float. */
	default float nzf() {
		return mesh().triangles().nzf(index());
	}

	/** X coordinate of triangle's first vertex, as a double. */
	default double v0x() {
		final long vIndex = mesh().triangles().vertex0(index());
		return mesh().vertices().x(vIndex);
	}

	/** Y coordinate of triangle's first vertex, as a double. */
	default double v0y() {
		final long vIndex = mesh().triangles().vertex0(index());
		return mesh().vertices().y(vIndex);
	}

	/** Z coordinate of triangle's first vertex, as a double. */
	default double v0z() {
		final long vIndex = mesh().triangles().vertex0(index());
		return mesh().vertices().z(vIndex);
	}

	/** X coordinate of triangle's second vertex, as a double. */
	default double v1x() {
		final long vIndex = mesh().triangles().vertex1(index());
		return mesh().vertices().x(vIndex);
	}

	/** Y coordinate of triangle's second vertex, as a double. */
	default double v1y() {
		final long vIndex = mesh().triangles().vertex1(index());
		return mesh().vertices().y(vIndex);
	}

	/** Z coordinate of triangle's second vertex, as a double. */
	default double v1z() {
		final long vIndex = mesh().triangles().vertex1(index());
		return mesh().vertices().z(vIndex);
	}

	/** X coordinate of triangle's third vertex, as a double. */
	default double v2x() {
		final long vIndex = mesh().triangles().vertex2(index());
		return mesh().vertices().x(vIndex);
	}

	/** Y coordinate of triangle's third vertex, as a double. */
	default double v2y() {
		final long vIndex = mesh().triangles().vertex2(index());
		return mesh().vertices().y(vIndex);
	}

	/** Z coordinate of triangle's third vertex, as a double. */
	default double v2z() {
		final long vIndex = mesh().triangles().vertex2(index());
		return mesh().vertices().z(vIndex);
	}

	/** X coordinate of triangle's normal, as a double. */
	default double nx() {
		return mesh().triangles().nx(index());
	}

	/** Y coordinate of triangle's normal, as a double. */
	default double ny() {
		return mesh().triangles().ny(index());
	}

	/** Z coordinate of triangle's normal, as a double. */
	default double nz() {
		return mesh().triangles().nz(index());
	}
}
