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

import java.util.function.BiFunction;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.OpDependency;

/**
 * Vector Accelerator implements acceleration scheme described in Acceleration
 * of iterative image restoration algorithms David S.C. Biggs and Mark Andrews
 * Applied Optics, Vol. 36, Issue 8, pp. 1766-1775 (1997)
 *
 * @author bnorthan
 * @param <T>
 * @implNote op names='deconvolve.accelerate', priority='0.'
 */
public class VectorAccelerator<T extends RealType<T>> implements
	Inplaces.Arity1<AccelerationState<T>>
{

	@OpDependency(name = "copy.rai")
	private Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> copyOp;

	@OpDependency(name = "create.img")
	private BiFunction<Dimensions, T, Img<T>> create;

	/**
	 * TODO
	 *
	 * @param state
	 */
	@Override
	public void mutate(AccelerationState<T> state) {

		accelerate(state);
	}

	private void initialize(AccelerationState<T> state) {
		if (state.ykPrediction() == null) {
			long[] temp = new long[state.ykIterated().numDimensions()];
			T type = Util.getTypeFromInterval(state.ykIterated());
			state.ykIterated().dimensions(temp);

			FinalDimensions dims = new FinalDimensions(temp);

			state.ykPrediction(create.apply(dims, type));
			state.xkm1Previous(create.apply(dims, type));
			state.ykPrediction(create.apply(dims, type));
			state.gk(create.apply(dims, type));
			state.hkVector(create.apply(dims, type));
		}

	}

	private void accelerate(AccelerationState<T> state) {

		// use the iterated prediction and the previous value of the prediction
		// to calculate the acceleration factor
		if (state.ykPrediction() != null) {

			double accelerationFactor = computeAccelerationFactor(state);

			if ((accelerationFactor < 0)) {
				state.gkm1(null);
				accelerationFactor = 0.0;
			}

			if ((accelerationFactor > 1.0f)) {
				accelerationFactor = 1.0f;
			}

			state.accelerationFactor(accelerationFactor);
		}

		// current estimate for x is yk_iterated
		RandomAccessibleInterval<T> xk_estimate = state.ykIterated();

		// calculate the change vector between x and x previous
		if (state.accelerationFactor() > 0) {
			Subtract(xk_estimate, state.xkm1Previous(), state.hkVector());

			// make the next prediction
			state.ykPrediction(AddAndScale(xk_estimate, state.hkVector(),
				(float) state.accelerationFactor()));
		}
		else {

			// TODO: Revisit where initialization should be done
			initialize(state);

			copyOp.compute(xk_estimate, state.ykPrediction());
		}

		// make a copy of the estimate to use as previous next time
		copyOp.compute(xk_estimate, state.xkm1Previous());

		// HACK: TODO: look over how to transfer the memory
		copyOp.compute(state.ykPrediction(), state.ykIterated());
	}

	private double computeAccelerationFactor(AccelerationState<T> state) {
		// gk=StaticFunctions.Subtract(yk_iterated, yk_prediction);
		Subtract(state.ykIterated(), state.ykPrediction(), state.gk());

		double result = 0.0;

		if (state.gkm1() != null) {
			double numerator = DotProduct(state.gk(), state.gkm1());
			double denominator = DotProduct(state.gkm1(), state.gkm1());

			result = numerator / denominator;
		}

		state.gkm1(state.gk().copy());

		return result;
	}

	/*
	 * multiply inputOutput by input and place the result in input
	 */
	private double DotProduct(final Img<T> image1, final Img<T> image2) {
		final Cursor<T> cursorImage1 = image1.cursor();
		final Cursor<T> cursorImage2 = image2.cursor();

		double dotProduct = 0.0d;

		while (cursorImage1.hasNext()) {
			cursorImage1.fwd();
			cursorImage2.fwd();

			float val1 = cursorImage1.get().getRealFloat();
			float val2 = cursorImage2.get().getRealFloat();

			dotProduct += val1 * val2;
		}

		return dotProduct;
	}

	// TODO replace with op.
	private void Subtract(RandomAccessibleInterval<T> a,
		RandomAccessibleInterval<T> input, RandomAccessibleInterval<T> output)
	{

		final Cursor<T> cursorA = Views.iterable(a).cursor();
		final Cursor<T> cursorInput = Views.iterable(input).cursor();
		final Cursor<T> cursorOutput = Views.iterable(output).cursor();

		while (cursorA.hasNext()) {
			cursorA.fwd();
			cursorInput.fwd();
			cursorOutput.fwd();

			cursorOutput.get().set(cursorA.get());
			cursorOutput.get().sub(cursorInput.get());
		}
	}

	// TODO: replace with op
	private Img<T> AddAndScale(final RandomAccessibleInterval<T> img1,
		final Img<T> img2, final float a)
	{
		Img<T> out = create.apply(img1, Util.getTypeFromInterval(img1));

		final Cursor<T> cursor1 = Views.iterable(img1).cursor();
		final Cursor<T> cursor2 = img2.cursor();
		final Cursor<T> cursorOut = out.cursor();

		while (cursor1.hasNext()) {
			cursor1.fwd();
			cursor2.fwd();
			cursorOut.fwd();

			float val1 = cursor1.get().getRealFloat();
			float val2 = cursor2.get().getRealFloat();

			float val3 = Math.max(val1 + a * val2, 0.0001f);

			cursorOut.get().setReal(val3);
		}

		return out;
	}
}
