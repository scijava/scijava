package org.scijava.ops.engine.hints;

import java.util.function.Function;

import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpHints;
import org.scijava.ops.api.features.BaseOpHints.Adaptation;
import org.scijava.ops.api.features.OpMatchingException;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.adapt.functional.FunctionsToComputers;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class AdaptationHintTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeClass
	public static void AddNeededOps() {
		discoverer.register("opcollection", new AdaptationHintTest());
		discoverer.register("op", new FunctionsToComputers.Function1ToComputer1());
		discoverer.register("opcollection", new CopyOpCollection());
	}

	@OpField(names = "test.adaptation.hints")
	public final Function<Double[], Double[]> op = (in) -> new Double[in.length];

	@Test
	public void testAdaptation() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Computers.Arity1<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.hints").inType(Double[].class).outType(Double[].class)
			.computer();
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Adaptation.FORBIDDEN);
		ops.setDefaultHints(hints);
		try {
			ops.op("test.adaptation.hints").inType(Double[].class).outType(
				Double[].class).computer();
			throw new IllegalStateException("This op call should not match!");
		} catch( OpMatchingException e) {
		}
	}

	@Test
	public void testAdaptationPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		@SuppressWarnings("unused")
		Computers.Arity1<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.hints").inType(Double[].class).outType(Double[].class)
			.computer(hints);
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Adaptation.FORBIDDEN);
		try {
			ops.op("test.adaptation.hints").inType(Double[].class).outType(
				Double[].class).computer(hints);
			throw new IllegalStateException("This op call should not match!");
		} catch( OpMatchingException e) {
		}
	}

	@OpHints(hints = {Adaptation.FORBIDDEN})
	@OpField(names = "test.adaptation.unadaptable")
	public final Function<Double[], Double[]> nonAdaptableOp = (in) -> new Double[in.length];

	@Test
	public void testNonAdaptableOp() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.unadaptable").inType(Double[].class).outType(Double[].class)
			.function();
		// make sure that we cannot match the Op via adaptation even when adaptation
		// is allowed (since it declares itself to be unadaptable)
		try {
			ops.op("test.adaptation.unadaptable").inType(Double[].class).outType(
				Double[].class).computer();
			throw new IllegalStateException("This op call should not match!");
		} catch( OpMatchingException e) {
		}
	}

	@Test
	public void testNonAdaptableOpPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.unadaptable").inType(Double[].class).outType(Double[].class)
			.function(hints);
		// make sure that we cannot match the Op via adaptation even when adaptation
		// is allowed (since it declares itself to be unadaptable)
		try {
			ops.op("test.adaptation.unadaptable").inType(Double[].class).outType(
				Double[].class).computer(hints);
			throw new IllegalStateException("This op call should not match!");
		} catch( OpMatchingException e) {
		}
	}

}
