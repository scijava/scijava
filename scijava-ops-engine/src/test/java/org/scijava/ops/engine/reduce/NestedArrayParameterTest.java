/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

package org.scijava.ops.engine.reduce;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.conversionLoss.impl.IdentityLossReporter;
import org.scijava.ops.engine.conversionLoss.impl.PrimitiveLossReporters;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.matcher.convert.IdentityCollection;
import org.scijava.ops.engine.matcher.convert.PrimitiveConverters;
import org.scijava.ops.engine.matcher.convert.UtilityConverters;
import org.scijava.ops.engine.matcher.impl.LossReporterWrapper;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;

/**
 * Tests ops with a multi-dimensional array parameter, whose reduced and
 * converted wrappers need a generated class name derived from the parameter
 * types ({@code double[][]} must give a valid identifier such as
 * {@code double_Arr_Arr}).
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class NestedArrayParameterTest extends AbstractTestEnvironment
	implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register( //
			new CopyOpCollection<>(), //
			new IdentityCollection<>(), //
			new IdentityLossReporter<>(), //
			new LossReporterWrapper<>(), //
			new PrimitiveConverters<>(), //
			new PrimitiveLossReporters(), //
			new UtilityConverters(), //
			new NestedArrayParameterTest() //
		);
	}

	@OpMethod(names = "test.nestedArraySum", type = BiFunction.class)
	public static Double nestedArraySum(final double[][] values,
		@Nullable final Double scale)
	{
		double sum = 0;
		for (final double[] row : values)
			for (final double v : row)
				sum += v;
		return sum * (scale == null ? 1 : scale);
	}

	@OpMethod(names = "test.nestedArrayCount", type = BiFunction.class)
	public static Double nestedArrayCount(final double[][][] values,
		@Nullable final Double scale)
	{
		long count = 0;
		for (final double[][] plane : values)
			for (final double[] row : plane)
				count += row.length;
		return count * (scale == null ? 1 : scale);
	}

	private static final double[][] VALUES = { { 1, 2 }, { 3, 4 } };

	@Test
	public void testAllArguments() {
		final Double sum = ops.op("test.nestedArraySum").input(VALUES, 2.0)
			.outType(Double.class).apply();
		assertEquals(20, sum, 0);
	}

	/** Omitting the optional parameter goes through a reduced wrapper. */
	@Test
	public void testOptionalOmitted() {
		final Double sum = ops.op("test.nestedArraySum").input(VALUES).outType(
			Double.class).apply();
		assertEquals(10, sum, 0);
		final Double count = ops.op("test.nestedArrayCount").input(
			new double[2][3][4]).outType(Double.class).apply();
		assertEquals(24, count, 0);
	}

	/** An Integer scale goes through a converted wrapper. */
	@Test
	public void testConverted() {
		final Double sum = ops.op("test.nestedArraySum").input(VALUES, 3)
			.outType(Double.class).apply();
		assertEquals(30, sum, 0);
		final Integer rounded = ops.op("test.nestedArraySum").input(VALUES)
			.outType(Integer.class).apply();
		assertEquals(10, rounded);
	}
}
