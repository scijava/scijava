/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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
package net.imagej.ops2.geom.geom3d;

import java.util.function.Function;

import net.imagej.mesh.Mesh;
import net.imagej.mesh.Triangle;
import net.imglib2.type.numeric.real.DoubleType;

import org.apache.commons.math3.util.MathArrays;
import org.scijava.Priority;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Generic implementation of {@link net.imagej.ops2.Ops.Geometric.Size}.
 * 
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
@Plugin(type = Op.class, name = "geom.size", label = "Geometric3D: Volume", priority = Priority.VERY_HIGH - 1)
@Parameter(key = "input")
@Parameter(key = "volume", itemIO = ItemIO.OUTPUT)
public class DefaultVolumeMesh implements Function<Mesh, DoubleType> {

	@Override
	public DoubleType apply(final Mesh input) {
		double volume = 0;
		for (final Triangle triangle : input.triangles()) {
			volume += signedVolumeOfTriangle(//
					triangle.v0x(), triangle.v0y(), triangle.v0z(), //
					triangle.v1x(), triangle.v1y(), triangle.v1z(), //
					triangle.v2x(), triangle.v2y(), triangle.v2z());
		}
		return new DoubleType(Math.abs(volume));
	}

	private double signedVolumeOfTriangle(//
			final double p0x, final double p0y, final double p0z, //
			final double p1x, final double p1y, final double p1z, //
			final double p2x, final double p2y, final double p2z) {
		// cross product
		final double cpx = MathArrays.linearCombination(p1y, p2z, -p1z, p2y);
		final double cpy = MathArrays.linearCombination(p1z, p2x, -p1x, p2z);
		final double cpz = MathArrays.linearCombination(p1x, p2y, -p1y, p2x);

		// dot product
		return MathArrays.linearCombination(p0x, cpx, p0y, cpy, p0z, cpz) / 6.0f;
	}
}
