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
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.Priority;
import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Generic implementation of {@link net.imagej.ops2.Ops.Geometric.Compactness}.
 * 
 * Based on http://www.sciencedirect.com/science/article/pii/S003132030700324X.
 * 
 * In the paper compactness is defined as area^3/volume^2. For a sphere this is
 * minimized and results in 36*PI. To get values between (0,1] we use
 * (36*PI)/(area^3/volume^2).
 * 
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
@Plugin(type = Op.class, name = "geom.compactness", label = "Geometric (3D): Compactness", priority = Priority.VERY_HIGH)
@Parameter(key = "input")
@Parameter(key = "compactness")
public class DefaultCompactness implements Computers.Arity1<Mesh, DoubleType> {

	@OpDependency(name = "geom.boundarySize")
	private Function<Mesh, DoubleType> surfaceArea;

	@OpDependency(name = "geom.size")
	private Function<Mesh, DoubleType> volume;

	@Override
	public void compute(final Mesh input, final DoubleType output) {
		final double s3 = Math.pow(surfaceArea.apply(input).get(), 3);
		final double v2 = Math.pow(volume.apply(input).get(), 2);
		final double c = s3 / v2;
		output.set((36.0 * Math.PI) / c);
	}

}
