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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.scijava.ops.image.gpl.ridge.LinesUtil.MutableInt;

/**
 * Driver of the line detection, from the ij-ridgedetection {@code LineDetector}
 * class: extracts the lines and junctions with Steger's algorithm, then
 * reconstructs the junctions and contour classes, splits lines at junctions,
 * optionally resolves overlapping lines, and prunes lines by length.
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 */
class LineDetector {

	private final boolean doCorrectPosition;

	private final boolean doEstimateWidth;

	private List<Junction> junctions;

	private Set<Integer> alreadyProcessedJunctionPoints;

	LineDetector(final boolean doCorrectPosition, final boolean doEstimateWidth) {
		this.doCorrectPosition = doCorrectPosition;
		this.doEstimateWidth = doEstimateWidth;
	}

	/**
	 * Detects lines.
	 *
	 * @param image the pixels of the image, row by row
	 * @param width the width of the image
	 * @param height the height of the image
	 * @param sigma the smoothing scale; at least width/(2*sqrt(3)) for lines of
	 *          the given width
	 * @param upperThresh upper hysteresis threshold of the linking algorithm
	 * @param lowerThresh lower hysteresis threshold of the linking algorithm
	 * @param minLength minimum line length
	 * @param maxLength maximum line length (0 for no maximum)
	 * @param isDarkLine whether lines are darker than the background
	 * @param doExtendLine whether to extend lines to find more junctions
	 * @param overlap how overlapping lines are resolved
	 * @return the detected lines
	 */
	Lines detectLines(final float[] image, final int width, final int height,
		final double sigma, final double upperThresh, final double lowerThresh,
		final double minLength, final double maxLength, final boolean isDarkLine,
		final boolean doExtendLine, final OverlapResolution overlap)
	{
		checkSigma(sigma, width, height);
		return getLines(image, width, height, sigma, upperThresh, lowerThresh,
			minLength, maxLength, isDarkLine ? LinesUtil.MODE_DARK
				: LinesUtil.MODE_LIGHT, doExtendLine, overlap);
	}

	/** @return the junctions found by the last detection */
	List<Junction> getJunctions() {
		return junctions;
	}

	private void assignLinesToJunctions(Lines lines, List<Junction> junctions) {
		for (Junction j : junctions) {
			j.lineCont1 = lines.get(j.cont1);
			j.lineCont2 = lines.get(j.cont2);
		}
	}

	private void addAdditionalJunctionPointsAndLines(Lines lines,
		List<Junction> junctions)
	{

		for (int i = 0; i < junctions.size(); i++) {
			Junction splitPoint = junctions.get(i);
			if (!alreadyProcessedJunctionPoints.contains(i)) {

				/*
				 * Find junctions with the same position as the split point
				 */
				List<Junction> junctionsWithTheSamePosition = new ArrayList<>();
				alreadyProcessedJunctionPoints.add(i);
				junctionsWithTheSamePosition.add(splitPoint);
				for (int j = i + 1; j < junctions.size(); j++) {
					if (!alreadyProcessedJunctionPoints.contains(j)) {
						Junction junc2 = junctions.get(j);
						if (Math.abs(junc2.x - splitPoint.x) < 0.01 && Math.abs(junc2.y -
							splitPoint.y) < 0.01)
						{
							alreadyProcessedJunctionPoints.add(j);
							junctionsWithTheSamePosition.add(junc2);
						}
					}
				}

				/*
				 * Connect all lines which are connected with the processed line also
				 * with each other (new junctions point)
				 */

				ArrayList<Line> connectedWithProcessedLine = new ArrayList<Line>();
				ArrayList<Integer> connectedWithProcessedIndex =
					new ArrayList<Integer>();
				for (Junction junc : junctionsWithTheSamePosition) {
					connectedWithProcessedLine.add(junc.getLine2());
					connectedWithProcessedIndex.add(junc.cont2);
				}
				for (int j = 0; j < connectedWithProcessedLine.size(); j++) {
					for (int k = j + 1; k < connectedWithProcessedLine.size(); k++) {
						Line l1 = connectedWithProcessedLine.get(j);
						Line l2 = connectedWithProcessedLine.get(k);
						Junction junc = new Junction();
						junc.lineCont1 = l1;
						junc.lineCont2 = l2;
						junc.x = splitPoint.x;
						junc.y = splitPoint.y;
						junc.cont1 = connectedWithProcessedIndex.get(j);
						junc.cont2 = connectedWithProcessedIndex.get(k);
						junc.pos = l1.getStartOrdEndPosition(junc.x, junc.y);
						junctions.add(junc);
						alreadyProcessedJunctionPoints.add(junctions.size() - 1);
					}
				}

				/*
				 * Split the line in two line at the split point if it is not at the end
				 * or beginning of the line
				 */
				Line l1 = splitPoint.getLine1();
				int pos = splitPoint.pos;
				boolean isClosedContour = l1.col[0] == l1.col[l1.num - 1] &&
					l1.row[0] == l1.row[l1.num - 1];

				if (isClosedContour) {
					l1.setContourClass(ContourClass.CLOSED);
					l1.setContourClass(reconstructContourClass(l1, l1
						.getStartOrdEndPosition(splitPoint.x, splitPoint.y)));
				}
				if (pos != 0 && pos != (l1.num - 1) && !isClosedContour) {
					// Split the line at pos, which belongs to both parts.
					int keepLength = pos + 1;

					float[] keepAsymmetry = new float[keepLength];
					float[] keepIntensity = new float[keepLength];

					float[] keepAngle = new float[keepLength];
					float[] keepWidthL = new float[keepLength];
					float[] keepWidthR = new float[keepLength];

					float[] keepCol = new float[keepLength];
					float[] keepRow = new float[keepLength];
					float[] keepResponse = new float[keepLength];

					int splitSize = (int) (l1.num - pos);

					float[] splitAsymmetry = new float[splitSize];
					float[] splitIntensity = new float[splitSize];

					float[] splitAngle = new float[splitSize];
					float[] splitWidthL = new float[splitSize];
					float[] splitWidthR = new float[splitSize];

					float[] splitCol = new float[splitSize];
					float[] splitRow = new float[splitSize];
					float[] splitResponse = new float[splitSize];

					if (doEstimateWidth) {
						if (doCorrectPosition) {
							System.arraycopy(l1.asymmetry, 0, keepAsymmetry, 0, keepLength);
							System.arraycopy(l1.asymmetry, pos, splitAsymmetry, 0, splitSize);

							System.arraycopy(l1.intensity, 0, keepIntensity, 0, keepLength);
							System.arraycopy(l1.intensity, pos, splitIntensity, 0, splitSize);
						}

						System.arraycopy(l1.angle, 0, keepAngle, 0, keepLength);
						System.arraycopy(l1.angle, pos, splitAngle, 0, splitSize);

						System.arraycopy(l1.widthL, 0, keepWidthL, 0, keepLength);
						System.arraycopy(l1.widthL, pos, splitWidthL, 0, splitSize);

						System.arraycopy(l1.widthR, 0, keepWidthR, 0, keepLength);
						System.arraycopy(l1.widthR, pos, splitWidthR, 0, splitSize);
					}

					System.arraycopy(l1.col, 0, keepCol, 0, keepLength);
					System.arraycopy(l1.col, pos, splitCol, 0, splitSize);

					System.arraycopy(l1.row, 0, keepRow, 0, keepLength);
					System.arraycopy(l1.row, pos, splitRow, 0, splitSize);

					System.arraycopy(l1.response, 0, keepResponse, 0, keepLength);
					System.arraycopy(l1.response, pos, splitResponse, 0, splitSize);

					Line lNew = new Line(lines);
					lNew.angle = splitAngle;
					lNew.asymmetry = splitAsymmetry;
					lNew.col = splitCol;
					lNew.row = splitRow;
					lNew.response = splitResponse;
					lNew.intensity = splitIntensity;
					lNew.widthL = splitWidthL;
					lNew.widthR = splitWidthR;
					lNew.num = splitSize;
					lNew.setContourClass(l1.getContourClass());
					lines.add(lNew);
					int newID = lNew.getID();

					// The IDs of all lines connected at this junction point.
					Set<Integer> lineIds = new HashSet<Integer>();
					for (Junction junc : junctionsWithTheSamePosition) {
						lineIds.add(junc.getLine1().getID());
						lineIds.add(junc.getLine2().getID());
					}
					Iterator<Integer> idIt = lineIds.iterator();
					while (idIt.hasNext()) {
						int id = idIt.next();
						int connectWithLineID = lines.getIndexByID(id);
						Line connectWith = lines.get(connectWithLineID);
						Junction j = new Junction();
						j.cont1 = lines.size() - 1;
						j.cont2 = connectWithLineID;
						j.lineCont1 = lNew;
						j.lineCont2 = connectWith;
						j.x = splitPoint.x;
						j.y = splitPoint.y;
						j.pos = lNew.getStartOrdEndPosition(splitPoint.x, splitPoint.y);
						junctions.add(j);
						alreadyProcessedJunctionPoints.add(junctions.size() - 1);
					}

					for (int j = 0; j < junctions.size(); j++) {
						Junction junc2 = junctions.get(j);
						if (junc2.cont1 == splitPoint.cont1 && junc2.pos > splitPoint.pos) {
							junc2.cont1 = lines.getIndexByID(newID);
							junc2.lineCont1 = lNew;
							junc2.pos = junc2.pos - splitPoint.pos;
						}

						double[] min = minDistance(junc2.getLine2(), junc2.x, junc2.y);

						if (junc2.cont2 == splitPoint.cont1 &&
							((int) min[1]) > splitPoint.pos)
						{
							junc2.cont2 = lines.getIndexByID(newID);
							junc2.lineCont2 = lNew;
						}

					}

					l1.angle = keepAngle;
					l1.asymmetry = keepAsymmetry;
					l1.col = keepCol;
					l1.row = keepRow;
					l1.response = keepResponse;
					l1.intensity = keepIntensity;
					l1.widthL = keepWidthL;
					l1.widthR = keepWidthR;
					l1.num = keepLength;

					splitPoint.pos = l1.getStartOrdEndPosition(splitPoint.x,
						splitPoint.y);
					lines.set(splitPoint.cont1, l1);

				}

			}
		}
	}

	private List<Junction> fixJunctions(Lines lines, List<Junction> junctions) {
		/*
		 * For some reason, the x and y coordinates are permuted
		 */
		for (Junction junction : junctions) {
			float help = junction.x;
			junction.x = junction.y;
			junction.y = help;
		}

		List<Junction> newJunctions = new ArrayList<>();
		ArrayList<Point2D.Float> processedJunctions =
			new ArrayList<Point2D.Float>();
		for (int i = 0; i < junctions.size(); i++) {
			Junction junc = junctions.get(i);
			Line mainLine = null;
			int mainLineIndex = -1;
			int mainLinePos = -1;
			ArrayList<Line> secondaryLines = new ArrayList<Line>();
			ArrayList<Integer> secondaryLineIndex = new ArrayList<Integer>();
			ArrayList<Integer> secondaryLinePos = new ArrayList<Integer>();

			// Process each junction position only once.
			if (!processedJunctions.contains(new Point2D.Float(junc.x, junc.y))) {

				processedJunctions.add(new Point2D.Float(junc.x, junc.y));

				// Find the main line and the secondary lines.
				for (int j = 0; j < lines.size(); j++) {
					Line l = lines.get(j);

					double[] mindist = minDistance(l, junc.x, junc.y);
					if (mindist[0] < 0.1) {
						// The junction lies on the line: on a secondary line if it is at
						// its start or end, else on the main line.
						if (mindist[1] == 0 || mindist[1] == (l.num - 1)) {
							secondaryLines.add(l);
							secondaryLineIndex.add(j);
							secondaryLinePos.add((int) mindist[1]);
						}
						else {
							if (mainLine != null) {
								if (mainLine.getID() == l.getID()) {
									continue;
								}
							}
							mainLine = l;
							mainLineIndex = j;
							mainLinePos = (int) mindist[1];

						}
					}
				}
				if (mainLine != null) {
					for (int j = 0; j < secondaryLines.size(); j++) {
						Junction newJunc = new Junction();
						newJunc.cont1 = mainLineIndex;
						newJunc.cont2 = secondaryLineIndex.get(j);
						newJunc.x = junc.x;
						newJunc.y = junc.y;
						newJunc.pos = mainLinePos;
						newJunctions.add(newJunc);

					}
				}
				else {
					// Note: In some cases there is no main line, due to an upstream bug
					// its author could not find; all secondary lines are then paired.
					HashSet<Integer> uniqueIDs = new HashSet<Integer>();
					ArrayList<Line> uniqueLines = new ArrayList<Line>();
					ArrayList<Integer> uniqueLineIndex = new ArrayList<Integer>();
					ArrayList<Integer> uniqueLinePos = new ArrayList<Integer>();
					for (int j = 0; j < secondaryLines.size(); j++) {
						if (!uniqueIDs.contains(secondaryLines.get(j).getID())) {
							uniqueIDs.add(secondaryLines.get(j).getID());
							uniqueLines.add(secondaryLines.get(j));
							uniqueLineIndex.add(secondaryLineIndex.get(j));
							uniqueLinePos.add(secondaryLinePos.get(j));

						}

					}
					for (int j = 0; j < uniqueLines.size(); j++) {
						for (int k = j + 1; k < uniqueLines.size(); k++) {
							Junction newJunc = new Junction();
							newJunc.cont1 = uniqueLineIndex.get(j);
							newJunc.cont2 = uniqueLineIndex.get(k);
							newJunc.x = junc.x;
							newJunc.y = junc.y;
							newJunc.pos = uniqueLinePos.get(j);
							newJunctions.add(newJunc);
							alreadyProcessedJunctionPoints.add(newJunctions.size() - 1);

						}
					}
				}

			}
		}
		return newJunctions;

	}

	private ContourClass reconstructContourClass(Line l, int pos) {
		ContourClass currentClass = l.getContourClass();

		boolean hasJunctionAtStartpoint = pos == 0 ? true : false;
		boolean hasJunctionAtEndpoint = pos == (l.num - 1) ? true : false;

		if (currentClass == ContourClass.NO_JUNCTION && hasJunctionAtStartpoint) {
			return ContourClass.START_JUNCTION;
		}

		if (currentClass == ContourClass.NO_JUNCTION && hasJunctionAtEndpoint) {
			return ContourClass.END_JUNCTION;
		}

		if (currentClass == ContourClass.START_JUNCTION && hasJunctionAtEndpoint) {
			return ContourClass.BOTH_JUNCTIONS;
		}

		if (currentClass == ContourClass.END_JUNCTION && hasJunctionAtStartpoint) {
			return ContourClass.BOTH_JUNCTIONS;
		}

		if (currentClass == ContourClass.CLOSED && (hasJunctionAtEndpoint ||
			hasJunctionAtStartpoint))
		{
			return ContourClass.BOTH_JUNCTIONS;
		}

		return currentClass;

	}

	private double[] minDistance(Line l, float x, float y) {
		double min = Double.MAX_VALUE;
		double index = -1;
		for (int i = 0; i < l.num; i++) {
			double d = Math.sqrt(Math.pow(l.col[i] - x, 2) + Math.pow(l.row[i] - y,
				2));
			if (d < min) {
				min = d;
				index = i;
			}
		}
		double[] ret = { min, index };
		return ret;
	}

	private void deleteJunctions(Lines contours, List<Junction> junctions,
		Line c)
	{
		deleteJunctions(contours, junctions, c, OverlapResolution.NONE);
	}

	private void deleteJunctions(Lines contours, List<Junction> junctions, Line c,
		OverlapResolution overlapOption)
	{

		ArrayList<Junction> remove = new ArrayList<Junction>();
		for (Junction junction : junctions) {
			// Note: With the slope overlap resolution, cont1 and cont2 hold line IDs
			// rather than indices.
			if (overlapOption == OverlapResolution.SLOPE) {
				if (junction.cont1 == c.getID() || junction.cont2 == c.getID()) {
					remove.add(junction);
				}
			}
			else {
				if (contours.get((int) junction.cont1).getID() == c.getID() || contours
					.get((int) junction.cont2).getID() == c.getID())
				{
					remove.add(junction);
				}
			}
		}
		for (Junction junction : remove) {
			junctions.remove(junction);
		}
	}

	private void fixContours(Lines contours, List<Junction> junctions) {

		ArrayList<Line> remove = new ArrayList<Line>();
		// Contours with only a single position cannot be valid.
		for (Line contour : contours) {
			if (contour.num == 1) {
				deleteJunctions(contours, junctions, contour);
				remove.add(contour);
			}
			// Note: The contour class is reconstructed from the junctions later.
			contour.setContourClass(ContourClass.NO_JUNCTION);
		}

		for (Line c : remove) {
			contours.remove(c);
		}

		// Note: For some reason, the first and the last contours can be the same.
		if (contours.size() >= 2) {
			if (contours.get(0).getID() == contours.get(contours.size() - 1)
				.getID())
			{
				contours.remove(contours.size() - 1);
			}
		}
	}

	private void pruneContours(Lines contours, List<Junction> junctions,
		double minLength, double maxLength, OverlapResolution overlapOption)
	{
		ArrayList<Line> remove = new ArrayList<Line>();

		for (Line c : contours) {
			if ((c.estimateLength() < minLength) || (maxLength > 0 && c
				.estimateLength() > maxLength))
			{
				deleteJunctions(contours, junctions, c, overlapOption);
				remove.add(c);
			}
		}
		for (Line c : remove) {

			contours.remove(c);
		}
	}

	private Lines getLines(final float[] image, final int cols, final int rows,
		final double sigma, final double high, final double low,
		final double minLength, final double maxLength, final int mode,
		final boolean doExtendLine, final OverlapResolution overlapOption)
	{
		Lines contours = new Lines();
		List<Junction> resultJunction = new ArrayList<>();
		final MutableInt hnumCont = new MutableInt(0);
		final Position p = new Position();
		p.findLines(image, cols, rows, contours, hnumCont, sigma, low, high, mode,
			doEstimateWidth, doCorrectPosition, doExtendLine, resultJunction);

		fixContours(contours, resultJunction);
		alreadyProcessedJunctionPoints = new HashSet<Integer>();
		// Note: In rare cases, the upstream algorithm corrupts the results (its
		// author could not find the bug), so the solution is reconstructed from
		// the junction points, which are not corrupted.
		resultJunction = fixJunctions(contours, resultJunction);

		assignLinesToJunctions(contours, resultJunction);

		addAdditionalJunctionPointsAndLines(contours, resultJunction);
		Collections.sort(resultJunction);
		junctions = resultJunction;

		// Reconstruct the contour classes.
		for (int i = 0; i < contours.size(); i++) {
			contours.get(i).setContourClass(ContourClass.NO_JUNCTION);
		}

		for (int i = 0; i < contours.size(); i++) {
			boolean isClosedContour = contours.get(i).col[0] == contours.get(
				i).col[contours.get(i).num - 1] && contours.get(i).row[0] == contours
					.get(i).row[contours.get(i).num - 1];
			if (isClosedContour) {
				contours.get(i).setContourClass(ContourClass.CLOSED);
			}
		}

		for (int i = 0; i < junctions.size(); i++) {
			Junction j = junctions.get(i);
			j.getLine1().setContourClass(reconstructContourClass(j.getLine1(),
				j.pos));
			float x = j.getLine1().getXCoordinates()[j.pos];
			float y = j.getLine1().getYCoordinates()[j.pos];
			j.getLine2().setContourClass(reconstructContourClass(j.getLine2(), j
				.getLine2().getStartOrdEndPosition(x, y)));
		}

		if (overlapOption == OverlapResolution.SLOPE) contours =
			new SlopeOverlapResolver().resolve(contours, junctions);

		if (minLength != 0 || maxLength != 0) {
			pruneContours(contours, junctions, minLength, maxLength, overlapOption);
		}

		return contours;

	}

	private void checkSigma(double sigma, int width, int height) {
		int minDim;
		minDim = width < height ? width : height;
		if (sigma < 0.4) throw new IllegalArgumentException("Sigma out of range: " +
			sigma + " < 0.4");
		if (LinesUtil.maskSize(LinesUtil.MAX_SIZE_MASK_2, sigma) >= minDim)
			throw new IllegalArgumentException("Sigma out of range: " + sigma +
				" is too large for the image size");
	}

}
