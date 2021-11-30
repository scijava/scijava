package org.scijava.ops.engine.impl;

import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.types.GenericTyped;
import org.scijava.types.Nil;

public class LambdaTypeBakerTest {

	@Test
	public void testBakeType() {
		Function<Double, Double> func = (in) -> in * 2;
		Type funcType = new Nil<Function<Double, Double>>() {
		}.getType();

		Function<Double, Double> wrappedFunction = LambdaTypeBaker.bakeLambdaType(func, funcType);

		Assertions.assertTrue(wrappedFunction instanceof GenericTyped,
				"wrappedFunction should be a GenericTyped but is not!");
		Type type = ((GenericTyped) wrappedFunction).getType();
		Assertions.assertEquals(funcType, type,
				"wrappedFunction type " + type + "is not equivalent to the provided type " + funcType + "!");
	}

}
