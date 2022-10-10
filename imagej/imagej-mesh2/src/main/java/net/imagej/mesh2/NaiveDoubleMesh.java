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

import org.scijava.collections.DoubleArray;
import org.scijava.collections.IntArray;

public class NaiveDoubleMesh implements Mesh {

	private final Vertices vertices;
	private final Triangles triangles;

	public NaiveDoubleMesh() {
		vertices = new Vertices();
		triangles = new Triangles();
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

		private final DoubleArray xs, ys, zs;
		private final DoubleArray nxs, nys, nzs;
		private final DoubleArray us, vs;

		public Vertices() {
			xs = new DoubleArray();
			ys = new DoubleArray();
			zs = new DoubleArray();
			nxs = new DoubleArray();
			nys = new DoubleArray();
			nzs = new DoubleArray();
			us = new DoubleArray();
			vs = new DoubleArray();
		}

		@Override
		public Mesh mesh() {
			return NaiveDoubleMesh.this;
		}

		@Override
		public long size() {
			return xs.size();
		}

		@Override
		public double x(long vIndex) {
			return xs.get(safeIndex(vIndex));
		}

		@Override
		public double y(long vIndex) {
			return ys.get(safeIndex(vIndex));
		}

		@Override
		public double z(long vIndex) {
			return zs.get(safeIndex(vIndex));
		}

		@Override
		public double nx(long vIndex) {
			return nxs.get(safeIndex(vIndex));
		}

		@Override
		public double ny(long vIndex) {
			return nys.get(safeIndex(vIndex));
		}

		@Override
		public double nz(long vIndex) {
			return nzs.get(safeIndex(vIndex));
		}

		@Override
		public double u(long vIndex) {
			return us.get(safeIndex(vIndex));
		}

		@Override
		public double v(long vIndex) {
			return vs.get(safeIndex(vIndex));
		}

		@Override
		public long add(double x, double y, double z, double nx, double ny, double nz,
			double u, double v)
		{
			final int index = xs.size();
			xs.add(x);
			ys.add(y);
			zs.add(z);
			nxs.add(nx);
			nys.add(ny);
			nzs.add(nz);
			us.add(u);
			vs.add(v);
			return index;
		}

		@Override
		public void set(long vIndex, double x, double y, double z, double nx, double ny,
			double nz, double u, double v)
		{
			final int index = safeIndex(vIndex);
			xs.set(index, x);
			ys.set(index, y);
			zs.set(index, z);
			nxs.set(index, nx);
			nys.set(index, ny);
			nzs.set(index, nz);
			us.set(index, u);
			vs.set(index, v);
		}

		@Override
		public void setPosition(final long vIndex, final double x,
			final double y, final double z)
		{
			final int index = safeIndex(vIndex);
			xs.set(index, x);
			ys.set(index, y);
			zs.set(index, z);
		}

		@Override
		public void setNormal(final long vIndex, final double nx,
			final double ny, final double nz)
		{
			final int index = safeIndex(vIndex);
			nxs.set(index, nx);
			nys.set(index, ny);
			nzs.set(index, nz);
		}

		@Override
		public void setTexture(final long vIndex, final double u,
			final double v)
		{
			final int index = safeIndex(vIndex);
			us.set(index, u);
			vs.set(index, v);
		}

		private int safeIndex(final long index) {
			if (index > Integer.MAX_VALUE) {
				throw new IndexOutOfBoundsException("Index too large: " + index);
			}
			return (int) index;
		}

		@Override
		public float xf(long vIndex) {
			return (float) x(vIndex);
		}

		@Override
		public float yf(long vIndex) {
			return (float) y(vIndex);
		}

		@Override
		public float zf(long vIndex) {
			return (float) z(vIndex);
		}

		@Override
		public float nxf(long vIndex) {
			return (float) nx(vIndex);
		}

		@Override
		public float nyf(long vIndex) {
			return (float) ny(vIndex);
		}

		@Override
		public float nzf(long vIndex) {
			return (float) nz(vIndex);
		}

		@Override
		public float uf(long vIndex) {
			return (float) u(vIndex);
		}

		@Override
		public float vf(long vIndex) {
			return (float) v(vIndex);
		}

		@Override
		public long addf(float x, float y, float z, float nx, float ny, float nz,
			float u, float v)
		{
			return add(x, y, z, nx, ny, nz, u, v);
		}

		@Override
		public void setf(long vIndex, float x, float y, float z, float nx, float ny,
			float nz, float u, float v)
		{
			set(vIndex, x, y, z, nx, ny, nz, u, v);
		}

		@Override
		public void setPositionf(final long vIndex, final float x,
			final float y, final float z)
		{
			setPosition(vIndex, x, y, z);
		}

		@Override
		public void setNormalf(final long vIndex, final float nx,
			final float ny, final float nz)
		{
			setNormal(vIndex, nx, ny, nz);
		}

		@Override
		public void setTexturef(final long vIndex, final float u, final float v)
		{
			setTexture(vIndex, u, v);
		}
	}

	public class Triangles implements net.imagej.mesh2.Triangles {

		private final IntArray v0s, v1s, v2s;
		private final DoubleArray nxs, nys, nzs;

		public Triangles() {
			v0s = new IntArray();
			v1s = new IntArray();
			v2s = new IntArray();
			nxs = new DoubleArray();
			nys = new DoubleArray();
			nzs = new DoubleArray();
		}

		@Override
		public Mesh mesh() {
			return NaiveDoubleMesh.this;
		}

		@Override
		public long size() {
			return v1s.size();
		}

		@Override
		public long vertex0(long tIndex) {
			return v0s.get(safeIndex(tIndex));
		}

		@Override
		public long vertex1(long tIndex) {
			return v1s.get(safeIndex(tIndex));
		}

		@Override
		public long vertex2(long tIndex) {
			return v2s.get(safeIndex(tIndex));
		}

		@Override
		public double nx(long tIndex) {
			return nxs.get(safeIndex(tIndex));
		}

		@Override
		public double ny(long tIndex) {
			return nys.get(safeIndex(tIndex));
		}

		@Override
		public double nz(long tIndex) {
			return nzs.get(safeIndex(tIndex));
		}

		@Override
		public long add(long v0, long v1, long v2, double nx, double ny, double nz) {
			final int index = v0s.size();
			v0s.add(safeIndex(v0));
			v1s.add(safeIndex(v1));
			v2s.add(safeIndex(v2));
			nxs.add(nx);
			nys.add(ny);
			nzs.add(nz);
			return index;
		}

		private int safeIndex(final long index) {
			if (index > Integer.MAX_VALUE) {
				throw new IndexOutOfBoundsException("Index too large: " + index);
			}
			return (int) index;
		}

		@Override
		public float nxf(long tIndex) {
			return (float) nx(tIndex);
		}

		@Override
		public float nyf(long tIndex) {
			return (float) ny(tIndex);
		}

		@Override
		public float nzf(long tIndex) {
			return (float) nz(tIndex);
		}

		@Override
		public long addf(long v0, long v1, long v2, float nx, float ny, float nz) {
			return add(v0, v1, v2, nx, ny, nz);
		}
	}
}
