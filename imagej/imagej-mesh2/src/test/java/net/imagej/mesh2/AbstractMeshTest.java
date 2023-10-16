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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link Mesh}.
 *
 * @author Curtis Rueden
 * @author Kyle Harrington (University of Idaho, Moscow)
 */
public abstract class AbstractMeshTest {

    public abstract Mesh createMesh();

    @Test
    public void testMesh() {
        // Initialize the mesh.
        final Mesh mesh = createMesh();
        assertSame(mesh, mesh.vertices().mesh());
        assertSame(mesh, mesh.triangles().mesh());

        // Add some triangles.

        final double t0v0x = 1, t0v0y = 2, t0v0z = 3;
        final double t0v1x = 4, t0v1y = 5, t0v1z = 6;
        final double t0v2x = 7, t0v2y = 8, t0v2z = 9;
        final double t0nx = 10, t0ny = 11, t0nz = 12;
        final long t0 = mesh.triangles().add( //
                t0v0x, t0v0y, t0v0z, //
                t0v1x, t0v1y, t0v1z, //
                t0v2x, t0v2y, t0v2z, //
                t0nx, t0ny, t0nz); //

        assertEquals(3, mesh.vertices().size());

        final double t1v0x = 13, t1v0y = 14, t1v0z = 15;
        final double t1v1x = 16, t1v1y = 17, t1v1z = 18;
        final double t1v2x = 19, t1v2y = 20, t1v2z = 21;
        final double t1nx = 22, t1ny = 23, t1nz = 24;
        final long t1 = mesh.triangles().add( //
                t1v0x, t1v0y, t1v0z, //
                t1v1x, t1v1y, t1v1z, //
                t1v2x, t1v2y, t1v2z, //
                t1nx, t1ny, t1nz); //

        assertEquals(6, mesh.vertices().size());

        final double t2v0x = 25, t2v0y = 26, t2v0z = 27;
        final double t2v1x = 28, t2v1y = 29, t2v1z = 30;
        final double t2v2x = 31, t2v2y = 32, t2v2z = 33;
        final double t2nx = 36, t2ny = 37, t2nz = 38;
        final long t2v0 = mesh.vertices().add(t2v0x, t2v0y, t2v0z);
        final long t2v1 = mesh.vertices().add(t2v1x, t2v1y, t2v1z);
        final long t2v2 = mesh.vertices().add(t2v2x, t2v2y, t2v2z);
        final long t2 = mesh.triangles().add(t2v0, t2v1, t2v2, t2nx, t2ny, t2nz);

        assertEquals(9, mesh.vertices().size());
        assertVertex(mesh, 0, t0v0x, t0v0y, t0v0z);
        assertVertex(mesh, 1, t0v1x, t0v1y, t0v1z);
        assertVertex(mesh, 2, t0v2x, t0v2y, t0v2z);
        assertVertex(mesh, 3, t1v0x, t1v0y, t1v0z);
        assertVertex(mesh, 4, t1v1x, t1v1y, t1v1z);
        assertVertex(mesh, 5, t1v2x, t1v2y, t1v2z);
        assertVertex(mesh, 6, t2v0x, t2v0y, t2v0z);
        assertVertex(mesh, 7, t2v1x, t2v1y, t2v1z);
        assertVertex(mesh, 8, t2v2x, t2v2y, t2v2z);

        assertEquals(3, mesh.triangles().size());
        final Set<Long> vertexIndices = new HashSet<>();
        assertTriangle(mesh, vertexIndices, 0, //
                t0v0x, t0v0y, t0v0z, //
                t0v1x, t0v1y, t0v1z, //
                t0v2x, t0v2y, t0v2z, //
                t0nx, t0ny, t0nz);
        assertTriangle(mesh, vertexIndices, 1, //
                t1v0x, t1v0y, t1v0z, //
                t1v1x, t1v1y, t1v1z, //
                t1v2x, t1v2y, t1v2z, //
                t1nx, t1ny, t1nz);
        assertTriangle(mesh, vertexIndices, 2, //
                t2v0x, t2v0y, t2v0z, //
                t2v1x, t2v1y, t2v1z, //
                t2v2x, t2v2y, t2v2z, //
                t2nx, t2ny, t2nz);

        // Ensure all vertices are distinct.
        assertEquals(9, vertexIndices.size());

        // Add a triangle that reuses vertices.
        final long t3v0 = mesh.triangles().vertex0(t0);
        final long t3v1 = mesh.triangles().vertex0(t1);
        final long t3v2 = mesh.triangles().vertex0(t2);
        final double t3nx = 39, t3ny = 40, t3nz = 41;
        final long t3 = mesh.triangles().add(t3v0, t3v1, t3v2, t3nx, t3ny, t3nz);

        assertEquals(4, new HashSet<>(Arrays.asList(t0, t1, t2, t3)).size());
        assertEquals(mesh.triangles().vertex0(t0), mesh.triangles().vertex0(t3));
        assertEquals(mesh.triangles().vertex0(t1), mesh.triangles().vertex1(t3));
        assertEquals(mesh.triangles().vertex0(t2), mesh.triangles().vertex2(t3));
        assertTriangle(mesh, vertexIndices, 3, //
                t0v0x, t0v0y, t0v0z, //
                t1v0x, t1v0y, t1v0z, //
                t2v0x, t2v0y, t2v0z, //
                t3nx, t3ny, t3nz);


    }

    @Test
    public void testTriangleNormal() {
        Mesh inputMesh = new NaiveDoubleMesh();
        inputMesh.vertices().add(1, 1, 1);
        inputMesh.vertices().add(1, -1, 1);
        inputMesh.vertices().add(1,0,-1);
        inputMesh.triangles().add(0, 1, 2, 1, 0, 0);

        Mesh lazyInputMesh = new NaiveDoubleMesh();
        lazyInputMesh.vertices().add(1, 1, 1);
        lazyInputMesh.vertices().add(1, -1, 1);
        lazyInputMesh.vertices().add(1,0,-1);
        lazyInputMesh.triangles().add(0, 1, 2);

        Mesh outMesh = new BufferMesh((int) inputMesh.vertices().size(), (int) inputMesh.triangles().size());
        Meshes.calculateNormals(inputMesh, outMesh);

        for (int idx = 0; idx < inputMesh.triangles().size(); idx++) {
            // calculateNormals test
            assertTriangleNormal(inputMesh,0, outMesh.triangles().nx(idx), outMesh.triangles().ny(idx), outMesh.triangles().nz(idx));
            // Add triangles automatic normals calc test
            assertTriangleNormal(inputMesh,0, lazyInputMesh.triangles().nx(idx), lazyInputMesh.triangles().ny(idx), lazyInputMesh.triangles().nz(idx));
        }
    }

    private void assertTriangleNormal(Mesh mesh, long tIndex, //
                                    double nx, double ny, double nz) {
        // We know that our normal calculation does normalization, let's pre-normalize input normals
        double mx = mesh.triangles().nx(tIndex);
        double my = mesh.triangles().ny(tIndex);
        double mz = mesh.triangles().nz(tIndex);
        double meshMag = Math.sqrt(Math.pow(mx, 2) + Math.pow(my, 2) + Math.pow(mz, 2));
        assertEquals(nx, mx / meshMag, 0);
        assertEquals(ny, my / meshMag, 0);
        assertEquals(nz, mz / meshMag, 0);
    }

//    private void assertVertexNormal(Mesh mesh, long vIndex, //
//                                    double nx, double ny, double nz) {
//        // We know that our normal calculation does normalization, let's pre-normalize input normals
//        double mx = mesh.vertices().nx(vIndex);
//        double my = mesh.vertices().ny(vIndex);
//        double mz = mesh.vertices().nz(vIndex);
//        double meshMag = Math.sqrt(Math.pow(mx, 2) + Math.pow(my, 2) + Math.pow(mz, 2));
//        assertEquals(nx, mx / meshMag, 0.7);
//        assertEquals(ny, my / meshMag, 0.7);
//        assertEquals(nz, mz / meshMag, 0.7);
//    }

    private void assertVertex(Mesh mesh, long vIndex, //
                              double x, double y, double z) {
        assertEquals(x, mesh.vertices().x(vIndex), 0);
        assertEquals(y, mesh.vertices().y(vIndex), 0);
        assertEquals(z, mesh.vertices().z(vIndex), 0);
    }

    private void assertTriangle(final Mesh mesh, //
                                final Set<Long> vertexIndices, final long tIndex, //
                                final double v0x, final double v0y, final double v0z, //
                                final double v1x, final double v1y, final double v1z, //
                                final double v2x, final double v2y, final double v2z, //
                                final double nx, final double ny, final double nz) {
        final long vIndex0 = mesh.triangles().vertex0(tIndex);
        final long vIndex1 = mesh.triangles().vertex1(tIndex);
        final long vIndex2 = mesh.triangles().vertex2(tIndex);
        assertVertex(mesh, vertexIndices, vIndex0, v0x, v0y, v0z);
        assertVertex(mesh, vertexIndices, vIndex1, v1x, v1y, v1z);
        assertVertex(mesh, vertexIndices, vIndex2, v2x, v2y, v2z);
        assertEquals(nx, mesh.triangles().nx(tIndex), 0);
        assertEquals(ny, mesh.triangles().ny(tIndex), 0);
        assertEquals(nz, mesh.triangles().nz(tIndex), 0);
    }

    private void assertVertex(final Mesh mesh, final Set<Long> vertexIndices, //
                              final long vIndex, final double vx, final double vy, final double vz) {
        vertexIndices.add(vIndex);
        assertEquals(vx, mesh.vertices().x(vIndex), 0);
        assertEquals(vy, mesh.vertices().y(vIndex), 0);
        assertEquals(vz, mesh.vertices().z(vIndex), 0);
    }
}
