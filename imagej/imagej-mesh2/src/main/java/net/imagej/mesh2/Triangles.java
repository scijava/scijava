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

import java.util.Iterator;

/**
 * Collection of triangles for a {@link Mesh}.
 *
 * @author Curtis Rueden
 */
public interface Triangles extends Iterable<Triangle> {

    /**
     * The mesh to which the collection of triangles belongs.
     */
    Mesh mesh();

    /**
     * Number of triangles in the collection.
     */
    long size();

    /**
     * <strong>Index</strong> of first vertex in a triangle.
     */
    long vertex0(long tIndex);

    /**
     * <strong>Index</strong> of second vertex in a triangle.
     */
    long vertex1(long tIndex);

    /**
     * <strong>Index</strong> of third vertex in a triangle.
     */
    long vertex2(long tIndex);

    /**
     * X coordinate of triangle's normal, as a float.
     */
    float nxf(long tIndex);

    /**
     * Y coordinate of triangle's normal, as a float.
     */
    float nyf(long tIndex);

    /**
     * Z coordinate of triangle's normal, as a float.
     */
    float nzf(long tIndex);

    /**
     * Adds a triangle to the mesh's triangles list.
     *
     * @param v0 Index of triangle's first vertex.
     * @param v1 Index of triangle's second vertex.
     * @param v2 Index of triangle's third vertex.
     * @param nx X coordinate of triangle's normal.
     * @param ny Y coordinate of triangle's normal.
     * @param nz Z coordinate of triangle's normal.
     * @return Index of newly added triangle.
     */
    long addf(long v0, long v1, long v2, float nx, float ny, float nz);

    /**
     * Adds a triangle to the mesh's triangles list.
     * <p>
     * Normal is computed with counterclockwise (i.e., right-hand) orientation.
     * </p>
     *
     * @param v0 Index of triangle's first vertex.
     * @param v1 Index of triangle's second vertex.
     * @param v2 Index of triangle's third vertex.
     * @return Index of newly added triangle.
     */
    default long addf(long v0, long v1, long v2) {
        // (v1 - v0) x (v2 - v0)

        final float v0x = mesh().vertices().xf(v0);
        final float v0y = mesh().vertices().yf(v0);
        final float v0z = mesh().vertices().zf(v0);
        final float v1x = mesh().vertices().xf(v1);
        final float v1y = mesh().vertices().yf(v1);
        final float v1z = mesh().vertices().zf(v1);
        final float v2x = mesh().vertices().xf(v2);
        final float v2y = mesh().vertices().yf(v2);
        final float v2z = mesh().vertices().zf(v2);

        final float v10x = v1x - v0x;
        final float v10y = v1y - v0y;
        final float v10z = v1z - v0z;

        final float v20x = v2x - v0x;
        final float v20y = v2y - v0y;
        final float v20z = v2z - v0z;

        final float nx = v10y * v20z - v10z * v20y;
        final float ny = v10z * v20x - v10x * v20z;
        final float nz = v10x * v20y - v10y * v20x;

        return addf(v0, v1, v2, nx, ny, nz);
    }

    /**
     * Adds a triangle to the mesh's triangles list.
     * <p>
     * This is a convenience method that first creates the vertices using
     * {@link Vertices#addf(float, float, float)}, then calls
     * {@link #addf(long, long, long, float, float, float)}.
     * </p>
     *
     * @param v0x X coordinate of triangle's first vertex.
     * @param v0y Y coordinate of triangle's first vertex.
     * @param v0z Z coordinate of triangle's first vertex.
     * @param v1x X coordinate of triangle's second vertex.
     * @param v1y Y coordinate of triangle's second vertex.
     * @param v1z Z coordinate of triangle's second vertex.
     * @param v2x X coordinate of triangle's third vertex.
     * @param v2y Y coordinate of triangle's third vertex.
     * @param v2z Z coordinate of triangle's third vertex.
     * @param nx  X coordinate of triangle's normal.
     * @param ny  Y coordinate of triangle's normal.
     * @param nz  Z coordinate of triangle's normal.
     * @return Index of newly added triangle.
     */
    default long addf(final float v0x, final float v0y, final float v0z, //
                      final float v1x, final float v1y, final float v1z, //
                      final float v2x, final float v2y, final float v2z, //
                      final float nx, final float ny, final float nz) {
        final long v0 = mesh().vertices().add(v0x, v0y, v0z);
        final long v1 = mesh().vertices().add(v1x, v1y, v1z);
        final long v2 = mesh().vertices().add(v2x, v2y, v2z);
        return addf(v0, v1, v2, nx, ny, nz);
    }

    /**
     * Adds a triangle to the mesh's triangles list.
     * <p>
     * This is a convenience method that first creates the vertices using
     * {@link Vertices#addf(float, float, float)}, then calls
     * {@link #addf(long, long, long)}.
     * </p>
     *
     * @param v0x X coordinate of triangle's first vertex.
     * @param v0y Y coordinate of triangle's first vertex.
     * @param v0z Z coordinate of triangle's first vertex.
     * @param v1x X coordinate of triangle's second vertex.
     * @param v1y Y coordinate of triangle's second vertex.
     * @param v1z Z coordinate of triangle's second vertex.
     * @param v2x X coordinate of triangle's third vertex.
     * @param v2y Y coordinate of triangle's third vertex.
     * @param v2z Z coordinate of triangle's third vertex.
     * @return Index of newly added triangle.
     */
    default long addf(final float v0x, final float v0y, final float v0z, //
                      final float v1x, final float v1y, final float v1z, //
                      final float v2x, final float v2y, final float v2z) {
        final long v0 = mesh().vertices().add(v0x, v0y, v0z);
        final long v1 = mesh().vertices().add(v1x, v1y, v1z);
        final long v2 = mesh().vertices().add(v2x, v2y, v2z);
        return addf(v0, v1, v2);
    }

    /**
     * X coordinate of triangle's normal, as a double.
     */
    default double nx(final long tIndex) {
        return nxf(tIndex);
    }

    /**
     * Y coordinate of triangle's normal, as a double.
     */
    default double ny(final long tIndex) {
        return nyf(tIndex);
    }

    /**
     * Z coordinate of triangle's normal, as a double.
     */
    default double nz(final long tIndex) {
        return nzf(tIndex);
    }

    /**
     * Adds a triangle to the mesh's triangles list.
     *
     * @param v0 Index of triangle's first vertex.
     * @param v1 Index of triangle's second vertex.
     * @param v2 Index of triangle's third vertex.
     * @param nx X coordinate of triangle's normal.
     * @param ny Y coordinate of triangle's normal.
     * @param nz Z coordinate of triangle's normal.
     * @return Index of newly added triangle.
     */
    default long add(final long v0, final long v1, final long v2, //
                     final double nx, final double ny, final double nz) {
        return addf(v0, v1, v2, (float) nx, (float) ny, (float) nz);
    }

    /**
     * Adds a triangle to the mesh's triangles list.
     * <p>
     * Normal is computed with counterclockwise (i.e., right-hand) orientation.
     * </p>
     *
     * @param v0 Index of triangle's first vertex.
     * @param v1 Index of triangle's second vertex.
     * @param v2 Index of triangle's third vertex.
     * @return Index of newly added triangle.
     */
    default long add(final long v0, final long v1, final long v2) {
        // (v1 - v0) x (v2 - v0)

        final double v0x = mesh().vertices().x(v0);
        final double v0y = mesh().vertices().y(v0);
        final double v0z = mesh().vertices().z(v0);
        final double v1x = mesh().vertices().x(v1);
        final double v1y = mesh().vertices().y(v1);
        final double v1z = mesh().vertices().z(v1);
        final double v2x = mesh().vertices().x(v2);
        final double v2y = mesh().vertices().y(v2);
        final double v2z = mesh().vertices().z(v2);

        final double v10x = v1x - v0x;
        final double v10y = v1y - v0y;
        final double v10z = v1z - v0z;

        final double v20x = v2x - v0x;
        final double v20y = v2y - v0y;
        final double v20z = v2z - v0z;

        final double nx = v10y * v20z - v10z * v20y;
        final double ny = v10z * v20x - v10x * v20z;
        final double nz = v10x * v20y - v10y * v20x;
        final double nmag = Math.sqrt(Math.pow(nx, 2) + Math.pow(ny, 2) + Math.pow(nz, 2));

        return add(v0, v1, v2, nx / nmag, ny / nmag, nz / nmag);
    }

    /**
     * Adds a triangle to the mesh's triangles list.
     * <p>
     * This is a convenience method that first creates the vertices using
     * {@link Vertices#add(double, double, double)}, then calls
     * {@link #add(long, long, long, double, double, double)}.
     * </p>
     *
     * @param v0x X coordinate of triangle's first vertex.
     * @param v0y Y coordinate of triangle's first vertex.
     * @param v0z Z coordinate of triangle's first vertex.
     * @param v1x X coordinate of triangle's second vertex.
     * @param v1y Y coordinate of triangle's second vertex.
     * @param v1z Z coordinate of triangle's second vertex.
     * @param v2x X coordinate of triangle's third vertex.
     * @param v2y Y coordinate of triangle's third vertex.
     * @param v2z Z coordinate of triangle's third vertex.
     * @param nx  X coordinate of triangle's normal.
     * @param ny  Y coordinate of triangle's normal.
     * @param nz  Z coordinate of triangle's normal.
     * @return Index of newly added triangle.
     */
    default long add(final double v0x, final double v0y, final double v0z, //
                     final double v1x, final double v1y, final double v1z, //
                     final double v2x, final double v2y, final double v2z, //
                     final double nx, final double ny, final double nz) {
        final long v0 = mesh().vertices().add(v0x, v0y, v0z);
        final long v1 = mesh().vertices().add(v1x, v1y, v1z);
        final long v2 = mesh().vertices().add(v2x, v2y, v2z);
        return add(v0, v1, v2, nx, ny, nz);
    }

    /**
     * Adds a triangle to the mesh's triangles list.
     * <p>
     * This is a convenience method that first creates the vertices using
     * {@link Vertices#add(double, double, double)}, then calls
     * {@link #add(long, long, long)}.
     * </p>
     *
     * @param v0x X coordinate of triangle's first vertex.
     * @param v0y Y coordinate of triangle's first vertex.
     * @param v0z Z coordinate of triangle's first vertex.
     * @param v1x X coordinate of triangle's second vertex.
     * @param v1y Y coordinate of triangle's second vertex.
     * @param v1z Z coordinate of triangle's second vertex.
     * @param v2x X coordinate of triangle's third vertex.
     * @param v2y Y coordinate of triangle's third vertex.
     * @param v2z Z coordinate of triangle's third vertex.
     * @return Index of newly added triangle.
     */
    default long add(final double v0x, final double v0y, final double v0z, //
                     final double v1x, final double v1y, final double v1z, //
                     final double v2x, final double v2y, final double v2z) {
        final long v0 = mesh().vertices().add(v0x, v0y, v0z);
        final long v1 = mesh().vertices().add(v1x, v1y, v1z);
        final long v2 = mesh().vertices().add(v2x, v2y, v2z);
        return add(v0, v1, v2);
    }

    // -- Iterable methods --

    @Override
    default Iterator<Triangle> iterator() {
        return new Iterator<Triangle>() {

            private long index = -1;

            private Triangle triangle = new Triangle() {

                @Override
                public Mesh mesh() {
                    return Triangles.this.mesh();
                }

                @Override
                public long index() {
                    return index;
                }
            };

            @Override
            public boolean hasNext() {
                return index + 1 < size();
            }

            @Override
            public Triangle next() {
                index++;
                return triangle;
            }
        };
    }
}
