/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.matcher.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.conversionLoss.impl.IdentityLossReporter;
import org.scijava.ops.engine.conversionLoss.impl.PrimitiveLossReporters;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.engine.matcher.impl.LossReporterWrapper;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

/**
 * Basic Op conversion test
 *
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
public class ConversionTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register( //
			new CopyOpCollection<>(), //
			new CreateOpCollection(), //
			new IdentityCollection<>(), //
			new IdentityLossReporter<>(), //
			new LossReporterWrapper<>(), //
			new PrimitiveArrayConverters<>(), //
			new PrimitiveConverters<>(), //
			new PrimitiveLossReporters(), //
			new ConversionTest(), //
			new UtilityConverters() //
		);
	}

	@OpField(names = "test.math.powDouble", params = "base, exponent, result")
	public final BiFunction<Double, Double, Double> powOp = Math::pow;

	@OpField(names = "test.math.powDouble", params = "base, exponent, result")
	public final BiFunction<Long, Long, Double> powOpL = Math::pow;

	@OpField(names = "test.math.powDouble", params = "base, exponent, result")
	public final BiFunction<Integer[], Double, Double> powOpArray = (b, e) -> Math
		.pow(b[0], e);

	@Test
	public void testConversion() {
		Integer number = 2;
		Integer exponent = 2;
		Double result = ops.op("test.math.powDouble").arity2().input(number,
			exponent).outType(Double.class).apply();
		assertEquals(4.0, result, 0);
	}

	@Test
	public void testSomeConversion() {
		Integer number = 2;
		Double exponent = 2.;
		Double result = ops.op("test.math.powDouble").arity2().input(number,
			exponent).outType(Double.class).apply();
		assertEquals(4.0, result, 0);
	}

	@Test
	public void testArrayConversion() {
		byte[] number = { 2 };
		Double exponent = 3.;
		Double result = ops.op("test.math.powDouble").arity2().input(number,
			exponent).outType(Double.class).apply();
		assertEquals(8.0, result, 0);
	}

	@Test
	public void testConvertedOp() {
		BiFunction<Number, Number, Double> numFunc = ops.op("test.math.powDouble")
			.arity2().inType(Number.class, Number.class).outType(Double.class)
			.function();

		Double result = numFunc.apply(3., 4.);
		assertEquals(81., result, 0);
	}

}
