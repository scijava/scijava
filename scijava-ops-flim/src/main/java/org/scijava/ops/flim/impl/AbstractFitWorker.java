/*-
 * #%L
 * Fluorescence lifetime analysis in SciJava Ops.
 * %%
 * Copyright (C) 2024 SciJava developers.
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
