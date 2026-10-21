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

package org.scijava.ops.image.coloc.costesThreshold;

/**
 * The result of {@code coloc.costesThreshold}: the thresholds of both
 * channels, the regression line they lie on, and the Pearson correlation of
 * the pixels below them.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class CostesResult {

	private final double threshold1;
	private final double threshold2;
	private final double slope;
	private final double intercept;
	private final double pearsonsBelow;

	public CostesResult(final double threshold1, final double threshold2,
		final double slope, final double intercept, final double pearsonsBelow)
	{
		this.threshold1 = threshold1;
		this.threshold2 = threshold2;
		this.slope = slope;
		this.intercept = intercept;
		this.pearsonsBelow = pearsonsBelow;
	}

	/** Gets the threshold of the first channel. */
	public double getThreshold1() {
		return threshold1;
	}

	/**
	 * Gets the threshold of the second channel, which is
	 * {@code slope * threshold1 + intercept}.
	 */
	public double getThreshold2() {
		return threshold2;
	}

	/** Gets the slope of the regression line of channel 2 on channel 1. */
	public double getSlope() {
		return slope;
	}

	/** Gets the intercept of the regression line of channel 2 on channel 1. */
	public double getIntercept() {
		return intercept;
	}

	/**
	 * Gets the Pearson correlation of the pixels below the thresholds, which
	 * is at most zero unless no threshold satisfies this.
	 */
	public double getPearsonsBelow() {
		return pearsonsBelow;
	}

	@Override
	public String toString() {
		return "threshold1 = " + threshold1 + ", threshold2 = " + threshold2 +
			", slope = " + slope + ", intercept = " + intercept +
			", pearsonsBelow = " + pearsonsBelow;
	}
}
