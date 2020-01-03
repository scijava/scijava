package org.scijava.ops.adapt;


import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.core.builder.OpBuilder;

public class ComputerToFunctionTransformTest extends AbstractTestEnvironment {
	
	@Test
	public void testComputer1ToFunction1() {
		double[] input = {2, 4};
		double[] output = new OpBuilder(ops, "test.CtF").input(input).outType(double[].class).apply();
		Assert.assertArrayEquals(input, output, 0);
	}

}
