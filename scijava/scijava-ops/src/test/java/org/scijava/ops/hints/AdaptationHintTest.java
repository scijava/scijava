package org.scijava.ops.hints;

import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.functions.Computers;
import org.scijava.ops.hints.BaseOpHints.Adaptation;
import org.scijava.ops.hints.impl.DefaultHints;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class AdaptationHintTest extends AbstractTestEnvironment {

	@OpField(names = "test.adaptation.hints")
	public final Function<Double[], Double[]> op = (in) -> new Double[in.length];

	@Test
	public void testAdaptation() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		hints.setHint(Adaptation.ALLOWED);
		ops.env().setHints(hints);
		@SuppressWarnings("unused")
		Computers.Arity1<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.hints").inType(Double[].class).outType(Double[].class)
			.computer();
		// make sure we cannot find the Op when adaptation is not allowed
		hints.setHint(Adaptation.FORBIDDEN);
		ops.env().setHints(hints);
		try {
			ops.op("test.adaptation.hints").inType(Double[].class).outType(
				Double[].class).computer();
			throw new IllegalStateException("This op call should not match!");
		} catch( IllegalArgumentException e) {
			assertTrue(e.getCause() instanceof OpMatchingException);
		}
	}

	@Test
	public void testAdaptationPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		hints.setHint(Adaptation.ALLOWED);
		@SuppressWarnings("unused")
		Computers.Arity1<Double[], Double[]> adaptable = ops.op(
			"test.adaptation.hints").inType(Double[].class).outType(Double[].class)
			.computer(hints);
		// make sure we cannot find the Op when adaptation is not allowed
		hints.setHint(Adaptation.FORBIDDEN);
		try {
			ops.op("test.adaptation.hints").inType(Double[].class).outType(
				Double[].class).computer(hints);
			throw new IllegalStateException("This op call should not match!");
		} catch( IllegalArgumentException e) {
			assertTrue(e.getCause() instanceof OpMatchingException);
		}
	}

	@OpHints(hints = {Adaptation.FORBIDDEN})
	@OpField(names = "test.adaptation.unadaptable")
	public final Function<Double[], Double[]> nonAdaptableOp = (in) -> new Double[in.length];

	@Test
	public void testNonAdaptableOp() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		hints.setHint(Adaptation.ALLOWED);
		ops.env().setHints(hints);
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
		} catch( IllegalArgumentException e) {
			assertTrue(e.getCause() instanceof OpMatchingException);
		}
	}

	@Test
	public void testNonAdaptableOpPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		hints.setHint(Adaptation.ALLOWED);
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
		} catch( IllegalArgumentException e) {
			assertTrue(e.getCause() instanceof OpMatchingException);
		}
	}

}
