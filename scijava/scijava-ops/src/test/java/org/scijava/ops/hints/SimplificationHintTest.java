
package org.scijava.ops.hints;

import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.hints.BaseOpHints.Adaptation;
import org.scijava.ops.hints.BaseOpHints.Simplification;
import org.scijava.ops.hints.impl.DefaultHints;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class SimplificationHintTest extends AbstractTestEnvironment {

	@OpField(names = "test.simplification.hints")
	public final Function<Double[], Double[]> op = (in) -> new Double[in.length];

	@Test
	public void testSimplification() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		hints.setHint(Simplification.ALLOWED);
		ops.env().setHints(hints);
		@SuppressWarnings("unused")
		Function<Integer[], Integer[]> adaptable = ops.op(
			"test.simplification.hints").inType(Integer[].class).outType(
				Integer[].class).function();
		// make sure we cannot find the Op when adaptation is not allowed
		hints.setHint(Simplification.FORBIDDEN);
		ops.env().setHints(hints);
		try {
			ops.op("test.simplification.hints").inType(Integer[].class).outType(
				Integer[].class).function();
			throw new IllegalStateException(
				"Simplification is forbidden - this op call should not match!");
		}
		catch (IllegalArgumentException e) {
			assertTrue(e.getCause() instanceof OpMatchingException);
		}

	}

	@Test
	public void testSimplificationPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		hints.setHint(Simplification.ALLOWED);
		@SuppressWarnings("unused")
		Function<Integer[], Integer[]> adaptable = ops.op(
			"test.simplification.hints").inType(Integer[].class).outType(
				Integer[].class).function(hints);
		// make sure we cannot find the Op when adaptation is not allowed
		hints.setHint(Simplification.FORBIDDEN);
		try {
			ops.op("test.simplification.hints").inType(Integer[].class).outType(
				Integer[].class).function(hints);
			throw new IllegalStateException(
				"Simplification is forbidden - this op call should not match!");
		}
		catch (IllegalArgumentException e) {
			assertTrue(e.getCause() instanceof OpMatchingException);
		}

	}

	@OpHints(hints = { Simplification.FORBIDDEN })
	@OpField(names = "test.simplification.unsimplifiable")
	public final Function<Double[], Double[]> nonAdaptableOp = (
		in) -> new Double[in.length];

	@Test
	public void testUnsimplifiableOp() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		hints.setHint(Simplification.ALLOWED);
		ops.env().setHints(hints);
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.simplification.unsimplifiable").inType(Double[].class).outType(
				Double[].class).function();
		// make sure that we cannot match the Op via adaptation even when
		// simplification
		// is allowed (since it declares itself to be unsimplifiable)
		try {
			ops.op("test.simplification.unsimplifiable").inType(Integer[].class)
				.outType(Integer[].class).function();
			throw new IllegalStateException(
				"The only relevant Op is not simplifiable - this op call should not match!");
		}
		catch (IllegalArgumentException e) {
			assertTrue(e.getCause() instanceof OpMatchingException);
		}

	}

	@Test
	public void testUnsimplifiableOpPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		hints.setHint(Simplification.ALLOWED);
		@SuppressWarnings("unused")
		Function<Double[], Double[]> adaptable = ops.op(
			"test.simplification.unsimplifiable").inType(Double[].class).outType(
				Double[].class).function(hints);
		// make sure that we cannot match the Op via adaptation even when
		// simplification
		// is allowed (since it declares itself to be unsimplifiable)
		try {
			ops.op("test.simplification.unsimplifiable").inType(Integer[].class)
				.outType(Integer[].class).function(hints);
			throw new IllegalStateException(
				"The only relevant Op is not simplifiable - this op call should not match!");
		}
		catch (IllegalArgumentException e) {
			assertTrue(e.getCause() instanceof OpMatchingException);
		}

	}

}
