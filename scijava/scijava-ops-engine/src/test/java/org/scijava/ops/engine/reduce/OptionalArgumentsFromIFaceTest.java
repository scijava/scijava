package org.scijava.ops.engine.reduce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

public class OptionalArgumentsFromIFaceTest extends AbstractTestEnvironment
		implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OptionalArgumentsFromIFaceTest());
		ops.register(new TestOpOptionalFromIFace());
	}

	@OpMethod(names = "test.optionalSubtract", type = BiFunctionWithOptional.class)
	public static Double foo(Double i1, Double i2, Double i3) {
		if (i3 == null) i3 = 0.;
		return i1 - i2 - i3;
	}

	@Test
	public void testMethodWithOneOptional() {
		Double o = ops.op("test.optionalSubtract").arity3().input(2., 5., 7.).outType(Double.class).apply();
		Double expected = -10.0;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testMethodWithoutOptionals() {
		Double o = ops.op("test.optionalSubtract").arity2().input(2., 5.).outType(Double.class).apply();
		Double expected = -3.0;
		Assertions.assertEquals(expected, o);
	}

	@OpField(names = "test.optionalAnd", params = "in1, in2, in3")
	public final BiFunctionWithOptional<Boolean, Boolean, Boolean, Boolean> bar =
		(in1, in2, in3) -> {
			if (in3 == null) in3 = true;
			return in1 & in2 & in3;
		};

	@Test
	public void testFieldWithOptionals() {
		Boolean in1 = true;
		Boolean in2 = true;
		Boolean in3 = false;
		Boolean o = ops.op("test.optionalAnd").arity3().input(in1, in2, in3).outType(Boolean.class).apply();
		Boolean expected = false;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testFieldWithoutOptionals() {
		Boolean in1 = true;
		Boolean in2 = true;
		Boolean o = ops.op("test.optionalAnd").arity2().input(in1, in2).outType(Boolean.class).apply();
		Boolean expected = true;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testClassWithOptionals() {
		Boolean in1 = true;
		Boolean in2 = false;
		Boolean in3 = false;
		Boolean o = ops.op("test.optionalOr").arity3().input(in1, in2, in3).outType(Boolean.class).apply();
		Boolean expected = true;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testClassWithoutOptionals() {
		Boolean in1 = true;
		Boolean in2 = false;
		Boolean o = ops.op("test.optionalOr").arity2().input(in1, in2).outType(Boolean.class).apply();
		Boolean expected = true;
		Assertions.assertEquals(expected, o);
	}

}
