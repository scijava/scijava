package org.scijava.ops.adapt;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.core.builder.OpBuilder;

public class FunctionToComputerTransformTest extends AbstractTestEnvironment {

	@Test
	public void testFunction1ToComputer1() {
		double[] input = { 2, 4 };
		double[] output = { 0, 0 };
		new OpBuilder(ops, "test.FtC").input(input).output(output).compute();
		Assert.assertArrayEquals(input, output, 0);
	}

}
