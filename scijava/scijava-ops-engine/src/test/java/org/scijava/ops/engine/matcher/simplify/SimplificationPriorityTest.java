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

package org.scijava.ops.engine.matcher.simplify;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.spi.OpHints;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.ops.engine.matcher.impl.LossReporterWrapper;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class SimplificationPriorityTest extends AbstractTestEnvironment
		implements OpCollection {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new SimplificationPriorityTest());
		ops.register(new IdentityLossReporter());
		ops.register(new IdentityCollection());
		ops.register(new LossReporterWrapper());
	}

	/** The Thing we will be converting from */
	class FromThing {}

	/** The intermediary Thing */
	class BasicThing {}

	/** An lossless Thing */
	class LosslessThing {}

	/** A somewhat lossy Thing */
	class SomewhatLossyThing {}

	/** A very lossy Thing */
	class VeryLossyThing {}

	/** An inconceivably lossy Thing */
	class InconceivablyLossyThing {}

	@Test
	public void testSimplificationPriority() {
		FromThing thing1 = new FromThing();
		FromThing thing2 = new FromThing();

		Double output = ops.op("test.thing").arity2().input(thing1, thing2).outType(
			Double.class).apply();
		Assertions.assertEquals(1., output, 0.);
	}

	@Test
	public void testMissingLossReporter() {
		FromThing thing1 = new FromThing();

		Double output = ops.op("test.thing").arity1().input(thing1).outType(
			Double.class).apply();
		Assertions.assertEquals(2., output, 0.);
	}

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<FromThing, BasicThing> fromToBasic =
		from -> new BasicThing();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<BasicThing, LosslessThing> basicToLossless =
		basic -> new LosslessThing();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<BasicThing, SomewhatLossyThing> basicToSomewhat =
		basic -> new SomewhatLossyThing();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<BasicThing, VeryLossyThing> basicToVery =
		basic -> new VeryLossyThing();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<BasicThing, InconceivablyLossyThing> basicToInconceivable =
		basic -> new InconceivablyLossyThing();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<FromThing, LosslessThing> fromToLossless = (nil1,
		nil2) -> 1.;

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<FromThing, SomewhatLossyThing> fromToSomewhat = (
		nil1, nil2) -> 1e3;

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "lossReporter")
	public final LossReporter<FromThing, VeryLossyThing> fromToVery = (nil1,
		nil2) -> 1e6;

	@OpField(names = "test.thing")
	public final BiFunction<LosslessThing, VeryLossyThing, Double> thingBiFunc1 = (
		in1, in2) -> 0.;

	@OpField(names = "test.thing")
	public final BiFunction<SomewhatLossyThing, SomewhatLossyThing, Double> thingBiFunc2 =
		(in1, in2) -> 1.;

	@OpField(names = "test.thing")
	public final Function<SomewhatLossyThing, Double> thingFunc1 =
		(in1) -> 2.;

	@OpField(names = "test.thing")
	public final Function<InconceivablyLossyThing, Double> thingFunc2 =
		(in1) -> 3.;
}
