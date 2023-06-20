package org.scijava.ops.engine.reduce;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Producer;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpMethod;
import org.scijava.ops.spi.Optional;

public class ReductionWithDependenciesTest extends AbstractTestEnvironment
		implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new ReductionWithDependenciesTest());
	}

	@OpMethod(names = "test.fooDependency", type = Producer.class)
	public static Double bar() {
		return 5.;
	}
	
	@OpMethod(names = "test.optionalWithDependency", type = Function.class)
	public static Double foo(@OpDependency(name = "test.fooDependency") Producer<Double> bar, @Optional
			Double opt) {
		if (opt == null) opt = 0.;
		return bar.create() + opt;
	}

	@Test
	public void testDependencyFirstMethodWithOptional() {
		Double opt = 7.;
		Double o = ops.op("test.optionalWithDependency").arity1().input(opt).outType(Double.class).apply();
		Double expected = 12.;
		Assertions.assertEquals(expected, o);
	}

	@Test
	public void testDependencyFirstMethodWithoutOptional() {
		Double o = ops.op("test.optionalWithDependency").arity0().outType(Double.class).create();
		Double expected = 5.;
		Assertions.assertEquals(expected, o);
	}

}
