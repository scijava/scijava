/*-
 * #%L
 * Fluorescence lifetime analysis in SciJava Ops.
 * %%
 * Copyright (C) 2024 - 2025 SciJava developers.
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

package org.scijava.ops.flim;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.Masks;
import net.imglib2.roi.RealMask;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ParamEstimator
 *
 * @param <I> The type of transient data
 */
public class ParamEstimator<I extends RealType<I>> {

	private static final float SAMPLE_RATE = 0.05f;
	/** (fitEnd - fitStart) / (nData - fitStart) */
	private static final float END_PERSENTAGE = 0.9f;

	private final FitParams<I> params;

	private final Img<FloatType> iMap;

	private final List<int[]> pos;
	private final int lifetimeAxis;

	private final int nData, nTrans;

	private final float[] sumAcrossTrans;
	private final float[] iSmpls;

	public ParamEstimator(FitParams<I> params, List<int[]> pos) {
		this.params = params;
		this.pos = pos;
		this.lifetimeAxis = params.ltAxis;
		nData = (int) params.transMap.dimension(lifetimeAxis);
		nTrans = pos.size();

		// if only percentage is set, calculate the value
		iSmpls = params.iThreshPercent > 0 && params.iThresh <= 0
			? new float[(int) (Math.max(nTrans * SAMPLE_RATE, 1))] : null;

		// create intensity image
		sumAcrossTrans = new float[nData];

		iMap = calcIMap();
	}

	public ParamEstimator(FitParams<I> params) {
		this(params, getRoiPositions(params.transMap, params.roiMask == null ? Masks
			.allRealMask(0) : params.roiMask, params.ltAxis));
	}

	public void estimateStartEnd() {
		// don't touch if not required
		if (params.fitStart < 0) {
            var max_idx = 0;
			for (var t = 0; t < sumAcrossTrans.length; t++) {
				max_idx = sumAcrossTrans[t] > sumAcrossTrans[max_idx] ? t : max_idx;
			}
			params.fitStart = max_idx;
		}
		if (params.fitEnd < 0 || params.fitEnd <= params.fitStart) {
			params.fitEnd = (int) (params.fitStart + (nData - params.fitStart) *
				END_PERSENTAGE);
		}
	}

	public void estimateIThreshold() {
		if (iSmpls != null) {
			Arrays.sort(iSmpls);
			params.iThreshPercent = Math.min(params.iThreshPercent, 100);
			params.iThresh = iSmpls[(int) (params.iThreshPercent / 100.0 *
				(iSmpls.length - 1))];
		}
	}

	public Img<FloatType> getIntensityMap() {
		return iMap;
	}

	private Img<FloatType> calcIMap() {
		// the intensity image has the same dim as say chisqMap
        var dimFit = new long[params.transMap.numDimensions()];
		params.transMap.dimensions(dimFit);
		dimFit[lifetimeAxis] = 1;
		Img<FloatType> iMap = ArrayImgs.floats(dimFit);

		// calculate the intensity of each interested trans
        var transRA = params.transMap.randomAccess();
        var iMapRA = iMap.randomAccess();
        var iSmplCnt = 0;
		for (var i = 0; i < pos.size(); i++) {
            var xytPos = pos.get(i);
			transRA.setPosition(xytPos);
			float intensity = 0;
			for (var t = 0; t < nData; t++, transRA.fwd(lifetimeAxis)) {
                var count = transRA.get().getRealFloat();
				intensity += count;
				sumAcrossTrans[t] += count;
			}
			// sample intensity every other nTrans * SAMPLE_RATE
			if (iSmpls != null && iSmplCnt + 1 <= i * SAMPLE_RATE) {
				iSmpls[iSmplCnt++] = intensity;
			}
			iMapRA.setPosition(xytPos);
			iMapRA.get().set(intensity);
		}
		return iMap;
	}

	private static <I> List<int[]> getRoiPositions(
		RandomAccessibleInterval<I> trans, RealMask mask, int lifetimeAxis)
	{
		final List<int[]> interested = new ArrayList<>();
		final var xyPlane = Views.hyperSlice(trans, lifetimeAxis, 0);
		final var xyCursor = xyPlane.localizingCursor();
		// work to do
		while (xyCursor.hasNext()) {
			xyCursor.fwd();
			if (mask.test(xyCursor)) {
                var pos = new int[3];
				xyCursor.localize(pos);
				// swap in lifetime axis
				for (var i = 2; i > lifetimeAxis; i--) {
                    var tmp = pos[i];
					pos[i] = pos[i - 1];
					pos[i - 1] = tmp;
				}
				pos[lifetimeAxis] = 0;
				interested.add(pos);
			}
		}
		return interested;
	}
}
