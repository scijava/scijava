
package org.scijava.ops.engine.matcher;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.features.DependencyMatchingException;
import org.scijava.ops.api.features.OpMatchingException;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.adapt.functional.ComputersToFunctionsViaFunction;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;

public class DefaultMatchingErrorTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeClass
	public static void addNeededOps() {
		discoverer.register(DefaultMatchingErrorTest.class, "opcollection");
		discoverer.register(DependentOp.class, "opcollection");
		discoverer.register(FurtherDependentOp.class, "opcollection");
		discoverer.register(MissingDependencyOp.class, "opcollection");
		discoverer.register(MissingDependencyOpArr1.class, "opcollection");
		discoverer.register(MissingDependencyOpArr2.class, "opcollection");
		discoverer.register(ComputersToFunctionsViaFunction.Computer1ToFunction1ViaFunction.class, "op");
		discoverer.register(CreateOpCollection.class, "opcollection");
	}

	@OpField(names = "test.duplicateOp")
	public final Function<Double, Double> duplicateA = (in) -> in;

	@OpField(names = "test.duplicateOp")
	public final Function<Double, Double> duplicateB = (in) -> in;

	/**
	 * Assert a relevant (i.e. informational) message is thrown when multiple Ops
	 * conflict for a given OpRef.
	 */
	@Test
	public void duplicateErrorRegressionTest() {
		try {
			ops.op("test.duplicateOp").inType(Double.class).outType(Double.class)
				.function();
			Assert.fail();
		}
		catch (OpMatchingException e) {
			Assert.assertTrue(e.getMessage().startsWith(
				"No MatchingRoutine was able to produce a match!"));
			Assert.assertTrue(Arrays.stream(e.getSuppressed()).anyMatch(s -> s
				.getMessage().startsWith("Multiple 'test.duplicateOp/" +
					"java.util.function.Function<java.lang.Double, java.lang.Double>' " +
					"ops of priority 0.0:")));
		}
	}

	/**
	 * Assert that a relevant error is thrown when a dependency is missing
	 */
	@Test
	public void missingDependencyRegressionTest() {
		try {
			ops.op("test.missingDependencyOp").input(1.).outType(Double.class)
				.apply();
			Assert.fail("Expected DependencyMatchingException");
		}
		catch (DependencyMatchingException e) {
			String message = e.getMessage();
			Assert.assertTrue(message.contains("Name: \"test.nonexistingOp\""));
		}
	}

	/**
	 * Assert the DependencyMatchingException contains both dependencies in the
	 * chain
	 */
	@Test
	public void missingNestedDependencyRegressionTest() {
		try {
			ops.op("test.outsideOp").input(1.).outType(Double.class).apply();
			Assert.fail("Expected DependencyMatchingException");
		}
		catch (DependencyMatchingException e) {
			String message = e.getMessage();
			Assert.assertTrue(message.contains("Name: \"test.missingDependencyOp\""));
			Assert.assertTrue(message.contains("Name: \"test.nonexistingOp\""));
		}
	}

	/**
	 * Assert that DependencyMatchingExceptions do not omit relevant information
	 * when an adaptation is involved.
	 */
	@Test
	public void missingDependencyViaAdaptationTest() {
		Double[] d = new Double[0];
		try {
			ops.op("test.adaptMissingDep").input(d).outType(Double[].class).apply();
			Assert.fail("Expected DependencyMatchingException");
		}
		catch (DependencyMatchingException e) {
			String message = e.getMessage();
			Assert.assertTrue(message.contains("Adaptor:"));
			Assert.assertTrue(message.contains("Name: \"test.nonexistingOp\""));

		}
	}

}

@OpClass(names = "test.furtherOutsideOp")
class FurtherDependentOp implements Function<Double, Double>, Op {

	@OpDependency(name = "test.outsideOp")
	private Function<Double, Double> op;

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double apply(Double t) {
		return op.apply(t);
	}

}

@OpClass(names = "test.outsideOp")
class DependentOp implements Function<Double, Double>, Op {

	@OpDependency(name = "test.missingDependencyOp")
	private Function<Double, Double> op;

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double apply(Double t) {
		return op.apply(t);
	}

}

@OpClass(names = "test.missingDependencyOp")
class MissingDependencyOp implements Function<Double, Double>, Op {

	@OpDependency(name = "test.nonexistingOp")
	private Function<Double, Double> op;

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double apply(Double t) {
		return op.apply(t);
	}

}

@OpClass(names = "test.adaptMissingDep")
class MissingDependencyOpArr1 implements Computers.Arity1<Double[], Double[]>, Op {

	@OpDependency(name = "test.nonexistingOp")
	private Function<Double, Double> op;

	/**
	 * @param t the input
	 * @param out the output
	 */
	@Override
	public void compute(Double[] t, Double[] out) {}

}

@OpClass(names = "test.adaptMissingDep")
class MissingDependencyOpArr2 implements Function<Double, Double>, Op {

	@OpDependency(name = "test.nonexistingOp")
	private Function<Double, Double> op;

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double apply(Double t) {
		return op.apply(t);
	}

}
