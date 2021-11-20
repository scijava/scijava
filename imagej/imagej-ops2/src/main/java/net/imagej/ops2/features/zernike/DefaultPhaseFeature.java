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
package net.imagej.ops2.features.zernike;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.Op;
import org.scijava.ops.OpDependency;
import org.scijava.plugin.Plugin;

/**
 * 
 * Get Phase of Zernike moment as feature
 * 
 * @author Andreas Graumann (University of Konstanz)
 *
 * @param <T>
 *            Input Type
 * @param <O>
 *            Output Type
 */
@Plugin(type = Op.class, name = "features.zernike.phase")
public class DefaultPhaseFeature<T extends RealType<T>>
		implements Computers.Arity3<IterableInterval<T>, Integer, Integer, DoubleType> {

	@OpDependency(name = "features.zernike.computer")
	private Functions.Arity3<IterableInterval<T>, Integer, Integer, ZernikeMoment> zernikeOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param order
	 * @param repetition
	 * @param output
	 */
	@Override
	public void compute(IterableInterval<T> input, Integer order, Integer repetition, DoubleType output) {
		if (input.numDimensions() != 2)
			throw new IllegalArgumentException("Only 2 dimensional inputs allowed!");
		output.setReal(zernikeOp.apply(input, order, repetition).getPhase());
	}

}
