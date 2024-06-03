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
//
//package org.scijava.ops.image.image.invert;
//
//import org.scijava.ops.image.Ops;
//import org.scijava.ops.image.special.computer.AbstractUnaryComputerOp;
//import org.scijava.ops.image.special.computer.Computers;
//import net.imglib2.IterableInterval;
//import net.imglib2.type.numeric.RealType;
//import net.imglib2.type.numeric.integer.UnsignedVariableBitLengthType;
//
//import org.scijava.ops.spi.OpDependency;
//import org.scijava.ops.spi.OpDependency;
//import org.scijava.ops.core.Op;
//import org.scijava.functions.Computers;
//import org.scijava.functions.Computers;
//import org.scijava.functions.Computers;
//
///**
// * @author Martin Horn (University of Konstanz)
// * @author Gabe Selzer
// * @implNote op names='image.invert'
// */
//public class InvertII<T extends RealType<T>> implements
//	Computers.Arity3<IterableInterval<T>, T, T, IterableInterval<T>>
//{
//
//	@OpDependency(name = "map")
//	private Computers.Arity2<IterableInterval<T>, Computers.Arity1<T, T>, IterableInterval<T>> mapper;
//
//	@Override
//	public void compute(final IterableInterval<T> input, final T min, final T max,
//		final IterableInterval<T> output)
//	{
//			// unfortunately we have to do all of this math in double space since in some cases min+max will go outside of the type range
//			final double minValue = min == null ? input.firstElement().getMinValue() : //
//				min.getRealDouble();
//			final double maxValue = max == null ? input.firstElement().getMaxValue() : //
//				max.getRealDouble();
//			final double minMax = maxValue + minValue;
//			final Computers.Arity1<T, T> inverter = (in, out) -> {
//				if ((minMax - in.getRealDouble()) <= out.getMinValue()) {
//					out.setReal(out.getMinValue());
//				}
//				else if ((minMax - in.getRealDouble()) >= out.getMaxValue()) {
//					out.setReal(out.getMaxValue());
//				}
//				else out.setReal(minMax - in.getRealDouble());
//			};
//		mapper.compute(input, inverter, output);
//	}
////
////	public static <T extends RealType<T>> T minValue(T type) {
////		if (type instanceof UnsignedVariableBitLengthType) {
////			return (T) new UnsignedVariableBitLengthType(0, 1);
////		}
////		else {
////			T min = type.createVariable();
////			min.setReal(min.getMinValue());
////			return min;
////		}
////	}
//
//}
