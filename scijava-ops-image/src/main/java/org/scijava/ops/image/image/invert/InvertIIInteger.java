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
//import java.math.BigInteger;
//
//import org.scijava.ops.image.util.UnboundedIntegerType;
//import net.imglib2.IterableInterval;
//import net.imglib2.type.numeric.IntegerType;
//import net.imglib2.type.numeric.RealType;
//import net.imglib2.type.numeric.integer.Unsigned128BitType;
//import net.imglib2.type.numeric.integer.UnsignedLongType;
//
//import org.scijava.ops.spi.OpDependency;
//import org.scijava.ops.spi.OpDependency;
//import org.scijava.ops.core.Op;
//import org.scijava.functions.Computers;
//import org.scijava.functions.Computers;
//import org.scijava.functions.Computers;
//
///**
// * @author Gabe Selzer
// * @implNote op names='image.invert', priority='100.'
// */
//public class InvertIIInteger<T extends IntegerType<T>> implements
//	Computers.Arity3<IterableInterval<T>, T, T, IterableInterval<T>>
//{
//
//	@OpDependency(name = "map")
//	private Computers.Arity2<IterableInterval<T>, Computers.Arity1<T, T>, IterableInterval<T>> mapper;
//
//
//	@Override
//	public void compute(final IterableInterval<T> input, final T min, final T max,
//		final IterableInterval<T> output)
//	{
//
//		final BigInteger minValue = min == null ? minValue(input.firstElement()).getBigInteger() : min.getBigInteger();
//		final BigInteger maxValue = max == null ? maxValue(input.firstElement()).getBigInteger() : max.getBigInteger();
//		final BigInteger minMax = minValue.add(maxValue);
//
//		final Computers.Arity1<T, T> inverter = (in, out) -> {
//			BigInteger inverted = minMax.subtract(in.getBigInteger());
//
//			if( inverted.compareTo(minValue(out).getBigInteger()) <= 0) out.set(minValue(out));
//			else if(inverted.compareTo(maxValue(out).getBigInteger()) >= 0) out.set(maxValue(out));
//			else out.setBigInteger(inverted);
//		};
//		mapper.compute(input, inverter, output);
//
//	}
//
//	public static <T extends RealType<T>> T minValue(T type) {
//		// TODO: Consider making minValue an Op.
//		final T min = type.createVariable();
//		if (type instanceof UnboundedIntegerType) min.setReal(0);
//		else min.setReal(min.getMinValue());
//		return min;
//
//	}
//
//	public static <T extends RealType<T>> T maxValue(T type) {
//		// TODO: Consider making maxValue an Op.
//		final T max = type.createVariable();
//		if (max instanceof Unsigned128BitType) {
//			final Unsigned128BitType t = (Unsigned128BitType) max;
//			t.set(t.getMaxBigIntegerValue());
//		}
//		else if (max instanceof UnsignedLongType) {
//			final UnsignedLongType t = (UnsignedLongType) max;
//			t.set(t.getMaxBigIntegerValue());
//		}
//		else if (max instanceof UnboundedIntegerType) {
//			max.setReal(0);
//		}
//		else {
//			max.setReal(type.getMaxValue());
//		}
//		return max;
//	}
//
//	@Override
//	public boolean conforms() {
//		final Object inType = in().firstElement();
//
//		// HACK: Help the matcher overcome generics limitations.
//		if (!(inType instanceof IntegerType)) return false;
//
//		// HACK: Reject types that are small.
//		// Because the InvertII is faster.
//		// TODO: Think of a better solution.
//		final T copy =  in().firstElement().createVariable();
//		copy.setInteger(Long.MAX_VALUE);
//		return copy.getIntegerLong() == Long.MAX_VALUE;
//	}
//
//}
