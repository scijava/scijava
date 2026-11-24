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

import java.util.List;

import org.scijava.ops.image.gpl.ridge.LinesUtil.MutableDouble;
import org.scijava.ops.image.gpl.ridge.LinesUtil.MutableInt;

/**
 * Extraction of line points with sub-pixel accuracy from the Hessian of the
 * image, from the ij-ridgedetection {@code Position} class (a translation of
 * the {@code position.c} of Carsten Steger).
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 * @author Carsten Steger
 */
class Position {

	/*
	 * The pixel boundaries need to be enlarged slightly since in practice it
	 * frequently happens for neighboring pixels a and b that pixel a says a
	 * maximum lies within pixel b and vice versa. This presents no problem since
	 * linking algoritm will take care of this.
	 */
	private static final double PIXEL_BOUNDARY = 0.6;

	/**
	 * Solve the linear equation a*x+b=0 and return the result in t and the number
	 * of solutions in num.
	 */
	void solveLinear(double a, double b, MutableDouble t, MutableInt num) {

		if (a == 0.0) { //
			num.setValue(0);
			return;
		}
		else {
			num.setValue(1);
			t.setValue(-b / a);
			return;
		}
	}

	/**
	 * Compute the eigenvalues and eigenvectors of the Hessian matrix given by
	 * dfdrr, dfdrc, and dfdcc, and sort them in descending order according to
	 * their absolute values.
	 */
	void computeEigenvals(double dfdrr, double dfdrc, double dfdcc,
		double[] eigval, double[][] eigvec)
	{
		double theta, t, c, s, e1, e2, n1, n2;

		/* Compute the eigenvalues and eigenvectors of the Hessian matrix. */
		if (dfdrc != 0.0) {
			theta = 0.5 * (dfdcc - dfdrr) / dfdrc;
			t = 1.0 / (Math.abs(theta) + Math.sqrt(theta * theta + 1.0));
			if (theta < 0.0) t = -t;
			c = 1.0 / Math.sqrt(t * t + 1.0);
			s = t * c;
			e1 = dfdrr - t * dfdrc;
			e2 = dfdcc + t * dfdrc;
		}
		else {
			c = 1.0;
			s = 0.0;
			e1 = dfdrr;
			e2 = dfdcc;
		}
		n1 = c;
		n2 = -s;

		/*
		 * If the absolute value of an eigenvalue is larger than the other, put that
		 * eigenvalue into first position. If both are of equal absolute value, put
		 * the negative one first.
		 */
		if (Math.abs(e1) > Math.abs(e2)) {
			eigval[0] = e1;
			eigval[1] = e2;
			eigvec[0][0] = n1;
			eigvec[0][1] = n2;
			eigvec[1][0] = -n2;
			eigvec[1][1] = n1;
		}
		else if (Math.abs(e1) < Math.abs(e2)) {
			eigval[0] = e2;
			eigval[1] = e1;
			eigvec[0][0] = -n2;
			eigvec[0][1] = n1;
			eigvec[1][0] = n1;
			eigvec[1][1] = n2;
		}
		else {
			if (e1 < e2) {
				eigval[0] = e1;
				eigval[1] = e2;
				eigvec[0][0] = n1;
				eigvec[0][1] = n2;
				eigvec[1][0] = -n2;
				eigvec[1][1] = n1;
			}
			else {
				eigval[0] = e2;
				eigval[1] = e1;
				eigvec[0][0] = -n2;
				eigvec[0][1] = n1;
				eigvec[1][0] = n1;
				eigvec[1][1] = n2;
			}
		}
	}

	/*
	 * For each point in the image determine whether there is a local maximum of
	 * the second directional derivative in the direction (nx[l],ny[l]) within the
	 * pixels's boundaries. If so, set ismax[l] to 2 if the eigenvalue ev[l] is
	 * larger than high, to 1 if ev[l] is larger than low, and to 0 otherwise.
	 * Furthermore, put the sub-pixel position of the maximum into (px[l],py[l]).
	 * The parameter mode determines whether maxima (dark lines points) or minima
	 * (bright line points) should be selected. The partial derivatives of the
	 * image are input as ku[].
	 */
	private void computeLinePoints(float[][] ku, byte[] ismax, float[] ev,
		float[] nx, float[] ny, float[] px, float[] py, int width, int height,
		double low, double high, int mode)
	{
		int r, c, l;
		double[] k = new double[5];
		double[] eigval = new double[2];
		double[][] eigvec = new double[2][2];
		double a, b;
		MutableDouble t = new MutableDouble();
		MutableInt num = new MutableInt();
		double n1, n2;
		double p1, p2;
		double val;

		for (r = 0; r < height; r++) {
			for (c = 0; c < width; c++) {
				l = LinesUtil.linCoor(r, c, width);

				k[0] = ku[0][l];
				k[1] = ku[1][l];
				k[2] = ku[2][l];
				k[3] = ku[3][l];
				k[4] = ku[4][l];
				ev[l] = (float) 0.0;
				nx[l] = (float) 0.0;
				ny[l] = (float) 0.0;
				computeEigenvals(k[2], k[3], k[4], eigval, eigvec);
				if (mode == LinesUtil.MODE_LIGHT) val = -eigval[0];
				else val = eigval[0];
				if (val > 0.0) {
					ev[l] = (float) val;
					n1 = eigvec[0][0];
					n2 = eigvec[0][1];
					a = k[2] * n1 * n1 + 2.0 * k[3] * n1 * n2 + k[4] * n2 * n2;
					b = k[0] * n1 + k[1] * n2;
					solveLinear(a, b, t, num);
					if (num.intValue() != 0) {
						p1 = t.doubleValue() * n1;
						p2 = t.doubleValue() * n2;
						if (Math.abs(p1) <= PIXEL_BOUNDARY && Math.abs(
							p2) <= PIXEL_BOUNDARY)
						{
							if (val >= low) {
								if (val >= high) ismax[l] = 2;
								else ismax[l] = 1;
							}
							nx[l] = (float) n1;
							ny[l] = (float) n2;
							px[l] = (float) (r + p1);
							py[l] = (float) (c + p2);
						}
					}
				}
			}
		}
	}

	/*
	 * Main routine to detect lines in an image of dimension width * height. The
	 * extracted lines are returned in result, while numResult is the number of
	 * detected lines. The parameter sigma is the amount of smoothing that the
	 * Gaussian kernel performs, while low and high are the hysteresis thresholds
	 * used in the linking algorithm. With mode, either bright or dark lines can
	 * be selected. The parameter computeWidth determines whether the line width
	 * should be extracted, while correctPos determines whether the line width and
	 * position correction should be applied.
	 */
	void findLines(float[] image, int width, int height, Lines contours,
		MutableInt numResult, double sigma, double low, double high, int mode,
		boolean computeWidth, boolean correctPos, boolean extendLines,
		List<Junction> junctions)
	{
		byte[] ismax;
		float[] ev, n1, n2, p1, p2;
		float[][] k = new float[5][(width * height)];
		Convol convol = new Convol();
		convol.convolveGauss(image, k[0], width, height, sigma, LinesUtil.DERIV_R);
		convol.convolveGauss(image, k[1], width, height, sigma, LinesUtil.DERIV_C);
		convol.convolveGauss(image, k[2], width, height, sigma, LinesUtil.DERIV_RR);
		convol.convolveGauss(image, k[3], width, height, sigma, LinesUtil.DERIV_RC);

		convol.convolveGauss(image, k[4], width, height, sigma, LinesUtil.DERIV_CC);

		ismax = new byte[(width * height)];
		ev = new float[(width * height)];
		n1 = new float[(width * height)];
		n2 = new float[(width * height)];
		p1 = new float[(width * height)];
		p2 = new float[(width * height)];

		computeLinePoints(k, ismax, ev, n1, n2, p1, p2, width, height, low, high,
			mode);

		Link l = new Link();
		l.computeContours(ismax, ev, n1, n2, p1, p2, k[0], k[1], contours,
			numResult, sigma, extendLines, mode, low, high, width, height, junctions);

		Width w = new Width();
		if (computeWidth) w.computeLineWidth(k[0], k[1], width, height, sigma, mode,
			correctPos, contours, numResult);

	}

}
