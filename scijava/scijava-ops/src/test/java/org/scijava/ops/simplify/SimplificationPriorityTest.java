
package org.scijava.ops.simplify;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.OpField;
import org.scijava.ops.OpHints;
import org.scijava.ops.BaseOpHints.Simplification;
import org.scijava.ops.conversionLoss.LossReporter;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class SimplificationPriorityTest extends AbstractTestEnvironment {

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

		Double output = ops.op("test.thing").input(thing1, thing2).outType(
			Double.class).apply();
		assertEquals(1., output);
	}

	@Test
	public void testMissingLossReporter() {
		FromThing thing1 = new FromThing();

		Double output = ops.op("test.thing").input(thing1).outType(
			Double.class).apply();
		assertEquals(2., output);
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
