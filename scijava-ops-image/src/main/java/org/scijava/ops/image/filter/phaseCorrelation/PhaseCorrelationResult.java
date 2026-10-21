/*-
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

package org.scijava.ops.image.filter.phaseCorrelation;

import java.util.Arrays;

/**
 * The result of {@code filter.phaseCorrelate}: the estimated translation
 * between two images and how well the images agree under it.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class PhaseCorrelationResult {

	private final double[] shift;
	private final double correlation;
	private final double phaseCorrelation;
	private final long overlap;

	public PhaseCorrelationResult(final double[] shift, final double correlation,
		final double phaseCorrelation, final long overlap)
	{
		this.shift = shift;
		this.correlation = correlation;
		this.phaseCorrelation = phaseCorrelation;
		this.overlap = overlap;
	}

	/**
	 * Gets the translation {@code s} of the second image relative to the first,
	 * such that {@code image2(x) = image1(x - s)} where both are defined.
	 */
	public double[] getShift() {
		return shift.clone();
	}

	/**
	 * Gets the Pearson correlation coefficient of the two images on their
	 * overlap under the integer shift nearest to the subpixel one, or
	 * {@code NaN} if no candidate shift had enough nonconstant overlap.
	 */
	public double getCorrelation() {
		return correlation;
	}

	/** Gets the height of the chosen peak of the phase correlation matrix. */
	public double getPhaseCorrelation() {
		return phaseCorrelation;
	}

	/**
	 * Gets the number of pixels in the overlap of the two images under the
	 * integer shift.
	 */
	public long getOverlap() {
		return overlap;
	}

	@Override
	public String toString() {
		return "shift = " + Arrays.toString(shift) + ", correlation = " +
			correlation + ", phaseCorrelation = " + phaseCorrelation +
			", overlap = " + overlap;
	}
}
