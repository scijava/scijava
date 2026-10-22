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

import org.scijava.ops.image.gpl.ridge.LinesUtil.MutableDouble;
import org.scijava.ops.image.gpl.ridge.LinesUtil.MutableInt;

/**
 * Extraction of the line width, by locating the edges of each line point along
 * its normal with a facet model edge detector, and the optional correction of
 * the width and position bias, from the ij-ridgedetection {@code Width} class
 * (a translation of the {@code width.c} of Carsten Steger).
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 * @author Carsten Steger
 */
class Width {

	/** A pixel offset. */
	static final class Offset {

		int x, y;
	}

	/*
	 * This constant is introduced because for very narrow lines the facet model
	 * width detection scheme sometimes extracts the line width too narrow. Since
	 * the correction function has a very steep slope in that area, this will lead
	 * to lines of almost zero width, especially since the bilinear interpolation
	 * in correct.c will tend to overcorrect. Therefore it is wise to make the
	 * extracted line width slightly larger before correction.
	 */
	static final double LINE_WIDTH_COMPENSATION = 1.05;

	/* Minimum line width allowed (used for outlier check in fixLocations()) */
	static final double MIN_LINE_WIDTH = 0.1;

	/* Maximum contrast allowed (used for outlier check in fixLocations()) */
	static final double MAX_CONTRAST = 275.0;

	/*
	 * Modified Bresenham algorithm. It returns in line all pixels that are
	 * intersected by a half line less than length away from the point (px,py)
	 * aint the direction (nx,ny). The point (px,py) must lie within the pixel of
	 * the origin, i.e., fabs(px) <= 0.5 and fabs(py) <= 0.5.
	 */
	void bresenham(double nx, double ny, double px, double py, double length,
		Offset[] line, MutableInt numPoints)
	{
		int i, n, x, y, s1, s2, xchg, maxit;
		double e, dx, dy, t;

		x = 0;
		y = 0;
		dx = Math.abs(nx);
		dy = Math.abs(ny);
		s1 = (int) Math.signum(nx);
		s2 = (int) Math.signum(ny);
		px *= s1;
		py *= s2;
		if (dy > dx) {
			t = dx;
			dx = dy;
			dy = t;
			t = px;
			px = py;
			py = t;
			xchg = 1;
		}
		else {
			xchg = 0;
		}
		maxit = (int) Math.ceil(length * dx);
		e = (0.5 - px) * dy / dx - (0.5 - py);
		n = 0;
		for (i = 0; i <= maxit; i++) {
			line[n].x = x;
			line[n].y = y;
			n++;
			while (e >= -1e-8) {
				if (!(xchg == 0)) x += s1;
				else y += s2;
				e--;
				if (e > -1) {
					line[n].x = x;
					line[n].y = y;
					n++;
				}
			}
			if (!(xchg == 0)) y += s2;
			else x += s1;
			e += dy / dx;
		}
		numPoints.setValue(n);
	}

	/*
	 * Fill gaps in the arrays master, slave1, and slave2, i.e., points where
	 * master=0, by interpolation (interior points) or extrapolation (end points).
	 * The array master will usually be the width of the line, while slave1 and
	 * slave2 will be values that depend on master[i] being 0, e.g., the gradient
	 * at each line point. The arrays slave1 and slave2 can be NULL.
	 */
	private void fillGaps(double[] master, double[] slave1, double[] slave2,
		Line cont)
	{
		int i, j, k, s, e;
		int numPoints;
		double mS, mE, s1S, s1E, s2S, s2E, dR, dC, arcLen, len;

		numPoints = cont.num;
		for (i = 0; i < numPoints; i++) {
			if (master[i] == 0) {
				for (j = i + 1; j < numPoints; j++) {
					if (master[j] > 0) break;
				}
				mS = 0;
				mE = 0;
				s1S = 0;
				s1E = 0;
				s2S = 0;
				s2E = 0;
				if (i > 0 && j < numPoints - 1) {
					s = i;
					e = j - 1;
					mS = master[(s - 1)];
					mE = master[(e + 1)];
					if (slave1 != null) {
						s1S = slave1[(s - 1)];
						s1E = slave1[(e + 1)];
					}
					if (slave2 != null) {
						s2S = slave2[(s - 1)];
						s2E = slave2[(e + 1)];
					}
				}
				else if (i > 0) {
					s = i;
					e = numPoints - 2;
					mS = master[(s - 1)];
					mE = master[(s - 1)];
					master[(e + 1)] = mE;
					if (slave1 != null) {
						s1S = slave1[(s - 1)];
						s1E = slave1[(s - 1)];
						slave1[(e + 1)] = s1E;
					}
					if (slave2 != null) {
						s2S = slave2[(s - 1)];
						s2E = slave2[(s - 1)];
						slave2[(e + 1)] = s2E;
					}
				}
				else if (j < numPoints - 1) {
					s = 1;
					e = j - 1;
					mS = master[(e + 1)];
					mE = master[(e + 1)];
					master[(s - 1)] = mS;
					if (slave1 != null) {
						s1S = slave1[(e + 1)];
						s1E = slave1[(e + 1)];
						slave1[(s - 1)] = s1S;
					}
					if (slave2 != null) {
						s2S = slave2[(e + 1)];
						s2E = slave2[(e + 1)];
						slave2[(s - 1)] = s2S;
					}
				}
				else {
					s = 1;
					e = numPoints - 2;
					mS = master[(s - 1)];
					mE = master[(e + 1)];
					if (slave1 != null) {
						s1S = slave1[(s - 1)];
						s1E = slave1[(e + 1)];
					}
					if (slave2 != null) {
						s2S = slave2[(s - 1)];
						s2E = slave2[(e + 1)];
					}
				}
				arcLen = 0;
				for (k = s; k <= e + 1; k++) {
					dR = cont.row[k] - cont.row[(k - 1)];
					dC = cont.col[k] - cont.col[(k - 1)];
					arcLen += Math.sqrt(dR * dR + dC * dC);
				}
				len = 0;
				for (k = s; k <= e; k++) {
					dR = cont.row[k] - cont.row[(k - 1)];
					dC = cont.col[k] - cont.col[(k - 1)];
					len += Math.sqrt(dR * dR + dC * dC);
					master[k] = (arcLen - len) / arcLen * mS + len / arcLen * mE;
					if (slave1 != null) slave1[k] = (arcLen - len) / arcLen * s1S + len /
						arcLen * s1E;
					if (slave2 != null) slave2[k] = (arcLen - len) / arcLen * s2S + len /
						arcLen * s2E;
				}
				i = j;
			}
		}
	}

	/*
	 * Correct the extracted line positions and widths. The algorithm first closes
	 * gaps in the extracted data widthL, widthR, gradL, and gradR to provide
	 * meaningful input over the whole line. Then the correction is calculated.
	 * After this, gaps that have been introduced by the width correction are
	 * again closed. Finally, the position correction is applied if correctPos is
	 * set. The results are returned in widthL, widthR, and cont.
	 */
	private void fixLocations(double[] widthL, double[] widthR, double[] gradL,
		double[] gradR, double[] posX, double[] posY, double[] correction,
		double[] contr, double[] asymm, double sigma, int mode, boolean correctPos,
		Line cont)
	{
		int i;
		int numPoints;
		double px, py;
		double nx, ny;
		double wEst, rEst;
		MutableDouble wReal, hReal, corr;
		wReal = new MutableDouble();
		hReal = new MutableDouble();
		corr = new MutableDouble();
		MutableDouble wStrong = new MutableDouble();
		MutableDouble wWeak = new MutableDouble();
		double correct, asymmetry, response, width, contrast;
		boolean weakIsR;
		boolean correctStart, correctEnd;
		Convol convol = new Convol();
		fillGaps(widthL, gradL, null, cont);
		fillGaps(widthR, gradR, null, cont);

		numPoints = cont.num;

		/* Calculate true line width, asymmetry, and position correction. */
		if (correctPos) {
			/*
			 * Do not correct the position of a junction point if its width is found
			 * by interpolation, i.e., if the position could be corrected differently
			 * for each junction point, thereby destroying the junction.
			 */
			correctStart = ((cont.getContourClass() == ContourClass.NO_JUNCTION ||
				cont.getContourClass() == ContourClass.END_JUNCTION || cont
					.getContourClass() == ContourClass.CLOSED) && (widthR[0] > 0 &&
						widthL[0] > 0));
			correctEnd = ((cont.getContourClass() == ContourClass.NO_JUNCTION || cont
				.getContourClass() == ContourClass.START_JUNCTION || cont
					.getContourClass() == ContourClass.CLOSED) && (widthR[(numPoints -
						1)] > 0 && widthL[(numPoints - 1)] > 0));
			/*
			 * Calculate the true width and assymetry, and its corresponding
			 * correction for each line point.
			 */
			for (i = 0; i < numPoints; i++) {
				if (widthR[i] > 0 && widthL[i] > 0) {
					wEst = (widthR[i] + widthL[i]) * LINE_WIDTH_COMPENSATION;
					if (gradR[i] <= gradL[i]) {
						rEst = gradR[i] / gradL[i];
						weakIsR = true;
					}
					else {
						rEst = gradL[i] / gradR[i];
						weakIsR = false;
					}
					Correct.lineCorrections(sigma, wEst, rEst, wReal, hReal, corr,
						wStrong, wWeak);
					wReal.setValue(wReal.getValue() / LINE_WIDTH_COMPENSATION);
					corr.setValue(corr.getValue() / LINE_WIDTH_COMPENSATION);
					widthR[i] = wReal.getValue();
					widthL[i] = wReal.getValue();
					if (weakIsR) {
						asymm[i] = hReal.getValue();
						correction[i] = -corr.getValue();
					}
					else {
						asymm[i] = -hReal.getValue();
						correction[i] = corr.getValue();
					}
				}
			}

			fillGaps(widthL, correction, asymm, cont);
			for (i = 0; i < numPoints; i++)
				widthR[i] = widthL[i];

			/* Adapt the correction for junction points if necessary. */
			if (!correctStart) correction[0] = 0;
			if (!correctEnd) correction[(numPoints - 1)] = 0;

			for (i = 0; i < numPoints; i++) {
				px = posX[i];
				py = posY[i];
				nx = Math.cos(cont.angle[i]);
				ny = Math.sin(cont.angle[i]);
				px = px + correction[i] * nx;
				py = py + correction[i] * ny;
				posX[i] = px;
				posY[i] = py;
			}
		}

		/* Update the position of a line and add the extracted width. */
		cont.widthL = new float[numPoints];
		cont.widthR = new float[numPoints];
		for (i = 0; i < numPoints; i++) {
			cont.widthL[i] = (float) widthL[i];
			cont.widthR[i] = (float) widthR[i];
			cont.row[i] = (float) posX[i];
			cont.col[i] = (float) posY[i];
		}

		/* Now calculate the true contrast. */
		if (correctPos) {
			cont.asymmetry = new float[numPoints];
			cont.intensity = new float[numPoints];
			for (i = 0; i < numPoints; i++) {
				response = cont.response[i];
				asymmetry = Math.abs(asymm[i]);
				correct = Math.abs(correction[i]);
				width = cont.widthL[i];
				if (width < MIN_LINE_WIDTH) contrast = 0;
				else contrast = (response / Math.abs(convol.phi2(correct + width,
					sigma) + (asymmetry - 1) * convol.phi2(correct - width, sigma)));

				if (contrast > MAX_CONTRAST) contrast = 0;
				contr[i] = contrast;
			}
			fillGaps(contr, null, null, cont);
			for (i = 0; i < numPoints; i++) {
				cont.asymmetry[i] = (float) asymm[i];
				if (mode == LinesUtil.MODE_LIGHT) cont.intensity[i] = (float) contr[i];
				else cont.intensity[i] = (float) -contr[i];
			}
		}
	}

	/*
	 * Extract the line width by using a facet model line detector on an image of
	 * the absolute value of the gradient.
	 */
	void computeLineWidth(float[] dx, float[] dy, int width, int height,
		double sigma, int mode, boolean correctPos, ArrayList<Line> contours,
		MutableInt numContours)
	{
		float[] grad;
		int i, j, k;
		int r, c, l;
		int x, y, dir;
		Offset[] line;
		int maxLine, numLine = 0;
		double length;
		Line cont;
		int numPoints, maxNumPoints;
		double[] widthR, widthL;
		double[] gradR, gradL;
		double[] posX, posY, correct, asymm, contrast;
		double d, dr, dc, drr, drc, dcc;
		double i1, i2, i3, i4, i5, i6, i7, i8, i9;
		double t1, t2, t3, t4, t5, t6;
		double[] eigval = new double[2];
		double[][] eigvec = new double[2][2];
		double a, b, t = 0;
		int num = 0;
		double nx, ny;
		double n1, n2;
		double p1, p2;
		double val;
		double px, py;
		Position p = new Position();
		maxNumPoints = 0;
		for (i = 0; i < numContours.getValue(); i++) {
			numPoints = contours.get(i).num;
			if (numPoints > maxNumPoints) maxNumPoints = numPoints;
		}

		widthL = new double[maxNumPoints];
		widthR = new double[maxNumPoints];
		gradL = new double[maxNumPoints];
		gradR = new double[maxNumPoints];
		posX = new double[maxNumPoints];
		posY = new double[maxNumPoints];
		correct = new double[maxNumPoints];
		contrast = new double[maxNumPoints];
		asymm = new double[maxNumPoints];

		grad = new float[(width * height)];

		length = 2.5 * sigma;
		maxLine = (int) Math.ceil(length * 3);
		line = new Offset[maxLine];
		for (int o = 0; o < line.length; o++) {
			line[o] = new Offset();
		}

		/* Compute the gradient image. */
		for (r = 0; r < height; r++) {
			for (c = 0; c < width; c++) {
				l = LinesUtil.linCoor(r, c, width);
				grad[l] = (float) Math.sqrt(dx[l] * dx[l] + dy[l] * dy[l]);
			}
		}

		for (i = 0; i < numContours.getValue(); i++) {
			cont = contours.get(i);
			numPoints = cont.num;

			for (j = 0; j < numPoints; j++) {
				px = cont.row[j];
				py = cont.col[j];
				posX[j] = px;
				posY[j] = py;
				r = (int) Math.floor(px + 0.5);
				c = (int) Math.floor(py + 0.5);
				nx = Math.cos(cont.angle[j]);
				ny = Math.sin(cont.angle[j]);
				/* Compute the search line. */
				MutableInt numLineh = new MutableInt(numLine);
				bresenham(nx, ny, 0.0, 0.0, length, line, numLineh);
				numLine = numLineh.intValue();
				widthR[j] = widthL[j] = 0;
				/* Look on both sides of the line. */
				for (dir = -1; dir <= 1; dir += 2) {
					for (k = 0; k < numLine; k++) {
						x = LinesUtil.mirrorRow(r + dir * line[k].x, height);
						y = LinesUtil.mirrorCol(c + dir * line[k].y, width);
						i1 = grad[LinesUtil.linCoor(LinesUtil.mirrorRow(x - 1, height),
							LinesUtil.mirrorCol(y - 1, width), width)];
						i2 = grad[LinesUtil.linCoor(LinesUtil.mirrorRow(x - 1, height), y,
							width)];
						i3 = grad[LinesUtil.linCoor(LinesUtil.mirrorRow(x - 1, height),
							LinesUtil.mirrorCol(y + 1, width), width)];
						i4 = grad[LinesUtil.linCoor(x, LinesUtil.mirrorCol(y - 1, width),
							width)];
						i5 = grad[LinesUtil.linCoor(x, y, width)];
						i6 = grad[LinesUtil.linCoor(x, LinesUtil.mirrorCol(y + 1, width),
							width)];
						i7 = grad[LinesUtil.linCoor(LinesUtil.mirrorRow(x + 1, height),
							LinesUtil.mirrorCol(y - 1, width), width)];
						i8 = grad[LinesUtil.linCoor(LinesUtil.mirrorRow(x + 1, height), y,
							width)];
						i9 = grad[LinesUtil.linCoor(LinesUtil.mirrorRow(x + 1, height),
							LinesUtil.mirrorCol(y + 1, width), width)];
						t1 = i1 + i2 + i3;
						t2 = i4 + i5 + i6;
						t3 = i7 + i8 + i9;
						t4 = i1 + i4 + i7;
						t5 = i2 + i5 + i8;
						t6 = i3 + i6 + i9;
						dr = (t3 - t1) / 6;
						dc = (t6 - t4) / 6;
						drr = (t1 - 2 * t2 + t3) / 6;
						dcc = (t4 - 2 * t5 + t6) / 6;
						drc = (i1 - i3 - i7 + i9) / 4;
						p.computeEigenvals(2 * drr, drc, 2 * dcc, eigval, eigvec);
						val = -eigval[0];
						if (val > 0.0) {
							n1 = eigvec[0][0];
							n2 = eigvec[0][1];
							a = 2.0 * (drr * n1 * n1 + drc * n1 * n2 + dcc * n2 * n2);
							b = dr * n1 + dc * n2;
							MutableDouble th = new MutableDouble(t);
							MutableInt numh = new MutableInt(num);
							p.solveLinear(a, b, th, numh);
							t = th.getValue();
							num = numh.getValue();
							if (num != 0) {
								p1 = t * n1;
								p2 = t * n2;
								if (Math.abs(p1) <= 0.5 && Math.abs(p2) <= 0.5) {
									/*
									 * Project the maximum point position perpendicularly onto the
									 * search line.
									 */
									a = 1;
									b = nx * (px - (r + dir * line[k].x + p1)) + ny * (py - (c +
										dir * line[k].y + p2));
									th = new MutableDouble(t);
									numh = new MutableInt(num);
									p.solveLinear(a, b, th, numh);
									t = th.getValue();
									num = numh.getValue();
									d = (-i1 + 2 * i2 - i3 + 2 * i4 + 5 * i5 + 2 * i6 - i7 + 2 *
										i8 - i9) / 9;
									if (dir == 1) {
										gradR[j] = d + p1 * dr + p2 * dc + p1 * p1 * drr + p1 * p2 *
											drc + p2 * p2 * dcc;
										widthR[j] = Math.abs(t);
									}
									else {
										gradL[j] = d + p1 * dr + p2 * dc + p1 * p1 * drr + p1 * p2 *
											drc + p2 * p2 * dcc;
										widthL[j] = Math.abs(t);
									}
									break;
								}
							}
						}
					}
				}
			}

			fixLocations(widthL, widthR, gradL, gradR, posX, posY, correct, contrast,
				asymm, sigma, mode, correctPos, cont);
		}
	}

}
