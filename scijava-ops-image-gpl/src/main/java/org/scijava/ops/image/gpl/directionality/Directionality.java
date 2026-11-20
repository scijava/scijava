/*-
 * #%L
 * GPL-licensed image processing operations for SciJava Ops, translated from copyleft ImageJ/Fiji plugins.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package org.scijava.ops.image.gpl.directionality;

/**
 * Immutable result of a directionality analysis: the histogram of the amount
 * of structures in each direction, and the Gaussian fit of its highest peak,
 * as reported by the Fiji Directionality plugin.
 * <p>
 * Angles are in degrees, in their usual mathematical sense: 0 is the East
 * direction (along increasing X) and angles increase counterclockwise, that is
 * towards decreasing Y.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Jean-Yves Tinevez
 */
public final class Directionality {

	private final double[] binCenters;
	private final double[] histogram;
	private final double offset;
	private final double peak;
	private final double direction;
	private final double dispersion;
	private final double amount;
	private final double goodness;

	Directionality(final double[] binCenters, final double[] histogram,
		final double offset, final double peak, final double direction,
		final double dispersion, final double amount, final double goodness)
	{
		this.binCenters = binCenters.clone();
		this.histogram = histogram.clone();
		this.offset = offset;
		this.peak = peak;
		this.direction = direction;
		this.dispersion = dispersion;
		this.amount = amount;
		this.goodness = goodness;
	}

	/** @return the number of bins of the histogram */
	public int binCount() {
		return histogram.length;
	}

	/**
	 * @return the direction of each bin, in degrees (a copy). As in the plugin,
	 *         the first and last bins are at the start and end of the analyzed
	 *         range, so with the default range they are -90 and 90 degrees.
	 */
	public double[] binCenters() {
		return binCenters.clone();
	}

	/**
	 * @return the amount of structures in each bin (a copy), normalized so that
	 *         the bins sum to 1
	 */
	public double[] histogram() {
		return histogram.clone();
	}

	/**
	 * @return the baseline of the fitted Gaussian ({@code a} in
	 *         {@code a + (b - a) exp(-(x - c)^2 / (2 d^2))})
	 */
	public double offset() {
		return offset;
	}

	/** @return the peak value of the fitted Gaussian ({@code b}) */
	public double peak() {
		return peak;
	}

	/**
	 * @return the dominant direction: the center of the fitted Gaussian
	 *         ({@code c}), in degrees
	 */
	public double direction() {
		return direction;
	}

	/**
	 * @return the dispersion: the standard deviation of the fitted Gaussian
	 *         ({@code d}), in degrees
	 */
	public double dispersion() {
		return dispersion;
	}

	/**
	 * @return the amount of structures in the dominant direction: the sum of
	 *         the histogram bins within two standard deviations of the center
	 *         of the fitted Gaussian, taking the 180 degree periodicity into
	 *         account
	 */
	public double amount() {
		return amount;
	}

	/**
	 * @return the goodness of the Gaussian fit, from 1 (good) down to 0 (bad)
	 *         or less: one minus the residual sum of squares relative to the
	 *         total sum of squares, corrected for the degrees of freedom
	 */
	public double goodness() {
		return goodness;
	}
}
