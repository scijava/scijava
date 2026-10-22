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
import org.scijava.ops.image.gpl.ridge.Threshold.Chord;
import org.scijava.ops.image.gpl.ridge.Threshold.Region;
import org.scijava.ops.image.gpl.ridge.Width.Offset;

/**
 * Linking of line points into lines with hysteresis thresholds, detection of
 * junctions, extension of line ends to find additional junctions, and splitting
 * of lines at junctions, from the ij-ridgedetection {@code Link} class (a
 * translation of the {@code link.c} of Carsten Steger).
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 * @author Thorsten Wagner
 * @author Carsten Steger
 */
class Link {

	static final double MAX_ANGLE_DIFFERENCE = Math.PI / 6.0;

	/*
	 * This data structure facilitates the quick search for the next possible
	 * starting point of a line. An array of crossrefs will be accumulated and
	 * sorted according to its value. x and y are the coordinates of a point in
	 * the image. When this point has been processed it will be marked as done.
	 */
	private static final class Crossref implements Comparable<Crossref> {

		short x;

		short y;

		double value;

		boolean done;

		/* Sort by decreasing value. */
		@Override
		public int compareTo(final Crossref arg0) {
			if (this.value > arg0.value) return -1;
			if (this.value < arg0.value) return 1;
			return 0;
		}
	}

	/*
	 * This table contains the three appropriate neighbor pixels that the linking
	 * algorithm must examine. It is indexed by the octant the current line angle
	 * lies in, e.g., 0 if the angle in degrees lies within [-22.5,22.5].
	 */
	final static int[][][] dirtab = new int[][][] { { { 1, 0 }, { 1, -1 }, { 1,
		1 } }, { { 1, 1 }, { 1, 0 }, { 0, 1 } }, { { 0, 1 }, { 1, 1 }, { -1, 1 } },
		{ { -1, 1 }, { 0, 1 }, { -1, 0 } }, { { -1, 0 }, { -1, 1 }, { -1, -1 } }, {
			{ -1, -1 }, { -1, 0 }, { 0, -1 } }, { { 0, -1 }, { -1, -1 }, { 1, -1 } },
		{ { 1, -1 }, { 0, -1 }, { 1, 0 } } };

	/*
	 * This table contains the two neighbor pixels that the linking algorithm
	 * should examine and mark as processed in case there are double responses.
	 */
	final static int[][][] cleartab = new int[][][] { { { 0, 1 }, { 0, -1 } }, { {
		-1, 1 }, { 1, -1 } }, { { -1, 0 }, { 1, 0 } }, { { -1, -1 }, { 1, 1 } }, { {
			0, -1 }, { 0, 1 } }, { { 1, -1 }, { -1, 1 } }, { { 1, 0 }, { -1, 0 } }, {
				{ 1, 1 }, { -1, -1 } } };

	/*
	 * Compute the response of the operator with sub-pixel accuracy by using the
	 * facet model to interpolate the pixel accurate responses.
	 */
	private double interpolateResponse(float[] resp, int x, int y, double px,
		double py, int width, int height)
	{
		double i1, i2, i3, i4, i5, i6, i7, i8, i9;
		double t1, t2, t3, t4, t5, t6;
		double d, dr, dc, drr, drc, dcc;
		double xx, yy;

		i1 = resp[LinesUtil.linCoor(LinesUtil.mirrorRow(x - 1, height), LinesUtil
			.mirrorCol(y - 1, width), width)];
		i2 = resp[LinesUtil.linCoor(LinesUtil.mirrorRow(x - 1, height), y, width)];
		i3 = resp[LinesUtil.linCoor(LinesUtil.mirrorRow(x - 1, height), LinesUtil
			.mirrorCol(y + 1, width), width)];
		i4 = resp[LinesUtil.linCoor(x, LinesUtil.mirrorCol(y - 1, width), width)];
		i5 = resp[LinesUtil.linCoor(x, y, width)];
		i6 = resp[LinesUtil.linCoor(x, LinesUtil.mirrorCol(y + 1, width), width)];
		i7 = resp[LinesUtil.linCoor(LinesUtil.mirrorRow(x + 1, height), LinesUtil
			.mirrorCol(y - 1, width), width)];
		i8 = resp[LinesUtil.linCoor(LinesUtil.mirrorRow(x + 1, height), y, width)];
		i9 = resp[LinesUtil.linCoor(LinesUtil.mirrorRow(x + 1, height), LinesUtil
			.mirrorCol(y + 1, width), width)];
		t1 = i1 + i2 + i3;
		t2 = i4 + i5 + i6;
		t3 = i7 + i8 + i9;
		t4 = i1 + i4 + i7;
		t5 = i2 + i5 + i8;
		t6 = i3 + i6 + i9;
		d = (-i1 + 2 * i2 - i3 + 2 * i4 + 5 * i5 + 2 * i6 - i7 + 2 * i8 - i9) / 9;
		dr = (t3 - t1) / 6;
		dc = (t6 - t4) / 6;
		drr = (t1 - 2 * t2 + t3) / 6;
		dcc = (t4 - 2 * t5 + t6) / 6;
		drc = (i1 - i3 - i7 + i9) / 4;
		xx = px - x;
		yy = py - y;
		return d + xx * dr + yy * dc + xx * xx * drr + xx * yy * drc + yy * yy *
			dcc;
	}

	/*
	 * Calculate the closest point to (px,py) on the line (lx,ly) + t*(dx,dy) and
	 * return the result in (cx,cy), plus the parameter in t.
	 */
	private void closestPoint(double lx, double ly, double dx, double dy,
		double px, double py, MutableDouble cx, MutableDouble cy, MutableDouble t)
	{
		double mx, my, den, nom, tt;
		mx = px - lx;
		my = py - ly;
		den = dx * dx + dy * dy;
		nom = mx * dx + my * dy;
		if (den != 0) tt = nom / den;
		else tt = 0;
		cx.setValue(lx + tt * dx);
		cy.setValue(ly + tt * dy);
		t.setValue(tt);
	}

	/*
	 * Interpolate the gradient of the gradient images gradx and grady with width
	 * width at the point (px,py) using linear interpolation, and return the
	 * result in (gx,gy).
	 */
	private void interpolateGradient(float[] gradx, float[] grady, double px,
		double py, int width, MutableDouble gx, MutableDouble gy)
	{
		int gix, giy, gpos;
		double gfx, gfy, gx1, gy1, gx2, gy2, gx3, gy3, gx4, gy4;

		gix = (int) Math.floor(px);
		giy = (int) Math.floor(py);

		gfx = px % 1.0;;
		gfy = py % 1.0;
		gpos = LinesUtil.linCoor(gix, giy, width);
		gx1 = gradx[gpos];
		gy1 = grady[gpos];
		gpos = LinesUtil.linCoor(gix + 1, giy, width);
		gx2 = gradx[gpos];
		gy2 = grady[gpos];
		gpos = LinesUtil.linCoor(gix, giy + 1, width);
		gx3 = gradx[gpos];
		gy3 = grady[gpos];
		gpos = LinesUtil.linCoor(gix + 1, giy + 1, width);
		gx4 = gradx[gpos];
		gy4 = grady[gpos];
		gx.setValue((1 - gfy) * ((1 - gfx) * gx1 + gfx * gx2) + gfy * ((1 - gfx) *
			gx3 + gfx * gx4));
		gy.setValue((1 - gfy) * ((1 - gfx) * gy1 + gfx * gy2) + gfy * ((1 - gfx) *
			gy3 + gfx * gy4));
	}

	/*
	 * This function links the line points into lines. The input to this function
	 * are the response of the filter, i.e., the second directional derivative
	 * along (nx[l],ny[l]), contained in eigval[l], and the sub-pixel position of
	 * each line point, contained in (px[l],py[l]). The parameters low and high
	 * are the hysteresis thresholds for the linking, while width and height are
	 * the dimensions of the five float-images. The linked lines are returned in
	 * result, and the number of lines detected is returned in numResult.
	 */
	void computeContours(byte[] ismax, float[] eigval, float[] normx,
		float[] normy, float[] posx, float[] posy, float[] gradx, float[] grady,
		Lines contours, MutableInt numResult, double sigma, boolean extendLines,
		int mode, double low, double high, int width, int height,
		List<Junction> junctions)
	{
		int i = 0, j = 0, k, l, it, pos, nextpos, nexti;
		int begin, end;
		int x, y;
		int octant, lastOctant;
		int[] label;
		int numCont, numPnt;
		int sizeCont, sizePnt;
		float[] row, col, trow, tcol;
		float[] angle, tangle;
		float[] resp, tresp;
		Junction[] junc;
		int numJunc, sizeJunc;
		Line[] cont;
		Line tmpCont;
		ContourClass cls;
		double max;
		int maxx, maxy;
		int nextx, nexty;
		double nx, ny;
		double alpha, nextalpha, diff, mindiff, dist, mindist;
		double beta, lastBeta, diff1, diff2;
		double px, py, nextpx = 0, nextpy = 0;
		double dx, dy;
		float tmp;
		int area;
		int[] indx;
		int indxMax;
		boolean nextismax;
		Crossref[] cross;
		int m = 0, maxLine, numAdd;
		MutableInt numLine = new MutableInt();
		double length, response;
		Offset[] line;
		double mx, my, gx, gy, s, endAngle = 0, endResp = 0;
		MutableDouble t = new MutableDouble();
		float[] extx, exty;
		boolean addExt;
		Region seg = new Region();
		Chord[] rl;
		Width w = new Width();

		/*
		 * The image label contains information on the pixels that have been
		 * processed by the linking algorithm.
		 */
		label = new int[(width * height)];

		/*
		 * The image indx is an index into the table of all pixels that possibly
		 * could be starting points for new lines. It is used to quickly determine
		 * the next starting point of a line.
		 */
		indx = new int[(width * height)];
		java.util.Arrays.fill(indx, 0);

		numCont = 0;
		numJunc = 0;
		sizeCont = LinesUtil.INITIAL_SIZE;
		sizePnt = LinesUtil.INITIAL_SIZE;
		sizeJunc = LinesUtil.INITIAL_SIZE;
		cont = new Line[sizeCont];
		for (int o = 0; o < cont.length; o++) {
			cont[o] = new Line(contours);
		}
		row = new float[sizePnt];
		col = new float[sizePnt];
		angle = new float[sizePnt];
		resp = new float[sizePnt];
		junc = new Junction[sizeJunc];
		for (int o = 0; o < junc.length; o++) {
			junc[o] = new Junction();
		}

		/* Select all pixels that can be starting points for lines. */
		Threshold.threshold(ismax, 2, width, height, seg);

		/* Count the number of possible starting points. */
		area = 0;
		for (i = 0; i < seg.num; i++)
			area += seg.rl[i].ce - seg.rl[i].cb + 1;

		/* Create the index of possible starting points. */
		cross = new Crossref[area];
		for (int o = 0; o < cross.length; o++) {
			cross[o] = new Crossref();
		}
		k = 0;
		rl = seg.rl;
		for (i = 0; i < seg.num; i++) {
			x = rl[i].r;
			for (y = rl[i].cb; y <= rl[i].ce; y++) {
				pos = LinesUtil.linCoor(x, y, width);
				cross[k].x = (short) x;
				cross[k].y = (short) y;
				cross[k].value = eigval[pos];
				cross[k].done = false;
				k++;
			}
		}

		java.util.Arrays.sort(cross);
		for (i = 0; i < area; i++)
			indx[LinesUtil.linCoor(cross[i].x, cross[i].y, width)] = i + 1;

		/* Link lines points. */
		indxMax = 0;
		for (;;) {
			/*
			 * Contour class unknown at this point; therefore assume both ends free.
			 */
			cls = ContourClass.NO_JUNCTION;
			/* Search for next starting point. */
			while (indxMax < area && cross[indxMax].done)
				indxMax++;
			/* Stop if no feasible starting point exists. */
			if (indxMax == area) break;
			max = cross[indxMax].value;
			maxx = cross[indxMax].x;
			maxy = cross[indxMax].y;
			if (max == 0.0) break;

			/* Add starting point to the line. */
			numPnt = 0;
			pos = LinesUtil.linCoor(maxx, maxy, width);
			label[pos] = (numCont + 1);
			if (!(indx[pos] == 0)) cross[(indx[pos] - 1)].done = true;
			row[numPnt] = posx[pos];
			col[numPnt] = posy[pos];
			/* Select line direction. */
			nx = -normy[pos];
			ny = normx[pos];
			alpha = Math.atan2(ny, nx);
			if (alpha < 0.0) alpha += 2.0 * Math.PI;
			if (alpha >= Math.PI) alpha -= Math.PI;
			octant = (int) (Math.floor(4.0 / Math.PI * alpha + 0.5)) % 4;
			/*
			 * Select normal to the line. The normal points to the right of the line
			 * as the line is traversed from 0 to num-1. Since the points are sorted
			 * in reverse order before the second iteration, the first beta actually
			 * has to point to the left of the line!
			 */
			beta = alpha + Math.PI / 2.0;
			if (beta >= 2.0 * Math.PI) beta -= 2.0 * Math.PI;
			angle[numPnt] = (float) beta;
			resp[numPnt] = (float) interpolateResponse(eigval, maxx, maxy, posx[pos],
				posy[pos], width, height);
			numPnt++;
			/* Mark double responses as processed. */
			for (i = 0; i < 2; i++) {
				nextx = maxx + cleartab[octant][i][0];
				nexty = maxy + cleartab[octant][i][1];
				if (nextx < 0 || nextx >= height || nexty < 0 || nexty >= width)
					continue;
				nextpos = LinesUtil.linCoor(nextx, nexty, width);
				if (ismax[nextpos] > 0) {
					nx = -normy[nextpos];
					ny = normx[nextpos];
					nextalpha = Math.atan2(ny, nx);
					if (nextalpha < 0.0) nextalpha += 2.0 * Math.PI;
					if (nextalpha >= Math.PI) nextalpha -= Math.PI;
					diff = Math.abs(alpha - nextalpha);
					if (diff >= Math.PI / 2.0) diff = Math.PI - diff;
					if (diff < MAX_ANGLE_DIFFERENCE) {
						label[nextpos] = (numCont + 1);
						if (!(indx[nextpos] == 0)) cross[(indx[nextpos] - 1)].done = true;
					}
				}
			}

			for (it = 1; it <= 2; it++) {
				if (it == 1) {
					/*
					 * Search along the initial line direction in the first iteration.
					 */
					x = maxx;
					y = maxy;
					pos = LinesUtil.linCoor(x, y, width);
					nx = -normy[pos];
					ny = normx[pos];
					alpha = Math.atan2(ny, nx);
					if (alpha < 0.0) alpha += 2.0 * Math.PI;
					if (alpha >= Math.PI) alpha -= Math.PI;
					lastOctant = (int) (Math.floor(4.0 / Math.PI * alpha + 0.5)) % 4;
					lastBeta = alpha + Math.PI / 2.0;
					if (lastBeta >= 2.0 * Math.PI) lastBeta -= 2.0 * Math.PI;
				}
				else {
					/* Search in the opposite direction in the second iteration. */
					x = maxx;
					y = maxy;
					pos = LinesUtil.linCoor(x, y, width);
					nx = -normy[pos];
					ny = normx[pos];
					alpha = Math.atan2(ny, nx);
					if (alpha < 0.0) alpha += 2.0 * Math.PI;
					if (alpha >= Math.PI) alpha -= Math.PI;
					lastOctant = (int) (Math.floor(4.0 / Math.PI * alpha + 0.5)) % 4 + 4;
					lastBeta = alpha + Math.PI / 2.0;
					if (lastBeta >= 2.0 * Math.PI) lastBeta -= 2.0 * Math.PI;
				}
				if (it == 2) {
					/* Sort the points found in the first iteration in reverse. */
					for (i = 0; i < numPnt / 2; i++) {
						tmp = row[i];
						row[i] = row[(numPnt - 1 - i)];
						row[(numPnt - 1 - i)] = tmp;
						tmp = col[i];
						col[i] = col[(numPnt - 1 - i)];
						col[(numPnt - 1 - i)] = tmp;
						tmp = angle[i];
						angle[i] = angle[(numPnt - 1 - i)];
						angle[(numPnt - 1 - i)] = tmp;
						tmp = resp[i];
						resp[i] = resp[(numPnt - 1 - i)];
						resp[(numPnt - 1 - i)] = tmp;
					}
				}

				/* Now start adding appropriate neighbors to the line. */
				for (;;) {
					pos = LinesUtil.linCoor(x, y, width);
					nx = -normy[pos];
					ny = normx[pos];
					px = posx[pos];
					py = posy[pos];
					/* Orient line direction w.r.t. the last line direction. */
					alpha = Math.atan2(ny, nx);
					if (alpha < 0.0) alpha += 2.0 * Math.PI;
					if (alpha >= Math.PI) alpha -= Math.PI;
					octant = (int) (Math.floor(4.0 / Math.PI * alpha + 0.5)) % 4;
					switch (octant) {
						case 0:
							if (lastOctant >= 3 && lastOctant <= 5) octant = 4;
							break;
						case 1:
							if (lastOctant >= 4 && lastOctant <= 6) octant = 5;
							break;
						case 2:
							if (lastOctant >= 4 && lastOctant <= 7) octant = 6;
							break;
						case 3:
							if (lastOctant == 0 || lastOctant >= 6) octant = 7;
							break;
					}
					lastOctant = octant;

					/* Determine appropriate neighbor. */
					nextismax = false;
					nexti = 1;
					mindiff = Double.MAX_VALUE;
					for (i = 0; i < 3; i++) {
						nextx = x + dirtab[octant][i][0];
						nexty = y + dirtab[octant][i][1];
						if (nextx < 0 || nextx >= height || nexty < 0 || nexty >= width)
							continue;
						nextpos = LinesUtil.linCoor(nextx, nexty, width);
						if (ismax[nextpos] == 0) continue;
						nextpx = posx[nextpos];
						nextpy = posy[nextpos];
						dx = nextpx - px;
						dy = nextpy - py;
						dist = Math.sqrt(dx * dx + dy * dy);
						nx = -normy[nextpos];
						ny = normx[nextpos];
						nextalpha = Math.atan2(ny, nx);
						if (nextalpha < 0.0) nextalpha += 2.0 * Math.PI;
						if (nextalpha >= Math.PI) nextalpha -= Math.PI;
						diff = Math.abs(alpha - nextalpha);
						if (diff >= Math.PI / 2.0) diff = Math.PI - diff;
						diff = dist + diff;
						if (diff < mindiff) {
							mindiff = diff;
							nexti = i;
						}
						if (!(ismax[nextpos] == 0)) nextismax = true;
					}

					/* Mark double responses as processed. */
					for (i = 0; i < 2; i++) {
						nextx = x + cleartab[octant][i][0];
						nexty = y + cleartab[octant][i][1];
						if (nextx < 0 || nextx >= height || nexty < 0 || nexty >= width)
							continue;
						nextpos = LinesUtil.linCoor(nextx, nexty, width);
						if (ismax[nextpos] > 0) {
							nx = -normy[nextpos];
							ny = normx[nextpos];
							nextalpha = Math.atan2(ny, nx);
							if (nextalpha < 0.0) nextalpha += 2.0 * Math.PI;
							if (nextalpha >= Math.PI) nextalpha -= Math.PI;
							diff = Math.abs(alpha - nextalpha);
							if (diff >= Math.PI / 2.0) diff = Math.PI - diff;
							if (diff < MAX_ANGLE_DIFFERENCE) {
								label[nextpos] = (numCont + 1);
								if (!(indx[nextpos] == 0)) cross[(indx[nextpos] - 1)].done =
									true;
							}
						}
					}

					/* Have we found the end of the line? */
					if (!nextismax) break;
					/* If not, add the neighbor to the line. */
					x += dirtab[octant][nexti][0];
					y += dirtab[octant][nexti][1];
					if (numPnt >= sizePnt) {
						sizePnt = (int) Math.floor((double) (sizePnt *
							LinesUtil.REALLOC_FACTOR));
						float[] newArr = new float[sizePnt];
						for (int o = 0; o < row.length; o++) {
							newArr[o] = row[o];
						}
						row = newArr;

						newArr = new float[sizePnt];
						for (int o = 0; o < col.length; o++) {
							newArr[o] = col[o];
						}
						col = newArr;

						newArr = new float[sizePnt];
						for (int o = 0; o < angle.length; o++) {
							newArr[o] = angle[o];
						}
						angle = newArr;

						newArr = new float[sizePnt];
						for (int o = 0; o < resp.length; o++) {
							// Note: the plugin assigns resp[o] = resp[o] here, losing the
							// responses of long lines.
							newArr[o] = resp[o];
						}
						resp = newArr;
					}
					pos = LinesUtil.linCoor(x, y, width);
					row[numPnt] = posx[pos];
					col[numPnt] = posy[pos];

					/*
					 * Orient normal to the line direction w.r.t. the last normal.
					 */
					nx = normx[pos];
					ny = normy[pos];
					beta = Math.atan2(ny, nx);
					if (beta < 0.0) beta += 2.0 * Math.PI;
					if (beta >= Math.PI) beta -= Math.PI;
					diff1 = Math.abs(beta - lastBeta);
					if (diff1 >= Math.PI) diff1 = 2.0 * Math.PI - diff1;
					diff2 = Math.abs(beta + Math.PI - lastBeta);
					if (diff2 >= Math.PI) diff2 = 2.0 * Math.PI - diff2;
					if (diff1 < diff2) {
						angle[numPnt] = (float) beta;
						lastBeta = beta;
					}
					else {
						angle[numPnt] = (float) (beta + Math.PI);
						lastBeta = beta + Math.PI;
					}

					resp[numPnt] = (float) interpolateResponse(eigval, x, y, posx[pos],
						posy[pos], width, height);
					numPnt++;

					/*
					 * If the appropriate neighbor is already processed a junction point
					 * is found.
					 */
					if (label[pos] > 0) {
						if (numJunc >= sizeJunc) {
							sizeJunc = (int) Math.floor((double) (sizeJunc *
								LinesUtil.REALLOC_FACTOR));
							Junction[] junch = new Junction[sizeJunc];
							for (int o = 0; o < junch.length; o++) {
								if (o < junc.length) junch[o] = junc[o];
								else junch[o] = new Junction();
							}
							junc = junch;
						}
						/* Look for the junction point in the other line. */
						k = label[pos] - 1;
						if (k == numCont) {
							/* Line intersects itself. */
							for (j = 0; j < numPnt - 1; j++) {
								if (row[j] == posx[pos] && col[j] == posy[pos]) {
									if (j == 0) {
										/* Contour is closed. */
										cls = ContourClass.CLOSED;
										for (i = 0; i < numPnt / 2; i++) {
											tmp = row[i];
											row[i] = row[(numPnt - 1 - i)];
											row[(numPnt - 1 - i)] = tmp;
											tmp = col[i];
											col[i] = col[(numPnt - 1 - i)];
											col[(numPnt - 1 - i)] = tmp;
											tmp = angle[i];
											angle[i] = angle[(numPnt - 1 - i)];
											angle[(numPnt - 1 - i)] = tmp;
											tmp = resp[i];
											resp[i] = resp[(numPnt - 1 - i)];
											resp[(numPnt - 1 - i)] = tmp;
										}
										it = 2;
									}
									else {
										if (it == 2) {
											/* Determine contour class. */
											if (cls == ContourClass.START_JUNCTION) cls =
												ContourClass.BOTH_JUNCTIONS;
											else cls = ContourClass.END_JUNCTION;
											/* Index j is the correct index. */
											junc[numJunc].cont1 = numCont;
											junc[numJunc].cont2 = numCont;
											junc[numJunc].pos = j;
											junc[numJunc].x = posx[pos];
											junc[numJunc].y = posy[pos];
											numJunc++;
										}
										else {
											/* Determine contour class. */
											cls = ContourClass.START_JUNCTION;
											/*
											 * Index numPnt-1-j is the correct index since the line is
											 * going to be sorted in reverse.
											 */
											junc[numJunc].cont1 = numCont;
											junc[numJunc].cont2 = numCont;
											junc[numJunc].pos = numPnt - 1 - j;
											junc[numJunc].x = posx[pos];
											junc[numJunc].y = posy[pos];
											numJunc++;
										}
									}
									break;
								}
							}
							/*
							 * Mark this case as being processed for the algorithm below.
							 */
							j = -1;
						}
						else {

							for (j = 0; j < cont[k].num; j++) {
								if (cont[k].row[j] == posx[pos] && cont[k].col[j] == posy[pos])
									break;
							}
							/*
							 * If no point can be found on the other line a double response
							 * must have occured. In this case, find the nearest point on the
							 * other line and add it to the current line.
							 */
							if (j == cont[k].num) {
								mindist = Double.MAX_VALUE;
								j = -1;
								for (l = 0; l < cont[k].num; l++) {
									dx = posx[pos] - cont[k].row[l];
									dy = posy[pos] - cont[k].col[l];
									dist = Math.sqrt(dx * dx + dy * dy);
									if (dist < mindist) {
										mindist = dist;
										j = l;
									}
								}
								/*
								 * Add the point with index j to the current line.
								 */
								if (numPnt >= sizePnt) {
									sizePnt = (int) Math.floor((double) (sizePnt *
										LinesUtil.REALLOC_FACTOR));
									float[] newArr = new float[sizePnt];
									for (int o = 0; o < row.length; o++) {
										newArr[o] = row[o];
									}
									row = newArr;
									newArr = new float[sizePnt];
									for (int o = 0; o < col.length; o++) {
										newArr[o] = col[o];
									}
									col = newArr;
									newArr = new float[sizePnt];
									for (int o = 0; o < angle.length; o++) {
										newArr[o] = angle[o];
									}
									angle = newArr;
									newArr = new float[sizePnt];
									for (int o = 0; o < resp.length; o++) {
										// Note: the plugin assigns resp[o] = resp[o] here.
										newArr[o] = resp[o];
									}
									resp = newArr;
								}

								row[numPnt] = cont[k].row[j];
								col[numPnt] = cont[k].col[j];
								beta = cont[k].angle[j];
								if (beta >= Math.PI) beta -= Math.PI;
								diff1 = Math.abs(beta - lastBeta);
								if (diff1 >= Math.PI) diff1 = 2.0 * Math.PI - diff1;
								diff2 = Math.abs(beta + Math.PI - lastBeta);
								if (diff2 >= Math.PI) diff2 = 2.0 * Math.PI - diff2;
								if (diff1 < diff2) angle[numPnt] = (float) beta;
								else angle[numPnt] = (float) (beta + Math.PI);
								resp[numPnt] = cont[k].response[j];
								numPnt++;
							}
						}
						/*
						 * Add the junction point only if it is not one of the other line's
						 * endpoints.
						 */
						if (j > 0 && j < cont[k].num - 1) {
							/* Determine contour class. */
							if (it == 1) cls = ContourClass.START_JUNCTION;
							else if (cls == ContourClass.START_JUNCTION) cls =
								ContourClass.BOTH_JUNCTIONS;
							else cls = ContourClass.END_JUNCTION;
							/* Add the new junction. */

							junc[numJunc].cont1 = k;
							junc[numJunc].cont2 = numCont;
							junc[numJunc].pos = j;
							junc[numJunc].x = row[(numPnt - 1)];
							junc[numJunc].y = col[(numPnt - 1)];
							numJunc++;
						}
						break;
					}
					label[pos] = (numCont + 1);
					if (!(indx[pos] == 0)) cross[(indx[pos] - 1)].done = true;
				}
			}

			if (numPnt > 1) {
				/* Only add lines with at least two points. */
				if (numCont >= sizeCont) {
					sizeCont = (int) Math.floor((double) (sizeCont *
						LinesUtil.REALLOC_FACTOR));
					Line[] conth = new Line[sizeCont];
					for (int o = 0; o < conth.length; o++) {
						if (o < cont.length) conth[o] = cont[o];
						else conth[o] = new Line(contours);
					}
					cont = conth;
				}
				cont[numCont] = new Line(contours);

				cont[numCont].row = java.util.Arrays.copyOf(row, numPnt);
				cont[numCont].col = java.util.Arrays.copyOf(col, numPnt);
				cont[numCont].angle = java.util.Arrays.copyOf(angle, numPnt);
				cont[numCont].response = java.util.Arrays.copyOf(resp, numPnt);

				cont[numCont].widthR = null;
				cont[numCont].widthL = null;
				cont[numCont].asymmetry = null;
				cont[numCont].intensity = null;
				cont[numCont].num = numPnt;
				cont[numCont].setContourClass(cls);
				numCont++;
			}
			else {
				/*
				 * Delete the point from the label image; we can use maxx and maxy as
				 * the coordinates in the label image in this case.
				 */
				for (i = -1; i <= 1; i++) {
					for (j = -1; j <= 1; j++) {
						pos = LinesUtil.linCoor(LinesUtil.mirrorRow(maxx + i, height),
							LinesUtil.mirrorCol(maxy + j, width), width);
						if (label[pos] == numCont + 1) label[pos] = 0;
					}
				}
			}
		}

		/*
		 * Now try to extend the lines at their ends to find additional junctions.
		 */
		if (extendLines) {
			/* Sign by which the gradient has to be multiplied below. */
			if (mode == LinesUtil.MODE_LIGHT) s = 1;
			else s = -1;
			double MAX_LINE_EXTENSION = 2.5 * sigma;
			length = MAX_LINE_EXTENSION;
			maxLine = (int) Math.ceil(length * 3);
			line = new Offset[maxLine];
			for (int o = 0; o < line.length; o++) {
				line[o] = new Offset();
			}
			extx = new float[maxLine];
			exty = new float[maxLine];
			for (i = 0; i < numCont; i++) {
				tmpCont = cont[i];
				numPnt = tmpCont.num;
				if (numPnt == 1) continue;
				if (tmpCont.getContourClass() == ContourClass.CLOSED) continue;
				trow = tmpCont.row;
				tcol = tmpCont.col;
				tangle = tmpCont.angle;
				tresp = tmpCont.response;
				/* Check both ends of the line (it==-1: start, it==1: end). */
				for (it = -1; it <= 1; it += 2) {
					/*
					 * Determine the direction of the search line. This is done by using
					 * the normal to the line (angle). Since this normal may point to the
					 * left of the line (see below) we have to check for this case by
					 * comparing the normal to the direction of the line at its respective
					 * end point.
					 */
					if (it == -1) {
						/* Start point of the line. */
						if (tmpCont.getContourClass() == ContourClass.START_JUNCTION ||
							tmpCont.getContourClass() == ContourClass.BOTH_JUNCTIONS)
							continue;
						dx = trow[1] - trow[0];
						dy = tcol[1] - tcol[0];
						alpha = tangle[0];
						nx = Math.cos(alpha);
						ny = Math.sin(alpha);
						if (nx * dy - ny * dx < 0) {
							/* Turn the normal by +90 degrees. */
							mx = -ny;
							my = nx;
						}
						else {
							/* Turn the normal by -90 degrees. */
							mx = ny;
							my = -nx;
						}
						px = trow[0];
						py = tcol[0];
						response = tresp[0];
					}
					else {
						/* End point of the line. */
						if (tmpCont.getContourClass() == ContourClass.END_JUNCTION ||
							tmpCont.getContourClass() == ContourClass.BOTH_JUNCTIONS)
							continue;
						dx = trow[(numPnt - 1)] - trow[(numPnt - 2)];
						dy = tcol[(numPnt - 1)] - tcol[(numPnt - 2)];
						alpha = tangle[(numPnt - 1)];
						nx = Math.cos(alpha);
						ny = Math.sin(alpha);
						if (nx * dy - ny * dx < 0) {
							/* Turn the normal by -90 degrees. */
							mx = ny;
							my = -nx;
						}
						else {
							/* Turn the normal by +90 degrees. */
							mx = -ny;
							my = nx;
						}
						px = trow[(numPnt - 1)];
						py = tcol[(numPnt - 1)];
						response = tresp[(numPnt - 1)];
					}
					/*
					 * Determine the current pixel and calculate the pixels on the search
					 * line.
					 */
					x = (int) Math.floor(px + 0.5);
					y = (int) Math.floor(py + 0.5);
					dx = px - x;
					dy = py - y;
					w.bresenham(mx, my, dx, dy, length, line, numLine);
					/*
					 * Now determine whether we can go only uphill (bright lines) or
					 * downhill (dark lines) until we hit another line.
					 */
					numAdd = 0;
					addExt = false;
					for (k = 0; k < numLine.intValue(); k++) {
						nextx = x + line[k].x;
						nexty = y + line[k].y;
						MutableDouble hnextpx = new MutableDouble(nextpx);
						MutableDouble hnextpy = new MutableDouble(nextpy);
						closestPoint(px, py, mx, my, (double) nextx, (double) nexty,
							hnextpx, hnextpy, t);
						nextpx = hnextpx.getValue();
						nextpy = hnextpy.getValue();
						/*
						 * Ignore points before or less than half a pixel away from the true
						 * end point of the line.
						 */
						if (t.getValue() <= 0.5) continue;
						/*
						 * Stop if the gradient can't be interpolated any more or if the
						 * next point lies outside the image.
						 */
						if (nextpx < 0 || nextpy < 0 || nextpx >= height - 1 ||
							nextpy >= width - 1 || nextx < 0 || nexty < 0 ||
							nextx >= height || nexty >= width) break;
						MutableDouble hgx = new MutableDouble();
						MutableDouble hgy = new MutableDouble();
						interpolateGradient(gradx, grady, nextpx, nextpy, width, hgx, hgy);
						gx = hgx.getValue();
						gy = hgy.getValue();
						/*
						 * Stop if we can't go uphill anymore. This is determined by the dot
						 * product of the line direction and the gradient. If it is smaller
						 * than 0 we go downhill (reverse for dark lines).
						 */
						nextpos = LinesUtil.linCoor(nextx, nexty, width);
						if (s * (mx * gx + my * gy) < 0 && label[nextpos] == 0) break;
						/* Have we hit another line? */
						if (label[nextpos] > 0) {
							m = label[nextpos] - 1;
							/* Search for the junction point on the other line. */
							mindist = Double.MAX_VALUE;
							j = -1;
							for (l = 0; l < cont[m].num; l++) {
								dx = nextpx - cont[m].row[l];
								dy = nextpy - cont[m].col[l];
								dist = Math.sqrt(dx * dx + dy * dy);
								if (dist < mindist) {
									mindist = dist;
									j = l;
								}
							}
							/*
							 * This should not happen... But better safe than sorry...
							 */
							if (mindist > 3.0) {
								break;
							}
							extx[numAdd] = cont[m].row[j];
							exty[numAdd] = cont[m].col[j];
							endResp = cont[m].response[j];
							endAngle = cont[m].angle[j];
							beta = endAngle;
							if (beta >= Math.PI) beta -= Math.PI;
							diff1 = Math.abs(beta - alpha);
							if (diff1 >= Math.PI) diff1 = 2.0 * Math.PI - diff1;
							diff2 = Math.abs(beta + Math.PI - alpha);
							if (diff2 >= Math.PI) diff2 = 2.0 * Math.PI - diff2;
							if (diff1 < diff2) endAngle = beta;
							else endAngle = beta + Math.PI;
							numAdd++;
							addExt = true;
							break;
						}
						else {
							extx[numAdd] = (float) nextpx;
							exty[numAdd] = (float) nextpy;
							numAdd++;
						}
					}
					if (addExt) {
						/* Make room for the new points. */
						numPnt += numAdd;
						float[] newArr = new float[numPnt];
						for (int o = 0; o < trow.length; o++) {
							newArr[o] = trow[o];
						}
						trow = newArr;

						newArr = new float[numPnt];
						for (int o = 0; o < tcol.length; o++) {
							newArr[o] = tcol[o];
						}
						tcol = newArr;

						newArr = new float[numPnt];
						for (int o = 0; o < tangle.length; o++) {
							newArr[o] = tangle[o];
						}
						tangle = newArr;

						newArr = new float[numPnt];
						for (int o = 0; o < tresp.length; o++) {
							newArr[o] = tresp[o];
						}
						tresp = newArr;

						tmpCont.row = trow;
						tmpCont.col = tcol;
						tmpCont.angle = tangle;
						tmpCont.response = tresp;
						tmpCont.num = numPnt;
						if (it == -1) {
							/* Move points on the line up numAdd places. */
							for (k = numPnt - 1 - numAdd; k >= 0; k--) {
								trow[(k + numAdd)] = trow[k];
								tcol[(k + numAdd)] = tcol[k];
								tangle[(k + numAdd)] = tangle[k];
								tresp[(k + numAdd)] = tresp[k];
							}
							/* Insert points at the beginning of the line. */
							for (k = 0; k < numAdd; k++) {
								trow[k] = extx[(numAdd - 1 - k)];
								tcol[k] = exty[(numAdd - 1 - k)];
								tangle[k] = (float) alpha;
								tresp[k] = (float) response;
							}
							tangle[0] = (float) endAngle;
							tresp[0] = (float) endResp;
							/* Adapt indices of the previously found junctions. */
							for (k = 0; k < numJunc; k++) {
								if (junc[k].cont1 == i) junc[k].pos += numAdd;
							}
						}
						else {
							/* Insert points at the end of the line. */
							for (k = 0; k < numAdd; k++) {
								trow[(numPnt - numAdd + k)] = extx[k];
								tcol[(numPnt - numAdd + k)] = exty[k];
								tangle[(numPnt - numAdd + k)] = (float) alpha;
								tresp[(numPnt - numAdd + k)] = (float) response;
							}
							tangle[(numPnt - 1)] = (float) endAngle;
							tresp[(numPnt - 1)] = (float) endResp;
						}
						/* If necessary, make room for the new junction. */
						if (numJunc >= sizeJunc) {
							sizeJunc = (int) Math.floor((double) (sizeJunc *
								LinesUtil.REALLOC_FACTOR));
							Junction[] junch = new Junction[sizeJunc];
							for (int o = 0; o < junch.length; o++) {
								if (o < junc.length) junch[o] = junc[o];
								else junch[o] = new Junction();
							}
							junc = junch;

						}
						/*
						 * Add the junction point only if it is not one of the other line's
						 * endpoints.
						 */
						if (j > 0 && j < cont[m].num - 1) {
							if (it == -1) {
								if (tmpCont.getContourClass() == ContourClass.END_JUNCTION)
									tmpCont.setContourClass(ContourClass.BOTH_JUNCTIONS);
								else tmpCont.setContourClass(ContourClass.START_JUNCTION);
							}
							else {
								if (tmpCont.getContourClass() == ContourClass.START_JUNCTION)
									tmpCont.setContourClass(ContourClass.BOTH_JUNCTIONS);
								else tmpCont.setContourClass(ContourClass.END_JUNCTION);
							}
							junc[numJunc].cont1 = m;
							junc[numJunc].cont2 = i;
							junc[numJunc].pos = j;
							if (it == -1) {
								junc[numJunc].x = trow[0];
								junc[numJunc].y = tcol[0];
							}
							else {
								junc[numJunc].x = trow[(numPnt - 1)];
								junc[numJunc].y = tcol[(numPnt - 1)];
							}
							numJunc++;
						}
					}
				}

			}
		}

		/* Done with linking. Now split the lines at the junction points. */
		java.util.Arrays.sort(junc);
		for (i = 0; i < numJunc; i += k) {
			j = junc[i].cont1;
			tmpCont = cont[j];
			numPnt = tmpCont.num;
			/* Count how often line j needs to be split. */
			for (k = 0; junc[(i + k)].cont1 == j && i + k < numJunc; k++);

			if (k == 1 && tmpCont.row[0] == tmpCont.row[(numPnt - 1)] &&
				tmpCont.col[0] == tmpCont.col[(numPnt - 1)])
			{
				/*
				 * If only one junction point is found and the line is closed it only
				 * needs to be rearranged cyclically, but not split.
				 */
				begin = junc[i].pos;
				trow = tmpCont.row;
				tcol = tmpCont.col;
				tangle = tmpCont.angle;
				tresp = tmpCont.response;
				tmpCont.row = new float[numPnt];
				tmpCont.col = new float[numPnt];
				tmpCont.angle = new float[numPnt];
				tmpCont.response = new float[numPnt];
				for (l = 0; l < numPnt; l++) {
					pos = begin + l;
					/* Skip starting point so that it is not added twice. */
					if (pos >= numPnt) pos = begin + l - numPnt + 1;
					tmpCont.row[l] = trow[pos];
					tmpCont.col[l] = tcol[pos];
					tmpCont.angle[l] = tangle[pos];
					tmpCont.response[l] = tresp[pos];
				}
				/* Modify contour class. */
				tmpCont.setContourClass(ContourClass.BOTH_JUNCTIONS);

			}
			else {
				/* Otherwise the line has to be split. */
				for (l = 0; l <= k; l++) {
					if (l == 0) begin = 0;
					else begin = junc[(i + l - 1)].pos;
					if (l == k) end = tmpCont.num - 1;
					else end = junc[(i + l)].pos;
					numPnt = end - begin + 1;
					if (numPnt == 1 && k > 1) {
						/* Do not add one point segments. */
						continue;
					}
					if (numCont >= sizeCont) {
						sizeCont = (int) Math.floor((double) (sizeCont *
							LinesUtil.REALLOC_FACTOR));
						Line[] conth = new Line[sizeCont];
						for (int o = 0; o < cont.length; o++) {
							conth[o] = cont[o];
						}
						cont = conth;
					}
					cont[numCont] = new Line(contours);

					cont[numCont].row = new float[numPnt];
					cont[numCont].col = new float[numPnt];
					cont[numCont].angle = new float[numPnt];
					cont[numCont].response = new float[numPnt];

					System.arraycopy(tmpCont.row, begin, cont[numCont].row, 0, numPnt);

					System.arraycopy(tmpCont.col, begin, cont[numCont].col, 0, numPnt);

					System.arraycopy(tmpCont.angle, begin, cont[numCont].angle, 0,
						numPnt);

					System.arraycopy(tmpCont.response, begin, cont[numCont].response, 0,
						numPnt);

					cont[numCont].widthR = null;
					cont[numCont].widthL = null;
					cont[numCont].asymmetry = null;
					cont[numCont].intensity = null;
					cont[numCont].num = numPnt;
					/* Modify contour class. */
					if (l == 0) {
						if (tmpCont.getContourClass() == ContourClass.START_JUNCTION ||
							tmpCont.getContourClass() == ContourClass.BOTH_JUNCTIONS)
							cont[numCont].setContourClass(ContourClass.BOTH_JUNCTIONS);
						else cont[numCont].setContourClass(ContourClass.END_JUNCTION);
					}
					else if (l == k) {
						if (tmpCont.getContourClass() == ContourClass.END_JUNCTION ||
							tmpCont.getContourClass() == ContourClass.BOTH_JUNCTIONS)
							cont[numCont].setContourClass(ContourClass.BOTH_JUNCTIONS);
						else cont[numCont].setContourClass(ContourClass.START_JUNCTION);
					}
					else {
						cont[numCont].setContourClass(ContourClass.BOTH_JUNCTIONS);
					}
					numCont++;
				}
				cont[j] = cont[--numCont];
			}
		}

		/* Finally, check whether all angles point to the right of the line. */
		for (i = 0; i < numCont; i++) {
			tmpCont = cont[i];
			numPnt = tmpCont.num;
			if (numPnt > 1) {
				trow = tmpCont.row;
				tcol = tmpCont.col;
				tangle = tmpCont.angle;
				/*
				 * One point of the contour is enough to determine the orientation.
				 */

				k = (numPnt - 1) / 2;
				/*
				 * The next few lines are ok because lines have at least two points.
				 */
				dx = trow[(k + 1)] - trow[k];
				dy = tcol[(k + 1)] - tcol[k];
				nx = Math.cos(tangle[k]);
				ny = Math.sin(tangle[k]);
				/*
				 * If the angles point to the left of the line they have to be adapted.
				 * The orientation is determined by looking at the z-component of the
				 * cross-product of (dx,dy,0) and (nx,ny,0).
				 */
				if (nx * dy - ny * dx < 0) {
					for (j = 0; j < numPnt; j++) {
						tangle[j] += Math.PI;
						if (tangle[j] >= 2 * Math.PI) tangle[j] -= 2 * Math.PI;
					}
				}
			}
		}

		for (Line c : cont) {
			if (c != null && c.getContourClass() != null) {
				contours.add(c);

			}
		}

		for (Junction jun : junc) {
			if (jun != null && !(jun.cont1 == 0 && jun.cont2 == 0)) {
				junctions.add(jun);
			}
		}
		numResult.setValue(numCont);
	}

}
