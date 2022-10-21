/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2016 - 2022 ImageJ2 developers.
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

import java.util.Iterator;

/**
 * Collection of vertices for a {@link Mesh}.
 *
 * @author Curtis Rueden
 */
public interface Vertices extends Iterable<Vertex> {

	/** The mesh to which the collection of vertices belongs. */
	Mesh mesh();

	/** Number of vertices in the collection. */
	long size();

	/** X position of a vertex, as a float. */
	float xf(long vIndex);

	/** Y position of a vertex, as a float. */
	float yf(long vIndex);

	/** Z position of a vertex, as a float. */
	float zf(long vIndex);

	/** X coordinate of vertex normal, as a float. */
	float nxf(long vIndex);

	/** Y coordinate of vertex normal, as a float. */
	float nyf(long vIndex);

	/** Z coordinate of vertex normal, as a float. */
	float nzf(long vIndex);

	/** U value of vertex texture coordinate, as a float. */
	float uf(long vIndex);

	/** V value of vertex texture coordinate, as a float. */
	float vf(long vIndex);

	/**
	 * Adds a vertex.
	 *
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 * @param nx X coordinate of the vertex's normal.
	 * @param ny Y coordinate of the vertex's normal.
	 * @param nz Z coordinate of the vertex's normal.
	 * @param u U value of vertex texture coordinate.
	 * @param v V value of vertex texture coordinate.
	 * @return Index of newly added vertex.
	 */
	long addf(float x, float y, float z, //
		float nx, float ny, float nz, //
		float u, float v);

	/**
	 * Overwrites a vertex's position, normal and texture coordinates.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 * @param nx X coordinate of the vertex's normal.
	 * @param ny Y coordinate of the vertex's normal.
	 * @param nz Z coordinate of the vertex's normal.
	 * @param u U value of vertex texture coordinate.
	 * @param v V value of vertex texture coordinate.
	 */
	void setf(long vIndex, float x, float y, float z, //
		float nx, float ny, float nz, //
		float u, float v);

	/**
	 * Overwrites a vertex's position.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 */
	void setPositionf(long vIndex, float x, float y, float z);

	/**
	 * Overwrites a vertex's normal.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param nx X coordinate of the vertex's normal.
	 * @param ny Y coordinate of the vertex's normal.
	 * @param nz Z coordinate of the vertex's normal.
	 */
	void setNormalf(long vIndex, float nx, float ny, float nz);

	/**
	 * Overwrites a vertex's texture coordinates.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param u U value of vertex texture coordinate.
	 * @param v V value of vertex texture coordinate.
	 */
	void setTexturef(long vIndex, float u, float v);

	/**
	 * Adds a vertex.
	 *
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 * @return Index of newly added vertex.
	 */
	default long addf(final float x, final float y, final float z) {
		return addf(x, y, z, 0, 0, 0, 0, 0);
	}

	/**
	 * Overwrites the position of a vertex, sets normal and texture coordinates
	 * to {@code 0}
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 */
	default void setf(final long vIndex, //
		final float x, final float y, final float z)
	{
		setf(vIndex, x, y, z, 0, 0, 0, 0, 0);
	}

	/** X position of a vertex, as a double. */
	default double x(final long vIndex) {
		return xf(vIndex);
	}

	/** Y position of a vertex, as a double. */
	default double y(final long vIndex) {
		return yf(vIndex);
	}

	/** Z position of a vertex, as a double. */
	default double z(final long vIndex) {
		return zf(vIndex);
	}

	/** X coordinate of vertex normal, as a double. */
	default double nx(final long vIndex) {
		return nxf(vIndex);
	}

	/** Y coordinate of vertex normal, as a double. */
	default double ny(final long vIndex) {
		return nyf(vIndex);
	}

	/** Z coordinate of vertex normal, as a double. */
	default double nz(final long vIndex) {
		return nzf(vIndex);
	}

	/** U value of vertex texture coordinate, as a double. */
	default double u(long vIndex) {
		return uf(vIndex);
	}

	/** V value of vertex texture coordinate, as a double. */
	default double v(long vIndex) {
		return vf(vIndex);
	}

	/**
	 * Adds a vertex.
	 *
	 * @param x X coordinate of the vertex.
	 * @param y Y coordinate of the vertex.
	 * @param z Z coordinate of the vertex.
	 * @return Index of newly added vertex.
	 */
	default long add(final double x, final double y, final double z) {
		return addf((float) x, (float) y, (float) z);
	}

	/**
	 * Adds a vertex.
	 *
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 * @param nx X coordinate of the vertex's normal.
	 * @param ny Y coordinate of the vertex's normal.
	 * @param nz Z coordinate of the vertex's normal.
	 * @param u U value of vertex texture coordinate.
	 * @param v V value of vertex texture coordinate.
	 * @return Index of newly added vertex.
	 */
	default long add(double x, double y, double z, //
		double nx, double ny, double nz, //
		double u, double v)
	{
		return addf((float) x, (float) y, (float) z, //
			(float) nx, (float) ny, (float) nz, //
			(float) u, (float) v);
	}

	/**
	 * Overwrites the position of a vertex, sets normal and texture coordinates
	 * to {@code 0}
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 */
	default void set(final long vIndex, final double x, final double y,
		final double z)
	{
		set(vIndex, x, y, z, 0, 0, 0, 0, 0);
	}

	/**
	 * Overwrites a vertex's position, normal and texture coordinates.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 * @param nx X coordinate of the vertex's normal.
	 * @param ny Y coordinate of the vertex's normal.
	 * @param nz Z coordinate of the vertex's normal.
	 * @param u U value of vertex texture coordinate.
	 * @param v V value of vertex texture coordinate.
	 */
	default void set(final long vIndex, final double x, final double y,
		final double z, //
		final double nx, final double ny, final double nz, //
		final double u, final double v)
	{
		setf(vIndex, (float) x, (float) y, (float) z, //
			(float) nx, (float) ny, (float) nz, //
			(float) u, (float) v);
	}

	/**
	 * Overwrites a vertex's position.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param x X position of the vertex.
	 * @param y Y position of the vertex.
	 * @param z Z position of the vertex.
	 */
	default void setPosition(final long vIndex, final double x, final double y,
		final double z)
	{
		setPositionf(vIndex, (float) x, (float) y, (float) z);
	}

	/**
	 * Overwrites a vertex's normal.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param nx X coordinate of the vertex's normal.
	 * @param ny Y coordinate of the vertex's normal.
	 * @param nz Z coordinate of the vertex's normal.
	 */
	default void setNormal(final long vIndex, final double nx, final double ny,
		final double nz)
	{
		setNormalf(vIndex, (float) nx, (float) ny, (float) nz);
	}

	/**
	 * Overwrites a vertex's texture coordinates.
	 *
	 * @param vIndex Index of vertex to overwrite.
	 * @param u U value of vertex texture coordinate.
	 * @param v V value of vertex texture coordinate.
	 */
	default void setTexture(final long vIndex, final double u, final double v)
	{
		setTexturef(vIndex, (float) u, (float) v);
	}


	// -- Iterable methods --

	@Override
	default Iterator<Vertex> iterator() {
		return new Iterator<Vertex>() {

			private long index = -1;

			private Vertex vertex = new Vertex() {

				@Override
				public Mesh mesh() { return Vertices.this.mesh(); }

				@Override
				public long index() { return index; }
			};

			@Override
			public boolean hasNext() {
				return index + 1 < size();
			}

			@Override
			public Vertex next() {
				index++;
				return vertex;
			}
		};
	}
}
