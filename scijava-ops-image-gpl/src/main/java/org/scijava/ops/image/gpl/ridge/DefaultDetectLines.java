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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

/**
 * Detects curvilinear structures (lines) in a 2D image with the unbiased line
 * detector of Steger, as in the Fiji Ridge Detection plugin.
 * <p>
 * Line points are the points where the first directional derivative of the
 * Gaussian-smoothed image across the line vanishes, located with sub-pixel
 * accuracy from the Hessian; their strength is the second directional
 * derivative across the line. Line points are linked into lines by hysteresis
 * thresholding on this strength, junctions are detected where lines meet, and
 * lines are split at junctions. Optionally, the width of each line point is
 * extracted by locating the line edges along its normal, and the bias of the
 * width and position caused by asymmetric line profiles is corrected.
 * </p>
 * <p>
 * Compared to {@code segment.detectRidges}, this op follows Steger closely
 * and also detects dark lines, estimates line widths, corrects the width and
 * position bias, reports junctions and contour classes, and can merge lines
 * across crossings.
 * </p>
 * <p>
 * This is a translation of the core of the
 * <a href="https://github.com/thorstenwagner/ij-ridgedetection">Ridge
 * Detection</a> plugin by Thorsten Wagner and Mark Hiner (GPL v2+), itself a
 * translation of the C implementation of Carsten Steger (with modifications by
 * R. Balasubramanian), without its display parts. Deviations from the plugin:
 * line IDs (used internally to resolve overlaps) start at 0 for each
 * detection instead of being numbered by a global counter, which matches the
 * first detection of a fresh plugin session; the overlap resolution iterates
 * over lines in a deterministic order (the plugin uses identity hash order);
 * a bug that lost the responses of the first points of lines with more than
 * 100 points is fixed; and an out of range sigma throws an exception instead
 * of showing an error.
 * </p>
 * <p>
 * As in the plugin, the contrast estimate of {@link DetectedLine#intensity}
 * is limited to 275 (larger values are reset to 0), which assumes an 8-bit
 * gray value range.
 * </p>
 * <p>
 * Reference: C. Steger, "An unbiased detector of curvilinear structures",
 * IEEE Transactions on Pattern Analysis and Machine Intelligence
 * 20(2):113-125, 1998.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 * @author Mark Hiner
 * @author Carsten Steger
 * @param <T> type of the input image
 * @implNote op names='segment.detectLines'
 */
public class DefaultDetectLines<T extends RealType<T>> implements
	Functions.Arity11<RandomAccessibleInterval<T>, Double, Double, Double, Boolean, Boolean, Boolean, Boolean, Double, Double, OverlapResolution, DetectedLines>
{

	/**
	 * Detects lines in a 2D image.
	 *
	 * @param input the 2D image
	 * @param sigma the smoothing scale; for lines of width w, it should be at
	 *          least w/(2 sqrt(3)), and at least 0.4
	 * @param lowerThreshold the lower hysteresis threshold on the line point
	 *          strength (the second directional derivative): line points weaker
	 *          than this are rejected
	 * @param upperThreshold the upper hysteresis threshold: lines start at line
	 *          points at least this strong
	 * @param darkLine whether to detect dark lines on a bright background
	 *          instead of bright lines on a dark background (default false)
	 * @param estimateWidth whether to estimate the line width (default false)
	 * @param correctPosition whether to correct the bias of the line width and
	 *          position caused by asymmetric line profiles, and to estimate the
	 *          line asymmetry and contrast; implies estimateWidth (default false)
	 * @param extendLine whether to extend the line ends to find additional
	 *          junctions (default true)
	 * @param minLength the minimum length of the reported lines (default 0)
	 * @param maxLength the maximum length of the reported lines, or 0 for no
	 *          maximum (default 0)
	 * @param overlapResolution how to resolve crossing lines (default
	 *          {@link OverlapResolution#NONE})
	 * @return the detected lines and their junctions
	 */
	@Override
	public DetectedLines apply( //
		final RandomAccessibleInterval<T> input, //
		final Double sigma, //
		final Double lowerThreshold, //
		final Double upperThreshold, //
		@Nullable final Boolean darkLine, //
		@Nullable final Boolean estimateWidth, //
		@Nullable final Boolean correctPosition, //
		@Nullable final Boolean extendLine, //
		@Nullable final Double minLength, //
		@Nullable final Double maxLength, //
		@Nullable final OverlapResolution overlapResolution //
	) {
		if (input.numDimensions() != 2) {
			throw new IllegalArgumentException(
				"Line detection requires a 2D image, not " + input.numDimensions() +
					"D");
		}
		final boolean correct = correctPosition != null && correctPosition;
		final boolean width = correct || estimateWidth != null && estimateWidth;
		final int w = (int) input.dimension(0);
		final int h = (int) input.dimension(1);
		final float[] pixels = new float[w * h];
		final Cursor<T> c = Views.flatIterable(input).cursor();
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = c.next().getRealFloat();

		final LineDetector detector = new LineDetector(correct, width);
		final Lines lines = detector.detectLines(pixels, w, h, sigma,
			upperThreshold, lowerThreshold, minLength == null ? 0 : minLength,
			maxLength == null ? 0 : maxLength, darkLine != null && darkLine,
			extendLine == null || extendLine, overlapResolution == null
				? OverlapResolution.NONE : overlapResolution);

		final double x0 = input.min(0), y0 = input.min(1);
		final Map<Line, DetectedLine> map = new IdentityHashMap<>();
		final List<DetectedLine> result = new ArrayList<>(lines.size());
		for (final Line line : lines) {
			if (x0 != 0 || y0 != 0) {
				for (int i = 0; i < line.num; i++) {
					line.col[i] += x0;
					line.row[i] += y0;
				}
			}
			final DetectedLine detected = new DetectedLine(line, width,
				correct);
			map.put(line, detected);
			result.add(detected);
		}
		final List<LineJunction> junctions = new ArrayList<>();
		for (final Junction j : detector.getJunctions()) {
			final DetectedLine l1 = map.get(j.getLine1());
			final DetectedLine l2 = map.get(j.getLine2());
			// Note: junctions whose lines were not reported are dropped.
			if (l1 == null || l2 == null) continue;
			junctions.add(new LineJunction(j.x + x0, j.y + y0, l1, l2, j.pos,
				j.isNonTerminal));
		}
		return new DetectedLines(result, junctions);
	}
}
