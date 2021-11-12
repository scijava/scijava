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

package net.imagej.ops2.geom.geom2d;

import java.util.List;
import java.util.function.Function;

import net.imagej.ops2.geom.GeomUtils;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * Ferets Diameter for a certain angle.
 * 
 * @author Tim-Oliver Buchholz, University of Konstanz
 *@implNote op names='geom.feretsDiameter'
 */
public class DefaultFeretsDiameterForAngle implements Computers.Arity2<Polygon2D, Double, DoubleType> {

	@OpDependency(name = "geom.convexHull")
	private Function<Polygon2D, Polygon2D> function;

	/**
	 * TODO
	 *
	 * @param input
	 * @param angle
	 * @param feretsDiameter
	 */
	@Override
	public void compute(Polygon2D input, final Double angle, DoubleType output) {
		final List<? extends RealLocalizable> points = GeomUtils.vertices(function.apply(input));

		final double angleRad = -angle * Math.PI / 180.0;

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;

		for (RealLocalizable p : points) {
			final double tmpX = p.getDoublePosition(0) * Math.cos(angleRad)
					- p.getDoublePosition(1) * Math.sin(angleRad);
			minX = tmpX < minX ? tmpX : minX;
			maxX = tmpX > maxX ? tmpX : maxX;
		}

		output.set(Math.abs(maxX - minX));
	}

}
