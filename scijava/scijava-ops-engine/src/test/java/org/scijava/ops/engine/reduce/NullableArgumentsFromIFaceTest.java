package org.scijava.ops.engine.reduce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

public class NullableArgumentsFromIFaceTest extends AbstractTestEnvironment
		implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new NullableArgumentsFromIFaceTest());
		ops.register(new TestOpNullableFromIFace());
	}

	@OpMethod(names = "test.nullableSubtract", type = BiFunctionWithNullable.class)
	public static Double foo(Double i1, Double i2, Double i3) {
		if (i3 == null) i3 = 0.;
		return i1 - i2 - i3;
	}

	@Test
	public void testMethodWithOneNullable() {
		Double o = ops.op("test.nullableSubtract").arity3().input(2., 5., 7.).outType(Double.class).apply();
		Double expected = -10.0;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testMethodWithoutNullables() {
		Double o = ops.op("test.nullableSubtract").arity2().input(2., 5.).outType(Double.class).apply();
		Double expected = -3.0;
		Assertions.assertEquals(expected, o);
	}

	@OpField(names = "test.nullableAnd", params = "in1, in2, in3")
	public final BiFunctionWithNullable<Boolean, Boolean, Boolean, Boolean> bar =
		(in1, in2, in3) -> {
			if (in3 == null) in3 = true;
			return in1 & in2 & in3;
		};

	@Test
	public void testFieldWithNullables() {
		Boolean in1 = true;
		Boolean in2 = true;
		Boolean in3 = false;
		Boolean o = ops.op("test.nullableAnd").arity3().input(in1, in2, in3).outType(Boolean.class).apply();
		Boolean expected = false;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testFieldWithoutNullables() {
		Boolean in1 = true;
		Boolean in2 = true;
		Boolean o = ops.op("test.nullableAnd").arity2().input(in1, in2).outType(Boolean.class).apply();
		Boolean expected = true;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testClassWithNullables() {
		Boolean in1 = true;
		Boolean in2 = false;
		Boolean in3 = false;
		Boolean o = ops.op("test.nullableOr").arity3().input(in1, in2, in3).outType(Boolean.class).apply();
		Boolean expected = true;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testClassWithoutNullables() {
		Boolean in1 = true;
		Boolean in2 = false;
		Boolean o = ops.op("test.nullableOr").arity2().input(in1, in2).outType(Boolean.class).apply();
		Boolean expected = true;
		Assertions.assertEquals(expected, o);
	}

}
