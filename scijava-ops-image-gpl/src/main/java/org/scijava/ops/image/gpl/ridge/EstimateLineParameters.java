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

package org.scijava.ops.image.gpl.ridge;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

/**
 * Estimates the parameters of {@code segment.detectLines} (sigma and the
 * hysteresis thresholds) from the width of the lines to detect and their
 * contrast, as the Fiji Ridge Detection plugin does in its dialog.
 * <p>
 * The smoothing scale is {@code sigma = w / (2 sqrt(3)) + 0.5} for lines of
 * width {@code w}. Each threshold is 0.17 times the floor of the magnitude of
 * the response at the center of a bar-shaped line of width {@code w} and
 * contrast {@code c} (Steger 1998),
 * {@code 2 c (w/2) / (sqrt(2 pi) sigma^3) exp(-(w/2)^2 / (2 sigma^2))}, with
 * the low contrast for the lower threshold and the high contrast for the
 * upper one. For dark lines, the contrasts are taken relative to the maximum
 * gray value: the lower threshold uses {@code max - high} and the upper one
 * {@code max - low}. Unlike the plugin dialog, the results are not rounded to
 * two decimals.
 * </p>
 * <p>
 * This is a translation of the parameter estimation of the
 * <a href="https://github.com/thorstenwagner/ij-ridgedetection">Ridge
 * Detection</a> plugin by Thorsten Wagner (GPL v2+).
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 * @implNote op names='segment.estimateLineParameters'
 */
public class EstimateLineParameters implements
	Functions.Arity5<Double, Double, Double, Boolean, Double, double[]>
{

	/**
	 * Estimates the line detection parameters.
	 *
	 * @param lineWidth the width of the lines to detect
	 * @param lowContrast the lowest gray value of the lines (for bright lines)
	 * @param highContrast the highest gray value of the lines (for bright lines)
	 * @param darkLine whether the lines are dark on a bright background
	 *          (default false)
	 * @param maxValue the maximum gray value, used for dark lines (default 255,
	 *          as in the plugin)
	 * @return the array {sigma, lowerThreshold, upperThreshold}
	 */
	@Override
	public double[] apply( //
		final Double lineWidth, //
		final Double lowContrast, //
		final Double highContrast, //
		@Nullable final Boolean darkLine, //
		@Nullable final Double maxValue //
	) {
		final boolean dark = darkLine != null && darkLine;
		final double max = maxValue == null ? 255 : maxValue;
		final double sigma = lineWidth / (2 * Math.sqrt(3)) + 0.5;
		final double clow = dark ? max - highContrast : lowContrast;
		final double chigh = dark ? max - lowContrast : highContrast;
		return new double[] { sigma, threshold(lineWidth, sigma, clow),
			threshold(lineWidth, sigma, chigh) };
	}

	private static double threshold(final double lineWidth, final double sigma,
		final double contrast)
	{
		final double halfWidth = lineWidth / 2.0;
		final double response = Math.abs(-2 * contrast * halfWidth / (Math.sqrt(2 *
			Math.PI) * sigma * sigma * sigma) * Math.exp(-(halfWidth * halfWidth) /
				(2 * sigma * sigma)));
		return Math.floor(response) * 0.17;
	}
}
