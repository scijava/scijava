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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;

/**
 * Mesh implemented using {@link java.nio.Buffer} objects.
 *
 * @author Curtis Rueden
 */
public class BufferMesh implements Mesh {

	private final Vertices vertices;
	private final Triangles triangles;

	public BufferMesh(final int vertexMax, final int triangleMax) {
		this(vertexMax, triangleMax, true);
	}

	public BufferMesh(final int vertexMax, final int triangleMax,
		final boolean direct)
	{
		this(vertexMax, triangleMax, //
			direct ? ByteBuffer::allocateDirect : ByteBuffer::allocate);
	}

	public BufferMesh(final int vertexMax, final int triangleMax,
		final Function<Integer, ByteBuffer> creator)
	{
		this(floats(create(creator, vertexMax * 12)), //
			floats(create(creator, vertexMax * 12)), //
			floats(create(creator, vertexMax * 8)), //
			ints(create(creator, triangleMax * 12)), //
			floats(create(creator, triangleMax * 12)));
	}

	public BufferMesh(final FloatBuffer verts, final FloatBuffer vNormals,
		final FloatBuffer texCoords, final IntBuffer indices,
		final FloatBuffer tNormals)
	{
		vertices = new Vertices(verts, vNormals, texCoords);
		triangles = new Triangles(indices, tNormals);
	}

	@Override
	public Vertices vertices() {
		return vertices;
	}

	@Override
	public Triangles triangles() {
		return triangles;
	}

	// -- Inner classes --

	public class Vertices implements net.imagej.mesh2.Vertices {

		private static final int V_STRIDE = 3;
		private static final int N_STRIDE = 3;
		private static final int T_STRIDE = 2;

		private FloatBuffer verts;
		private FloatBuffer normals;
		private FloatBuffer texCoords;

		public Vertices(final FloatBuffer verts, final FloatBuffer normals,
			final FloatBuffer texCoords)
		{
			this.verts = verts;
			this.normals = normals;
			this.texCoords = texCoords;
		}

		public FloatBuffer verts() {
			return verts;
		}

		public FloatBuffer normals() {
			return normals;
		}

		public FloatBuffer texCoords() {
			return texCoords;
		}

		@Override
		public Mesh mesh() {
			return BufferMesh.this;
		}

		@Override
		public long size() {
			return verts.limit() / V_STRIDE;
		}

		@Override
		public float xf(long vIndex) {
			return verts.get(safeIndex(vIndex, V_STRIDE, 0));
		}

		@Override
		public float yf(long vIndex) {
			return verts.get(safeIndex(vIndex, V_STRIDE, 1));
		}

		@Override
		public float zf(long vIndex) {
			return verts.get(safeIndex(vIndex, V_STRIDE, 2));
		}

		@Override
		public float nxf(long vIndex) {
			return normals.get(safeIndex(vIndex, N_STRIDE, 0));
		}

		@Override
		public float nyf(long vIndex) {
			return normals.get(safeIndex(vIndex, N_STRIDE, 1));
		}

		@Override
		public float nzf(long vIndex) {
			return normals.get(safeIndex(vIndex, N_STRIDE, 2));
		}

		@Override
		public float uf(long vIndex) {
			return texCoords.get(safeIndex(vIndex, T_STRIDE, 0));
		}

		@Override
		public float vf(long vIndex) {
			return texCoords.get(safeIndex(vIndex, T_STRIDE, 1));
		}

		@Override
		public long addf(float x, float y, float z, float nx, float ny, float nz,
			float u, float v)
		{
			final long index = size();
			grow(verts, V_STRIDE);
			verts.put(x);
			verts.put(y);
			verts.put(z);
			grow(normals, N_STRIDE);
			normals.put(nx);
			normals.put(ny);
			normals.put(nz);
			grow(texCoords, T_STRIDE);
			texCoords.put(u);
			texCoords.put(v);
			return index;
		}

		@Override
		public void setf(long vIndex, float x, float y, float z, //
			float nx, float ny, float nz, //
			float u, float v)
		{
			verts.put(safeIndex(vIndex, V_STRIDE, 0), x);
			verts.put(safeIndex(vIndex, V_STRIDE, 1), y);
			verts.put(safeIndex(vIndex, V_STRIDE, 2), z);
			normals.put(safeIndex(vIndex, N_STRIDE, 0), nx);
			normals.put(safeIndex(vIndex, N_STRIDE, 1), ny);
			normals.put(safeIndex(vIndex, N_STRIDE, 2), nz);
			texCoords.put(safeIndex(vIndex, T_STRIDE, 0), u);
			texCoords.put(safeIndex(vIndex, T_STRIDE, 1), v);
		}

		@Override
		public void setPositionf(final long vIndex, final float x,
			final float y, final float z)
		{
			verts.put(safeIndex(vIndex, V_STRIDE, 0), x);
			verts.put(safeIndex(vIndex, V_STRIDE, 1), y);
			verts.put(safeIndex(vIndex, V_STRIDE, 2), z);
		}

		@Override
		public void setNormalf(final long vIndex, final float nx,
			final float ny, final float nz)
		{
			normals.put(safeIndex(vIndex, N_STRIDE, 0), nx);
			normals.put(safeIndex(vIndex, N_STRIDE, 1), ny);
			normals.put(safeIndex(vIndex, N_STRIDE, 2), nz);
		}

		@Override
		public void setTexturef(final long vIndex, final float u, final float v)
		{
			texCoords.put(safeIndex(vIndex, T_STRIDE, 0), u);
			texCoords.put(safeIndex(vIndex, T_STRIDE, 1), v);
		}
	}

	public class Triangles implements net.imagej.mesh2.Triangles {

		private static final int I_STRIDE = 3;
		private static final int N_STRIDE = 3;

		private IntBuffer indices;
		private FloatBuffer normals;

		public Triangles(final IntBuffer indices, final FloatBuffer normals) {
			this.indices = indices;
			this.normals = normals;
		}

		public IntBuffer indices() {
			return indices;
		}

		public FloatBuffer normals() {
			return normals;
		}

		@Override
		public Mesh mesh() {
			return BufferMesh.this;
		}

		@Override
		public long size() {
			return indices.limit() / I_STRIDE;
		}

		@Override
		public long vertex0(long tIndex) {
			return indices.get(safeIndex(tIndex, I_STRIDE, 0));
		}

		@Override
		public long vertex1(long tIndex) {
			return indices.get(safeIndex(tIndex, I_STRIDE, 1));
		}

		@Override
		public long vertex2(long tIndex) {
			return indices.get(safeIndex(tIndex, I_STRIDE, 2));
		}

		@Override
		public float nxf(long tIndex) {
			return normals.get(safeIndex(tIndex, N_STRIDE, 0));
		}

		@Override
		public float nyf(long tIndex) {
			return normals.get(safeIndex(tIndex, N_STRIDE, 1));
		}

		@Override
		public float nzf(long tIndex) {
			return normals.get(safeIndex(tIndex, N_STRIDE, 2));
		}

		@Override
		public long addf(long v0, long v1, long v2, float nx, float ny, float nz) {
			final long index = size();
			grow(indices, I_STRIDE);
			indices.put(safeInt(v0));
			indices.put(safeInt(v1));
			indices.put(safeInt(v2));
			grow(normals, N_STRIDE);
			normals.put(nx);
			normals.put(ny);
			normals.put(nz);
			return index;
		}
	}

	private static int safeIndex(final long index, final int span,
		final int offset)
	{
		return safeInt(span * index + offset);
	}

	private static int safeInt(final long value) {
		if (value > Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException("Value too large: " + value);
		}
		return (int) value;
	}

	private static ByteBuffer create(final Function<Integer, ByteBuffer> creator,
		final int length)
	{
		return creator.apply(length).order(ByteOrder.nativeOrder());
	}

	private static FloatBuffer floats(final ByteBuffer bytes) {
		final FloatBuffer floats = bytes.asFloatBuffer();
		// NB: The limit must be set _after_ casting to float,
		// or else the casted buffer will have a capacity of 0!
		floats.limit(0);
		return floats;
	}

	private static IntBuffer ints(final ByteBuffer bytes) {
		final IntBuffer ints = bytes.asIntBuffer();
		// NB: The limit must be set _after_ casting to int,
		// or else the casted buffer will have a capacity of 0!
		ints.limit(0);
		return ints;
	}

	/** Expands the buffer limit in anticipation of {@code put} operations. */
	private static void grow(final Buffer buffer, final int step) {
		buffer.limit(buffer.limit() + step);
	}
}
