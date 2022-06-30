package org.scijava.ops.engine.impl;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.types.GenericTyped;
import org.scijava.types.Nil;

public class LambdaTypeBakerTest {

	@Test
	public void testBakeType() {
		Function<Double, Double> func = (in) -> in * 2;
		Type funcType = new Nil<Function<Double, Double>>() {}.getType();

		Function<Double, Double> wrappedFunction = LambdaTypeBaker.bakeLambdaType(func,
			funcType);

		Assert.assertTrue("wrappedFunction should be a GenericTyped but is not!",
			wrappedFunction instanceof GenericTyped);
		Type type = ((GenericTyped) wrappedFunction).getType();
		Assert.assertEquals("wrappedFunction type " + type +
			"is not equivalent to the provided type " + funcType + "!", funcType,
			type);
	}

}
