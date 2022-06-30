
package org.scijava.ops.engine.hints;


import java.util.function.Function;

import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpHints;
import org.scijava.ops.api.features.BaseOpHints.Simplification;
import org.scijava.ops.api.features.OpMatchingException;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.conversionLoss.impl.IdentityLossReporter;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.simplify.PrimitiveArraySimplifiers;
import org.scijava.ops.engine.simplify.PrimitiveSimplifiers;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class SimplificationHintTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeClass
	public static void addNeededOps() {
		ops.register(new SimplificationHintTest());
		ops.register(new IdentityLossReporter());
		ops.register(new PrimitiveSimplifiers());
		ops.register(new PrimitiveArraySimplifiers());
	}

	@OpField(names = "test.simplification.hints")
	public final Function<Double[], Double[]> op = (in) -> new Double[in.length];

	@Test
	public void testSimplification() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		ops.setDefaultHints(hints);
		@SuppressWarnings("unused")
		Function<Integer[], Integer[]> adaptable = ops.op(
			"test.simplification.hints").inType(Integer[].class).outType(
				Integer[].class).function();
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Simplification.FORBIDDEN);
		ops.setDefaultHints(hints);
		try {
			ops.op("test.simplification.hints").inType(Integer[].class).outType(
				Integer[].class).function();
			throw new IllegalStateException(
				"Simplification is forbidden - this op call should not match!");
		}
		catch (OpMatchingException e) {
		}

	}

	@Test
	public void testSimplificationPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
		@SuppressWarnings("unused")
		Function<Integer[], Integer[]> adaptable = ops.op(
			"test.simplification.hints").inType(Integer[].class).outType(
				Integer[].class).function(hints);
		// make sure we cannot find the Op when adaptation is not allowed
		hints = hints.plus(Simplification.FORBIDDEN);
		try {
			ops.op("test.simplification.hints").inType(Integer[].class).outType(
				Integer[].class).function(hints);
			throw new IllegalStateException(
				"Simplification is forbidden - this op call should not match!");
		}
		catch (OpMatchingException e) {
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
		ops.setDefaultHints(hints);
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
		catch (OpMatchingException e) {
		}

	}

	@Test
	public void testUnsimplifiableOpPerCallHints() {
		// make sure we can find the Op when adaptation is allowed
		Hints hints = new DefaultHints();
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
		catch (OpMatchingException e) {
		}

	}

}
