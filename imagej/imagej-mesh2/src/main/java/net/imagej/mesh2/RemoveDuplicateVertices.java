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

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Deborah Schmidt
 */
class RemoveDuplicateVertices {

	static Mesh calculate(Mesh mesh, int precision) {
		Map<String, IndexedVertex> vertices = new LinkedHashMap<>();
		int[][] triangles = new int[(int) mesh.triangles().size()][3];

		int trianglesCount = 0;
		for (net.imagej.mesh2.Triangle triangle : mesh.triangles()) {
			RealPoint p1 = new RealPoint(triangle.v0x(), triangle.v0y(), triangle.v0z());
			RealPoint p2 = new RealPoint(triangle.v1x(), triangle.v1y(), triangle.v1z());
			RealPoint p3 = new RealPoint(triangle.v2x(), triangle.v2y(), triangle.v2z());
			triangles[trianglesCount][0] = getVertex(vertices, p1, precision);
			triangles[trianglesCount][1] = getVertex(vertices, p2, precision);
			triangles[trianglesCount][2] = getVertex(vertices, p3, precision);
			trianglesCount++;
		}
		Mesh res = new BufferMesh(vertices.size(), triangles.length);
		vertices.values().forEach(vertex -> {
			res.vertices().add(vertex.point.getFloatPosition(0), vertex.point.getFloatPosition(1), vertex.point.getFloatPosition(2));
		});

		for (int[] triangle : triangles) {
			res.triangles().add(triangle[0], triangle[1], triangle[2]);
		}
		return res;
	}

	private static int getVertex(Map<String, IndexedVertex> vertices, RealPoint point, int precision) {
		String hash = getHash(point, precision);
		IndexedVertex vertex = vertices.get(hash);
		if (vertex == null) return createVertex(vertices, hash, point, precision);
		return vertex.index;
	}

	private static int createVertex(Map<String, IndexedVertex> vertices, String hash, RealPoint point, int precision) {
		int index = vertices.size();
		IndexedVertex vertex = new IndexedVertex(point, index, precision);
		vertices.put(hash, vertex);
		return index;
	}

	private static String getHash(RealPoint point, int precision) {
		int factor = (int) Math.pow(10, precision);
		return Math.round(point.getFloatPosition(0) * factor) + "-" + Math.round(point.getFloatPosition(1) * factor) + "-" + Math.round(point.getFloatPosition(2) * factor);
	}

	private static class IndexedVertex {

		int index;
		RealLocalizable point;

		IndexedVertex(RealLocalizable pos, int index, int precision) {
			double[] newpos = new double[pos.numDimensions()];
			double factor = Math.pow(10, precision);
			for (int i = 0; i < newpos.length; i++) {
				newpos[i] = Math.round(pos.getDoublePosition(i) * factor) / factor;
			}
			this.point = new RealPoint(newpos);
			this.index = index;
		}

	}

}
