
package org.scijava.ops.engine.adapt.functional;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.adapt.functional.ComputersToFunctionsViaFunction.Computer1ToFunction1ViaFunction;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

public class ComputerToFunctionObjectOutputTest extends AbstractTestEnvironment
	implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		// Register Ops from this class
		ops.register(new ComputerToFunctionObjectOutputTest());
		// Register needed adapt Op
		ops.register(new Computer1ToFunction1ViaFunction<>());
	}

	@OpMethod(names = "test.objectOutput", type = Computers.Arity1.class)
	public static void computer(final double[] input, final double[] output) {
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i] + 1;
		}
	}

	@OpField(names = "create")
	public final Function<double[], double[]> creator =
		in -> new double[in.length];

	@Test
	public void testObjectOutput() {
		double[] input = { 1.0, 2.0, 3.0, 4.0 };
		double[] output = new double[input.length];
		ops.op("test.objectOutput").arity1().input(input).output(output).compute();
		for (int i = 0; i < input.length; i++) {
			Assertions.assertEquals(output[i], input[i] + 1);
		}

	}

}
