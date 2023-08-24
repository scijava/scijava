package org.scijava.ops.engine.hints;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.Hints;
import org.scijava.ops.spi.OpHints;
import org.scijava.ops.api.OpRetrievalException;
import org.scijava.ops.engine.BaseOpHints.Adaptation;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.adapt.functional.FunctionsToComputers;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class AdaptationHintTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new AdaptationHintTest());
		ops.register(new FunctionsToComputers.Function1ToComputer1());
		ops.register(new CopyOpCollection());
	}

	@OpField(names = "test.adaptation.hints")
	public final Function<Double[], Double[]> op = (in) -> new Double[in.length];

	@Test
	public void testAdaptation() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Computers.Arity1<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.hints").arity1().inType(Double[].class).outType(Double[].class)
			.computer();
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Adaptation.FORBIDDEN);
		ops.setDefaultHints(hints);
		try {
			ops.op("test.adaptation.hints").arity1().inType(Double[].class).outType(
				Double[].class).computer();
			throw new IllegalStateException("This op call should not match!");
		} catch( OpRetrievalException e) {
		}
	}

	@Test
	public void testAdaptationPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		@SuppressWarnings("unused")
		Computers.Arity1<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.hints", hints).arity1().inType(Double[].class).outType(Double[].class)
			.computer();
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Adaptation.FORBIDDEN);
		try {
			ops.op("test.adaptation.hints", hints).arity1().inType(Double[].class).outType(
				Double[].class).computer();
			throw new IllegalStateException("This op call should not match!");
		} catch( OpRetrievalException e) {
		}
	}

	@OpHints(hints = {Adaptation.FORBIDDEN})
	@OpField(names = "test.adaptation.unadaptable")
	public final Function<Double[], Double[]> nonAdaptableOp = (in) -> new Double[in.length];

	@Test
	public void testNonAdaptableOp() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.unadaptable").arity1().inType(Double[].class).outType(Double[].class)
			.function();
		// make sure that we cannot match the Op via adaptation even when adaptation
		// is allowed (since it declares itself to be unadaptable)
		try {
			ops.op("test.adaptation.unadaptable").arity1().inType(Double[].class).outType(
				Double[].class).computer();
			throw new IllegalStateException("This op call should not match!");
		} catch( OpRetrievalException e) {
			// NB: expected behavior.
		}
	}

	@Test
	public void testNonAdaptableOpPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new Hints();
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.unadaptable", hints).arity1().inType(Double[].class).outType(Double[].class)
			.function();
		// make sure that we cannot match the Op via adaptation even when adaptation
		// is allowed (since it declares itself to be unadaptable)
		try {
			ops.op("test.adaptation.unadaptable", hints).arity1().inType(Double[].class).outType(
				Double[].class).computer();
			throw new IllegalStateException("This op call should not match!");
		} catch( OpRetrievalException e) {
		}
	}

}
