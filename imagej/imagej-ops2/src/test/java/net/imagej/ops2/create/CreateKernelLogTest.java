/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.create;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import net.imagej.ops2.AbstractOpTest;
import org.scijava.types.Nil;
import org.scijava.function.Functions;
import org.scijava.ops.function.FunctionUtils;

/**
 * Tests {@code CreateKernelLogDoubleType} and
 * {@code CreateKernelLogSymmetricDoubleType}.
 * 
 * @author Brian Northan
 * @author Curtis Rueden
 */
public class CreateKernelLogTest extends AbstractOpTest {

	@Test
	public void testKernelLog() {
		final double sigma = 5.0;
		final double[] sigmas = { sigma, sigma };

		BiFunction<Double, Integer, RandomAccessibleInterval<DoubleType>> func1 = FunctionUtils.match(ops.env(),
				"create.kernelLog", new Nil<Double>() {
				}, new Nil<Integer>() {
				}, new Nil<RandomAccessibleInterval<DoubleType>>() {
				});

		final RandomAccessibleInterval<DoubleType> logKernel = //
				func1.apply(sigma, sigmas.length);

		Function<double[], RandomAccessibleInterval<DoubleType>> func2 = FunctionUtils.match(ops.env(), "create.kernelLog",
				new Nil<double[]>() {
				}, new Nil<RandomAccessibleInterval<DoubleType>>() {
				});

		final RandomAccessibleInterval<DoubleType> logKernel2 = //
				func2.apply(sigmas);

		assertEquals(logKernel.dimension(1), 27);
		assertEquals(logKernel2.dimension(1), 27);
	}

}
