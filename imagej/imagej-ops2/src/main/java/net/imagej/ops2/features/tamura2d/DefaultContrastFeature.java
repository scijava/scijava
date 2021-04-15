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
package net.imagej.ops2.features.tamura2d;

import java.util.function.Function;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.functions.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Default implementation of tamura feature contrast.
 * 
 * @author Andreas Graumann (University of Konstanz)
 */
@Plugin(type = Op.class, name = "features.tamura.contrast")
@Parameter(key = "input")
@Parameter(key = "output")
public class DefaultContrastFeature<I extends RealType<I>, O extends RealType<O>>
		implements Computers.Arity1<IterableInterval<I>, O> {

	@OpDependency(name = "stats.moment4AboutMean")
	private Function<Iterable<I>, O> m4Op;
	@OpDependency(name = "stats.variance")
	private Function<Iterable<I>, O> varOp;
	@OpDependency(name = "stats.stdDev")
	private Function<Iterable<I>, O> stdOp;

	@Override
	public void compute(final IterableInterval<I> input, final O output) {
		if (input.numDimensions() != 2)
			throw new IllegalArgumentException("Only 2 dimensional images allowed!");

		// Get fourth moment about mean
		double m4 = m4Op.apply(input).getRealDouble();
		double var = varOp.apply(input).getRealDouble();
		double std = stdOp.apply(input).getRealDouble();

		double l4 = m4 / (var * var);

		// contrast
		double fCon = std / Math.pow(l4, 0.25);
		output.setReal(fCon);
	}

}
