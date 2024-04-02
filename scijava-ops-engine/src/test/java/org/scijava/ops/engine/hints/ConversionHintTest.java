/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.hints;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.matcher.convert.IdentityCollection;
import org.scijava.ops.engine.matcher.convert.PrimitiveArrayConverters;
import org.scijava.ops.engine.matcher.convert.UtilityConverters;
import org.scijava.ops.spi.OpHints;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.engine.BaseOpHints.Conversion;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.conversionLoss.impl.IdentityLossReporter;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionHintTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new ConversionHintTest());
		ops.register(new IdentityCollection<>());
		ops.register(new IdentityLossReporter<>());
		ops.register(new UtilityConverters());
		ops.register(new PrimitiveArrayConverters<>());
	}

	@OpField(names = "test.conversion.hints")
	public final Function<Double[], Double[]> op = (in) -> new Double[in.length];

	@Test
	public void testConversion() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Function<Integer[], Integer[]> adaptable = ops.op("test.conversion.hints")
			.arity1().inType(Integer[].class).outType(Integer[].class).function();
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Conversion.FORBIDDEN);
		ops.setDefaultHints(hints);
		Assertions.assertThrows(OpMatchingException.class, () -> ops //
			.op("test.conversion.hints") //
			.arity1() //
			.inType(Integer[].class) //
			.outType(Integer[].class) //
			.function(), //
			"Conversion is forbidden - this op call should not match!" //
		);
	}

	@Test
	public void testConversionPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		@SuppressWarnings("unused")
		Function<Integer[], Integer[]> adaptable = ops.op("test.conversion.hints",
			hints).arity1().inType(Integer[].class).outType(Integer[].class)
			.function();
		// make sure we cannot find the Op when adaptation is not allowed
		final Hints forbid = hints.plus(Conversion.FORBIDDEN);
		Assertions.assertThrows(OpMatchingException.class, () -> ops //
			.op("test.conversion.hints", forbid) //
			.arity1() //
			.inType(Integer[].class) //
			.outType(Integer[].class) //
			.function(), //
			"Conversion is forbidden - this op call should not match!");
	}

	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "test.conversion.nonconvertible")
	public final Function<Double[], Double[]> nonAdaptableOp = (
		in) -> new Double[in.length];

	@Test
	public void testNonconvertibleOp() {
		// make sure we can find the Op when conversion is allowed
		Hints hints = new Hints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.conversion.nonconvertible").arity1().inType(Double[].class).outType(
				Double[].class).function();
		// make sure that we cannot match the Op via adaptation even when
		// conversion
		// is allowed (since it declares itself to be nonconvertible)
		Assertions.assertThrows(OpMatchingException.class, () -> ops //
			.op("test.conversion.nonconvertible") //
			.arity1() //
			.inType(Integer[].class) //
			.outType(Integer[].class) //
			.function(), //
			"The only relevant Op is not convertible - this op call should not match!" //
		);
	}

	@Test
	public void testNonconvertibleOpPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.conversion.nonconvertible", hints).arity1().inType(Double[].class)
			.outType(Double[].class).function();
		// make sure that we cannot match the Op via adaptation even when
		// conversion
		// is allowed (since it declares itself to be nonconvertible)
		assertThrows(OpMatchingException.class, () -> ops //
			.op("test.conversion.nonconvertible", hints) //
			.arity1() //
			.inType(Integer[].class) //
			.outType(Integer[].class) //
			.function(), //
			"The only relevant Op is not convertible - this op call should not match!" //
		);

	}

}
