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

package org.scijava.ops.image.threshold;

import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Ops which compute and apply a global threshold to an image.
 *
 * @author Christian Dietz (University of Konstanz)
 * @author Curtis Rueden
 * @author Brian Northan
 */
public final class ApplyThresholdMethod {

	private ApplyThresholdMethod() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * @implNote op names='threshold.huang'
	 */
	public static class Huang < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.huang")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.ij1'
	 */
	public static class IJ1 < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.ij1")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.intermodes'
	 */
	public static class Intermodes < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.intermodes")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.isoData'
	 */
	public static class IsoData < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.isoData")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.li'
	 */
	public static class Li < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.li")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.maxEntropy'
	 */
	public static class MaxEntropy < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.maxEntropy")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.maxLikelihood'
	 */
	public static class MaxLikelihood < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.maxLikelihood")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.mean'
	 */
	public static class Mean < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.mean")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.minError'
	 */
	public static class MinError < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.minError")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.minimum'
	 */
	public static class Minimum < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.minimum")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.moments'
	 */
	public static class Moments < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.moments")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.otsu'
	 */
	public static class Otsu < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.otsu")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.percentile'
	 */
	public static class Percentile < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.percentile")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.renyiEntropy'
	 */
	public static class RenyiEntropy < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.renyiEntropy")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.rosin'
	 */
	public static class Rosin < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.rosin")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.shanbhag'
	 */
	public static class Shanbhag < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.shanbhag")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.triangle'
	 */
	public static class Triangle < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.triangle")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

	/**
	 * @implNote op names='threshold.yen'
	 */
	public static class Yen < //
		T extends RealType<T>, //
		I extends Iterable<T>, //
		J extends Iterable<BitType> //
	> extends AbstractApplyThresholdImg<T, I, J> { //

		@OpDependency(name = "threshold.yen")
		private Computers.Arity1<Histogram1d<T>, T> computeThresholdOp;

		@Override
		protected Computers.Arity1<Histogram1d<T>, T> getComputeThresholdOp() {
			return computeThresholdOp;
		}
	}

}
