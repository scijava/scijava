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

package org.scijava.ops.image.threshold.localSauvola;

import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.image.threshold.Thresholds;
import org.scijava.ops.spi.Nullable;

/**
 * Sauvola's local threshold, a modification of Niblack's method: a pixel is
 * foreground if it is strictly above
 * {@code mean * (1 + k * (sd / r - 1))}, with {@code mean} and {@code sd} the
 * mean and the population standard deviation (as in Fiji's
 * Auto_Local_Threshold plugin) of its neighborhood.
 * <p>
 * The parameter {@code r} is the dynamic range of the standard deviation, in
 * the units of the image. Unlike Phansalkar's rule, Sauvola's rule is
 * scale-equivariant (scaling the image and {@code r} scales the threshold
 * alike), so the image is not normalized; instead, {@code r} defaults to half
 * the nominal range of the pixel type, which is the largest possible standard
 * deviation of values in that range, and corresponds to the default
 * {@code r = 0.5} of {@code threshold.localPhansalkar} on its [0, 1] scale:
 * </p>
 * <ul>
 * <li>for {@link IntegerType} images, half the number of grey levels, e.g.
 * 128 for 8-bit images (the value recommended by Sauvola and Pietik&auml;inen
 * and used by Fiji) and 32768 for 16-bit images;</li>
 * <li>for floating-point images, 0.5, i.e. the image is assumed to be
 * normalized to [0, 1].</li>
 * </ul>
 * <p>
 * Images that use only part of their type's range (e.g. 12-bit data in a
 * 16-bit image) need an explicit {@code r}.
 * </p>
 * <p>
 * Sauvola J. and Pietik&auml;inen M. (2000) "Adaptive Document Image
 * Binarization", Pattern Recognition, 33(2): 225-236.
 * </p>
 * <p>
 * Original ImageJ implementation by Gabriel Landini.
 * </p>
 *
 * @author Stefan Helfrich (University of Konstanz)
 * @implNote op names='threshold.localSauvola', priority='-100.'
 */
public class ComputeLocalSauvolaThreshold<T extends RealType<T>> implements
	Computers.Arity4<Iterable<T>, T, Double, Double, BitType>
{

	public static final double DEFAULT_K = 0.5;

	/**
	 * Gets the default {@code r} of Sauvola's rule for images of a type: half
	 * the number of grey levels for {@link IntegerType}s, and 0.5 for other
	 * types.
	 *
	 * @param type a pixel of the image
	 * @return the default {@code r}
	 */
	public static double defaultR(final RealType<?> type) {
		if (type instanceof IntegerType) {
			return (type.getMaxValue() - type.getMinValue() + 1) / 2;
		}
		return 0.5;
	}

	/**
	 * Applies Sauvola's rule to a pixel value.
	 *
	 * @param value the pixel value
	 * @param mean the mean of its neighborhood
	 * @param stdDev the population standard deviation of its neighborhood
	 * @param k the k parameter
	 * @param r the r parameter
	 * @return whether the pixel is foreground, i.e. strictly above the threshold
	 */
	public static boolean isForeground(final double value, final double mean,
		final double stdDev, final double k, final double r)
	{
		return value > mean * (1.0 + k * (stdDev / r - 1.0));
	}

	/**
	 * Applies Sauvola's rule to one pixel.
	 *
	 * @param inputNeighborhood the neighborhood of the pixel
	 * @param inputCenterPixel the pixel
	 * @param k the k parameter (default: 0.5)
	 * @param r the dynamic range of the standard deviation (default: half the
	 *          nominal range of the pixel type, e.g. 128 for 8-bit images)
	 * @param output true if the pixel is foreground
	 */
	@Override
	public void compute(final Iterable<T> inputNeighborhood,
		final T inputCenterPixel, @Nullable final Double k,
		@Nullable final Double r, final BitType output)
	{
		final var stats = Thresholds.meanAndPopulationStdDev(inputNeighborhood);
		output.set(isForeground(inputCenterPixel.getRealDouble(), stats[0],
			stats[1], k == null ? DEFAULT_K : k, r == null ? defaultR(
				inputCenterPixel) : r));
	}

}
