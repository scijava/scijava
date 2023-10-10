/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

package net.imagej.ops2.threshold.localPhansalkar;

import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

/**
 * <p>
 * This is a modification of Sauvola's thresholding method to deal with low
 * contrast images. In this algorithm the threshold is computed as t =
 * mean*(1+p*exp(-q*mean)+k*((stdev/r)-1)) for an image that is normalized to
 * [0, 1].
 * </p>
 * <p>
 * Phansalkar recommends k = 0.25, r = 0.5, p = 2 and q = 10. In the current
 * implementation, the values of p and q are fixed but can be implemented as
 * additional parameters.
 * </p>
 * <p>
 * <a href="http://fiji.sc/Auto_Local_Threshold#Phansalkar">Originally
 * implemented</a> from Phansalkar's paper description by G. Landini.
 * </p>
 * <p>
 * <i>Phansalkar N. et al. Adaptive local thresholding for detection of nuclei
 * in diversity stained cytology images. International Conference on
 * Communications and Signal Processing (ICCSP), 2011, 218 - 220.
 * <a href="http://dx.doi.org/10.1109/ICCSP.2011.5739305">
 * doi:10.1109/ICCSP.2011.5739305</a></i>
 * </p>
 *
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op names='threshold.localPhansalkar', priority='-100.'
 */
public class ComputeLocalPhansalkarThreshold<T extends RealType<T>> implements
	Computers.Arity4<Iterable<T>, T, Double, Double, BitType>
{

	public static final double DEFAULT_K = 0.25;
	public static final double DEFAULT_R = 0.5;

	private static final double P = 2.0;
	private static final double Q = 10.0;

	@OpDependency(name = "stats.mean")
	private Computers.Arity1<Iterable<T>, DoubleType> meanOp;

	@OpDependency(name = "stats.stdDev")
	private Computers.Arity1<Iterable<T>, DoubleType> stdDeviationOp;

	/**
	 * TODO
	 *
	 * @param inputNeighborhood
	 * @param inputCenterPixel
	 * @param k (required = false)
	 * @param r (required = false)
	 * @param output
	 */
	@Override
	public void compute(final Iterable<T> inputNeighborhood,
		final T inputCenterPixel, @Nullable Double k, @Nullable Double r,
		final BitType output)
	{
		if (k == null) k = DEFAULT_K;
		if (r == null) r = DEFAULT_R;

		final DoubleType meanValue = new DoubleType();
		meanOp.compute(inputNeighborhood, meanValue);

		final DoubleType stdDevValue = new DoubleType();
		stdDeviationOp.compute(inputNeighborhood, stdDevValue);

		final double threshold = meanValue.get() * (1.0d + P * Math.exp(-Q *
				meanValue.get()) + k * ((stdDevValue.get() / r) - 1.0));

		output.set(inputCenterPixel.getRealDouble() >= threshold);
	}
}
