/*-
 * #%L
 * Fluorescence lifetime analysis in ImageJ.
 * %%
 * Copyright (C) 2017 - 2022 Board of Regents of the University of Wisconsin-Madison.
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

package org.scijava.ops.flim.util;

import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.ops.flim.FitResults;
import org.scijava.ops.flim.FitParams;

public class RAHelper<I extends RealType<I>> {

	final RandomAccess<I> transRA;
	final RandomAccess<FloatType> intensityRA, initialParamRA, fittedParamRA,
			fittedRA, residualsRA, chisqRA;
	final RandomAccess<IntType> retcodeRA;
	final int lifetimeAxis, raOffset, bufDataStart, bufDataEnd;

	public RAHelper(FitParams<I> params, FitResults rslts) {
		this.lifetimeAxis = params.ltAxis;
		transRA = params.transMap.randomAccess();
		intensityRA = rslts.intensityMap.randomAccess();
		initialParamRA = params.paramMap != null && params.paramMap.dimension(
			lifetimeAxis) >= params.nComp ? params.paramMap.randomAccess() : null;
		fittedParamRA = params.getParamMap ? rslts.paramMap.randomAccess() : null;
		fittedRA = params.getFittedMap ? rslts.fittedMap.randomAccess() : null;
		residualsRA = params.getResidualsMap ? rslts.residualsMap.randomAccess()
			: null;
		chisqRA = params.getChisqMap ? rslts.chisqMap.randomAccess() : null;
		retcodeRA = params.getReturnCodeMap ? rslts.retCodeMap.randomAccess()
			: null;

		int prefixLen = Math.min(params.instr == null ? 0 : params.instr.length,
			params.fitStart);

		// where does copy start in transRA
		raOffset = params.fitStart - prefixLen;
		// the start and end data (excluding instr prefix/suffix) index in buffer
		bufDataStart = prefixLen;
		bufDataEnd = bufDataStart + params.fitEnd - params.fitStart;
	}

	/**
	 * Fill buffers with data from {@code params}. Starting coordinate specified
	 * by {@code xytPos}. Skip and return {@code false} if
	 * {@code params.intensityMap} is less than intensity threshold.
	 *
	 * @param transBuffer the transient buffer
	 * @param paramBuffer the parameter buffer
	 * @param params the fitting parameters
	 * @param xytPos the starting position of data
	 * @return {@code false} if the transient is skipped, {@code true} otherwise
	 */
	public boolean loadData(float[] transBuffer, float[] paramBuffer,
		FitParams<I> params, int[] xytPos)
	{
		// intensity thresholding
		intensityRA.setPosition(xytPos);
		intensityRA.setPosition(0, lifetimeAxis);
		if (intensityRA.get().getRealFloat() < params.iThresh) {
			return false;
		}

		// load transient
		transRA.setPosition(xytPos);
		// to take the trans before start when convolving
		transRA.setPosition(raOffset, lifetimeAxis);
		for (int t = 0; t < transBuffer.length; t++, transRA.fwd(lifetimeAxis)) {
			transBuffer[t] = transRA.get().getRealFloat();
		}

		// fill initial values from params.paramMap
		if (initialParamRA != null) {
			initialParamRA.setPosition(xytPos);
			for (int p = 0; p < paramBuffer.length; p++, initialParamRA.fwd(
				lifetimeAxis))
			{
				paramBuffer[p] = initialParamRA.get().getRealFloat();
			}
		}
		// try to fill initial values from map if per-pixel parameter not present
		else if (params.param != null) {
			for (int p = 0; p < paramBuffer.length; p++) {
				paramBuffer[p] = params.param[p];
			}
		}
		// or if both local and global settings are not present, set to +Inf "no
		// estimation"
		else {
			for (int p = 0; p < paramBuffer.length; p++) {
				paramBuffer[p] = Float.POSITIVE_INFINITY;
			}
		}
		return true;
	}

	/**
	 * Put results back into the proper position in {@code rslts}. An RA in
	 * {@code rslts} will be skipped if {@code params.getXxMap} is {@code true}.
	 *
	 * @param params the fitting parameters
	 * @param rslts the result to fill in
	 * @param xytPos the coordinate of the single-pixel result in maps.
	 */
	public void commitRslts(FitParams<I> params, FitResults rslts, int[] xytPos) {
		if (params.getReturnCodeMap) {
			retcodeRA.setPosition(xytPos);
			retcodeRA.get().set(rslts.retCode);
		}
		if (params.dropBad && rslts.retCode != FitResults.RET_OK) return;
		if (params.getChisqMap) {
			chisqRA.setPosition(xytPos);
			chisqRA.get().set(rslts.chisq);
		}
		// fill in maps on demand
		if (params.getParamMap) {
			fillRA(fittedParamRA, xytPos, rslts.param, 0, rslts.param.length);
		}
		if (params.getFittedMap) {
			fillRA(fittedRA, xytPos, rslts.fitted, bufDataStart, bufDataEnd);
		}
		if (params.getResidualsMap) {
			fillRA(residualsRA, xytPos, rslts.residuals, bufDataStart, bufDataEnd);
		}
	}

	private void fillRA(RandomAccess<FloatType> ra, int[] xytPos, float[] arr,
		int start, int end)
	{
		xytPos[lifetimeAxis] = 0;
		ra.setPosition(xytPos);
		for (int i = start; i < end; i++) {
			ra.get().set(arr[i]);
			ra.fwd(lifetimeAxis);
		}
	}
}
