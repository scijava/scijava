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

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import flimlib.FitFunc;
import flimlib.NoiseType;
import flimlib.RestrainType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.RealMask;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.Arrays;

/**
 * The collection of all fit parameters required to perform a single fit of an
 * image. Fields named {@code xxMap} are image representations of the particular
 * attribute. Other fields are not intended to be used by external programs and
 * should be ignored when this object is processed with in the fitting ops.
 *
 * @param <I> The type of the transient data.
 */
public class FitParams<I extends RealType<I>> {

	/** Fields with this value are uninitialized */
	@Expose
	public static final int UNINIT = -1;

	/** The time increment between two consecutive data points */
	@Expose
	public float xInc = UNINIT;

	/** The transient data to fit */
	public float[] trans;

	/** The index of lifetime axis */
	@Expose
	public int ltAxis = UNINIT;

	/** The image representation of the dataset */
	public RandomAccessibleInterval<I> transMap;

	/** The ROI mask (test() returns true on interested regions) */
	public RealMask roiMask;

	/** The start of the decay interval */
	@Expose
	public int fitStart = UNINIT;

	/** The end of the decay interval */
	@Expose
	public int fitEnd = UNINIT;

	/** The array of instrument response (optional) */
	@Expose
	public float[] instr;

	/** The assumed noise model of the fit (Poisson by default) @see NoiseType */
	@Expose
	public NoiseType noise = NoiseType.NOISE_POISSON_FIT;

	/**
	 * The standard deviation (sigma) of the data, used for calculating
	 * chi-squared if {@link #noise} is {@link NoiseType#NOISE_CONST} or
	 * {@link NoiseType#NOISE_GIVEN}.
	 */
	@Expose
	public float[] sig;

	/**
	 * The number of exponential components of the fit (1 by default). This
	 * parameter is only used by LMA and Global ops, ignored otherwise.
	 */
	@Expose
	public int nComp = 1;

	/** The estimated parameters of the fit (global setting) */
	@Expose
	public float[] param;

	/**
	 * The image representation of the estimated parameters the fit (per-pixel
	 * setting, overides {@link #param})
	 */
	public RandomAccessibleInterval<FloatType> paramMap;

	/** The indicators of which of the parameters can be changed */
	@Expose
	public boolean[] paramFree;

	/**
	 * The fit restraint ({@link RestrainType#ECF_RESTRAIN_DEFAULT} by default)
	 */
	@Expose
	public RestrainType restrain = RestrainType.ECF_RESTRAIN_DEFAULT;

	/**
	 * The fit restraints (min or max) for each parameter. A parameter at index
	 * {@code i} will be restrained during the fit in the range
	 * {@code (restraintMin[i], restraintMax[i])}. If any of the two bounds are
	 * not present (due to the array being {@code null} or {@code [i] == NaN}),
	 * then {@code -/+Inf} is used instead and that parameter will not be bounded
	 * from below/above. The bounds only take effect if {@link #restrain} is set
	 * to {@link RestrainType#ECF_RESTRAIN_USER}.
	 */
	@Expose
	public float[] restraintMin, restraintMax;

	/**
	 * The fitting model to use (Z + A_1e^(-t/tau_1) + A_2e^(-t/tau_2) + ... by
	 * default)
	 */
	@Expose
	public FitFunc fitFunc = FitFunc.GCI_MULTIEXP_TAU;

	/**
	 * Stopping condition 1: stop if reduced chi-squared is less than
	 * {@link #chisq_target} (1 by default)
	 */
	@Expose
	public float chisq_target = 1;

	/**
	 * Stopping condition 2: stop if change in chi-squared is less than
	 * {@link #chisq_target} (1E-4 by default)
	 */
	@Expose
	public float chisq_delta = 0.0001f;

	/** Confidence interval when calculating the error axes (95% by default) */
	@Expose
	public int chisq_percent = 95;

	/** Intensity threshold value (overrides {@link #iThreshPercent}) */
	@Expose
	public float iThresh = 0;

	/** Intensity threshold percentage */
	@Expose
	public float iThreshPercent = 0;

	/** Enable multithread fitting ({@code true} by default) */
	@Expose
	public boolean multithread = true;

	// FitResults Settings

	/**
	 * Whether to declare {@link FitResults#retCode} as
	 * {@link FitResults#RET_BAD_FIT_CHISQ_OUT_OF_RANGE} or
	 * {@link FitResults#RET_BAD_FIT_DIVERGED} if {@link FitResults#chisq} is
	 * larger than 1e5 or less than 0.
	 */
	@Expose
	public boolean dropBad = true;

	/**
	 * Whether to generate an image representation for the return codes
	 * ({@code false} by default)
	 */
	@Expose
	public boolean getReturnCodeMap = false;

	/**
	 * Whether to generate an image representation for fitted parameters
	 * ({@code true} by default)
	 */
	@Expose
	public boolean getParamMap = true;

	/**
	 * Whether to generate an image representation for fitted transients
	 * ({@code false} by default)
	 */
	@Expose
	public boolean getFittedMap = false;

	/**
	 * Whether to generate an image representation for residuals ({@code false} by
	 * default)
	 */
	@Expose
	public boolean getResidualsMap = false;

	/**
	 * Whether to generate an image representation for chi-squred ({@code false}
	 * by default)
	 */
	@Expose
	public boolean getChisqMap = false;

	/**
	 * Create a new instance of {@link FitParams} with shallow copy (maps are not
	 * duplicated).
	 *
	 * @return A clone of the current instance.
	 */
	public FitParams<I> copy() {
        var newParams = new FitParams<I>();
		newParams.xInc = xInc;
		newParams.trans = trans;
		newParams.ltAxis = ltAxis;
		newParams.transMap = transMap;
		newParams.roiMask = roiMask;
		newParams.fitStart = fitStart;
		newParams.fitEnd = fitEnd;
		newParams.instr = instr;
		newParams.noise = noise;
		newParams.sig = sig;
		newParams.nComp = nComp;
		newParams.param = param;
		newParams.paramMap = paramMap;
		newParams.paramFree = paramFree;
		newParams.restrain = restrain;
		newParams.restraintMin = restraintMin;
		newParams.restraintMax = restraintMax;
		newParams.fitFunc = fitFunc;
		newParams.chisq_target = chisq_target;
		newParams.chisq_delta = chisq_delta;
		newParams.chisq_percent = chisq_percent;
		newParams.iThresh = iThresh;
		newParams.iThreshPercent = iThreshPercent;
		newParams.multithread = multithread;
		newParams.dropBad = dropBad;
		newParams.getParamMap = getParamMap;
		newParams.getFittedMap = getFittedMap;
		newParams.getResidualsMap = getResidualsMap;
		newParams.getChisqMap = getChisqMap;
		newParams.getReturnCodeMap = getReturnCodeMap;
		return newParams;
	}

	/**
	 * Serialize this FitParams into a JSON string. {@link #trans},
	 * {@link #transMap}, {@link #roiMask}, and {@link #paramMap} are skipped.
	 *
	 * @return the JSON string
	 */
	public String toJSON() {
		JsonSerializer<FitFunc> fitFuncSerializer = (elem, type, ctx) -> {
			String name;
			if (elem.equals(FitFunc.GCI_MULTIEXP_LAMBDA)) name =
				"GCI_MULTIEXP_LAMBDA";
			else if (elem.equals(FitFunc.GCI_MULTIEXP_TAU)) name = "GCI_MULTIEXP_TAU";
			else if (elem.equals(FitFunc.GCI_STRETCHEDEXP)) name = "GCI_STRETCHEDEXP";
			else throw new IllegalArgumentException(
				"Cannot serialize custom fitFunc: " + fitFunc);

			return new JsonPrimitive(name);
		};
        var gson = new GsonBuilder().serializeSpecialFloatingPointValues()
			.excludeFieldsWithoutExposeAnnotation().registerTypeAdapter(FitFunc.class,
				fitFuncSerializer).setPrettyPrinting().create();
		return gson.toJson(this);
	}

	/**
	 * Creates a FitParams from serialized JSON string.
	 *
	 * @param <I> data type
	 * @param jsonString the JSON string produced by {@link #toJSON()}
	 * @return the serialized parameters
	 */
	public static <I extends RealType<I>> FitParams<I> fromJSON(
		String jsonString)
	{
		JsonDeserializer<FitFunc> fitfuncDeserializer = (elem, type, ctx) -> {
			switch (elem.getAsString()) {
				case "GCI_MULTIEXP_LAMBDA":
					return FitFunc.GCI_MULTIEXP_LAMBDA;
				case "GCI_MULTIEXP_TAU":
					return FitFunc.GCI_MULTIEXP_TAU;
				case "GCI_STRETCHEDEXP":
					return FitFunc.GCI_STRETCHEDEXP;
				default:
					throw new IllegalArgumentException("Unrecognized fitFunc: " + elem
						.getAsString());
			}
		};
        var gson = new GsonBuilder().registerTypeAdapter(FitFunc.class,
			fitfuncDeserializer).create();
		return gson.fromJson(jsonString, new TypeToken<FitParams<I>>() {}
			.getType());
	}

	@Override
	public String toString() {
        var str = String.format(
			"xInc: %f, interval: [%d, %d), intensity threshold: %f, instr: %s, noise: %s, sig: %s, param: %s, paramFree: %s, restrain: %s, fitFunc: %s, chisq_target: %f, chisq_delta: %f, chisq_percent: %d",
			xInc, fitStart, fitEnd, iThresh, Arrays.toString(instr), noise.name(),
			Arrays.toString(sig), Arrays.toString(param), Arrays.toString(paramFree),
			restrain.name(), fitFunc, chisq_target, chisq_delta, chisq_percent);
		return str;
	}
}
