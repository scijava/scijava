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

/*
 * -- Vertices --
 *
 * Mastodon
 * - net.imagej.mesh2.Vertex3 concrete class
 * - vertices are one float array.
 * - can do multiarrays for big data.
 * - might be useful for very large meshes
 *
 * JOML
 * - org.joml.Vector3f concrete class, Vector3fc read-only iface
 * - works with OpenGL and Vulkan
 * - has good geometry operations
 * - Externalizable
 * - fastest in general
 *
 * ClearGL
 * - cleargl.GLVector concrete class
 * - Serializable
 * - "not the thing that should be done in the future"
 *  -- JOML instead
 *
 * ImageJ Ops
 * - net.imagej.ops.geom.geom3d.mesh.Vector3D
 *  -- extends org.apache.commons.math3.geometry.euclidean.thread.Vector3D
 *  -- which implements org.apache.commons.math3.geometry.Vector
 * - needs reconciliation with imagej-mesh
 *
 * -- Mesh --
 *
 * scenery's mesh
 * - uses java.nio.FloatBuffer offheap (see HasGeometry):
 *  -- vertices
 *  -- normals
 *  -- texcoords
 *  -- indices (IntBuffer)
 *
 * ImageJ Mesh
 * - net.imagej.mesh2.Mesh interface
 * - One impl backed by JOML object(s)
 * -
 */

/**
 * <a href="https://en.wikipedia.org/wiki/Polygon_mesh">3D mesh</a> data
 * structure consisting of triangles and their vertices.
 * <p>
 * Vertices may be shared by multiple triangles. Coordinates can be retrieved as
 * {@code float} or {@code double} values.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface Mesh {

	/** The mesh's collection of vertices. */
	Vertices vertices();

	/** The mesh's collection of triangles. */
	Triangles triangles();
}
