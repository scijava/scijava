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

package org.scijava.ops.flim.impl;

import flimlib.FLIMLib;
import flimlib.RestrainType;
import net.imglib2.type.numeric.RealType;
import org.scijava.ops.flim.FitParams;
import org.scijava.ops.flim.FitResults;
import org.scijava.ops.flim.FitWorker;

import java.util.Arrays;

/**
 * AbstractFitWorker
 */
public abstract class AbstractFitWorker<I extends RealType<I>> implements
	FitWorker<I>
{

	/** The fit parameters for this worker */
	protected final FitParams<I> params;

	/** The fit results for this worker */
	protected final FitResults results;

	/** Should be self-explanatory */
	protected final int nData, nParam;

	/**
	 * The adjusted {@link FitParams#fitStart} and {@link FitParams#fitEnd} taking
	 * into account leading instr prefix (see below)
	 */
	protected int adjFitStart, adjFitEnd;

	/**
	 * The number of data copied (including instr prefix/suffix and the part to
	 * fit, see below)
	 */
	protected int nDataTotal;

	/** The raw chisq target (params.chisq is reduced by DOF) */
	protected float rawChisq_target;

	public AbstractFitWorker(FitParams<I> params, FitResults results) {
		this.params = params;
		this.results = results;

		nData = nDataOut();
		nParam = nParamOut();

		// assume params are free if not specified
		int fillStart;
		if (params.paramFree == null) {
			params.paramFree = new boolean[nParam];
			fillStart = 0;
		}
		else if (params.paramFree.length < nParam) {
			fillStart = params.paramFree.length;
			params.paramFree = Arrays.copyOf(params.paramFree, nParam);
		}
		else {
			fillStart = params.paramFree.length;
		}
		for (int i = fillStart; i < params.paramFree.length; i++) {
			params.paramFree[i] = true;
		}

		populate();
	}

	/**
	 * The settings passed into the fit worker is mutable. This method refreshes
	 * the fit worker by updating cached information and probably re-allocating
	 * buffers.
	 */
	public void populate() {
		// we want to copy a little bit more around the interval to correctly conv
		// with
		// instr
		int instrLen = params.instr == null ? 0 : params.instr.length;
		int prefixLen = Math.min(instrLen, params.fitStart);
		int suffixLen = Math.min(instrLen, (int) params.transMap.dimension(
			params.ltAxis) - params.fitEnd);
		nDataTotal = prefixLen + nData + suffixLen;

		// adjust fitStart and fitEnd by the length of instr prefix
		adjFitStart = prefixLen;
		adjFitEnd = adjFitStart + nData;

		// the target is compared with raw chisq, so multiply by dof first
		rawChisq_target = params.chisq_target * (nData - nParam);

		results.ltAxis = params.ltAxis;

		if (params.restrain.equals(RestrainType.ECF_RESTRAIN_USER) &&
			(params.restraintMin != null || params.restraintMax != null))
		{
			boolean[] restrain = new boolean[this.nParam];
			float[] rMinOrig = params.restraintMin;
			float[] rMaxOrig = params.restraintMax;
			float[] rMin = new float[this.nParam];
			float[] rMax = new float[this.nParam];

			for (int i = 0; i < restrain.length; i++) {
				// only restrain the parameter if at least one of the restraints are
				// valid (finite
				// or inf)
				boolean restrainCurrent = false;
				if (rMinOrig != null && i < rMinOrig.length && !Float.isNaN(
					rMinOrig[i]))
				{
					rMin[i] = rMinOrig[i];
					restrainCurrent = true;
				}
				else rMin[i] = Float.NEGATIVE_INFINITY;

				if (rMaxOrig != null && i < rMaxOrig.length && !Float.isNaN(
					rMaxOrig[i]))
				{
					rMax[i] = rMaxOrig[i];
					restrainCurrent = true;
				}
				else rMax[i] = Float.POSITIVE_INFINITY;

				restrain[i] = restrainCurrent;
			}

			// restrain limits are not thread-local, onThreadInit() not needed
			FLIMLib.GCI_set_restrain_limits(restrain, rMin, rMax);
		}
	}

	@Override
	public int nParamOut() {
		// Z, A_i, tau_i
		return params.nComp * 2 + 1;
	}

	@Override
	public int nDataOut() {
		return params.fitEnd - params.fitStart;
	}
}
