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

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.api.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Generic implementation of {@link net.imagej.ops2.Ops.Geometric.MainElongation}
 * 
 * Adapted to 3D from http://www.math.uci.edu/icamp/summer/research_11/park/
 * shape_descriptors_survey.pdf
 * 
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
@Plugin(type = Op.class, name = "geom.mainElongation", label = "Geometric (3D): Main Elongation", priority = Priority.VERY_HIGH)
public class DefaultMainElongation implements Computers.Arity1<Mesh, DoubleType> {

	@OpDependency(name = "geom.secondMoment")
	private Function<Mesh, RealMatrix> inertiaTensor;

	/**
	 * TODO
	 *
	 * @param inputMesh
	 * @param output
	 */
	@Override
	public void compute(final Mesh input, final DoubleType output) {
		final RealMatrix it = inertiaTensor.apply(input);
		final EigenDecomposition ed = new EigenDecomposition(it);

		final double l1 = ed.getRealEigenvalue(0) - ed.getRealEigenvalue(2) + ed.getRealEigenvalue(1);
		final double l2 = ed.getRealEigenvalue(0) - ed.getRealEigenvalue(1) + ed.getRealEigenvalue(2);
		final double l3 = ed.getRealEigenvalue(2) - ed.getRealEigenvalue(0) + ed.getRealEigenvalue(1);

		final double g = 1 / (8 * Math.PI / 15);

		final double a = Math.pow(g * l1 * l1 / Math.sqrt(l2 * l3), 1 / 5d);
		final double b = Math.pow(g * l2 * l2 / Math.sqrt(l1 * l3), 1 / 5d);
		output.set(1 - (b / a));
	}

}
