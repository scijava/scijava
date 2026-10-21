/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.geom.feretAspectRatio;

import java.util.function.Function;

import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Computes the Feret aspect ratio of a polygon: its maximum Feret diameter
 * divided by its minimum Feret diameter. It is 1 for a circle and grows for
 * elongated shapes. Unlike the ellipse-based aspect ratio, it does not depend
 * on the distribution of the area inside the shape.
 * <p>
 * Based on the "Feret aspect ratio" (FAR) shape descriptor of the
 * {@code Extended_Particle_Analyzer} plugin of the BioVoxxel Toolbox by Jan
 * Brocher / BioVoxxel (BSD 3-Clause license, Copyright (C) Jan Brocher /
 * BioVoxxel).
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jan Brocher (BioVoxxel)
 * @see <a href="https://github.com/biovoxxel/BioVoxxel-Toolbox">BioVoxxel
 *      Toolbox</a>
 * @implNote op names='geom.feretAspectRatio', label='Geometric (2D): Feret
 *           Aspect Ratio'
 */
public class DefaultFeretAspectRatio implements
	Computers.Arity1<Polygon2D, DoubleType>
{

	@OpDependency(name = "geom.maximumFeretsDiameter")
	private Function<Polygon2D, DoubleType> maxFeretFunc;

	@OpDependency(name = "geom.minimumFeretsDiameter")
	private Function<Polygon2D, DoubleType> minFeretFunc;

	/**
	 * Computes the Feret aspect ratio.
	 *
	 * @param input the polygon
	 * @param ratio the Feret aspect ratio of the polygon
	 */
	@Override
	public void compute(final Polygon2D input, final DoubleType ratio) {
		ratio.set(maxFeretFunc.apply(input).getRealDouble() / minFeretFunc.apply(
			input).getRealDouble());
	}
}
