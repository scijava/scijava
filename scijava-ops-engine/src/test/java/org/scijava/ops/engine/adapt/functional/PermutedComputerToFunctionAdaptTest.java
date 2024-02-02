
package org.scijava.ops.engine.adapt.functional;

import java.util.function.BiFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;

/**
 * Tests that a permuted computer, such as {@link Computers.Arity2_1}, can be
 * adapted as a {@link BiFunction}.
 *
 * @author Gabriel Selzer
 */
public class PermutedComputerToFunctionAdaptTest extends AbstractTestEnvironment
	implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new PermutedComputerToFunctionAdaptTest());
		ops.register(
			new ComputersToFunctionsViaFunction.Computer2ToFunction2ViaFunction<>());
		ops.register(new CreateOpCollection());
	}

	@OpMethod(names = "adapt.permuted", type = Computers.Arity2_1.class)
	public static void compute(@Container double[] out, double[] in1,
		double[] in2)
	{
		for (int i = 0; i < in1.length; i++) {
			out[i] = in1[i] * in2[i];
		}
	}

	@Test
	public void testAdaptingPermutedComputer() {
		double[] result = ops.binary("adapt.permuted").input(new double[] { 1.5 },
			new double[] { 2.0 }).outType(double[].class).apply();
		Assertions.assertEquals(3.0, result[0]);
	}

}
