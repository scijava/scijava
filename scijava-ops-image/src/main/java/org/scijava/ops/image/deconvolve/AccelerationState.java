/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.deconvolve;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Helper class for the Vector Accelerator Op. The Acceleration State stores the
 * Vector Accelerator's data while iterative deconvolution is applied.
 *
 * @author Curtis Rueden
 * @author Edward Evans
 * @param <T>
 */

public class AccelerationState<T extends RealType<T>> {

	private final RandomAccessibleInterval<T> ykIterated;
	private Img<T> xkm1Previous = null;
	private Img<T> ykPrediction = null;
	private Img<T> hkVector = null;
	private Img<T> gk;
	private Img<T> gkm1;
	private double accelerationFactor = 0.0f;

	public AccelerationState(RandomAccessibleInterval<T> ykIterated) {
		this.ykIterated = ykIterated;
	}

	public RandomAccessibleInterval<T> ykIterated() {
		return ykIterated;
	}

	public Img<T> xkm1Previous() {
		return xkm1Previous;
	}

	public void xkm1Previous(Img<T> xkm1Previous) {
		this.xkm1Previous = xkm1Previous;
	}

	public Img<T> ykPrediction() {
		return ykPrediction;
	}

	public void ykPrediction(Img<T> ykPrediction) {
		this.ykPrediction = ykPrediction;
	}

	public Img<T> hkVector() {
		return hkVector;
	}

	public void hkVector(Img<T> hkVector) {
		this.hkVector = hkVector;
	}

	public Img<T> gk() {
		return gk;
	}

	public void gk(Img<T> gk) {
		this.gk = gk;
	}

	public Img<T> gkm1() {
		return gkm1;
	}

	public void gkm1(Img<T> gkm1) {
		this.gkm1 = gkm1;
	}

	public double accelerationFactor() {
		return accelerationFactor;
	}

	public void accelerationFactor(double accelerationFactor) {
		this.accelerationFactor = accelerationFactor;
	}
}
