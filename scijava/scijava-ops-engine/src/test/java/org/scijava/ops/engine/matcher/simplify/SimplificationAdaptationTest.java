package org.scijava.ops.engine.matcher.simplify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.adapt.functional.ComputersToFunctionsViaFunction;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class SimplificationAdaptationTest<T> extends AbstractTestEnvironment
		implements OpCollection {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new SimplificationAdaptationTest());
		ops.register(new PrimitiveSimplifiers());
		ops.register(new PrimitiveArraySimplifiers());
		ops.register(new CopyOpCollection());
		ops.register(new CreateOpCollection());
		Object[] adapters = objsFromNoArgConstructors(ComputersToFunctionsViaFunction.class.getDeclaredClasses());
		ops.register(adapters);
	}

	@OpField(names = "test.math.modulus")
	public final Computers.Arity2<Integer[], Integer, Integer[]> modOp = (inArr, mod, outArr) -> {
		for(int i = 0; i < inArr.length && i < outArr.length; i++) {
			outArr[i] = inArr[i] % mod;
		}
	};

	@Test
	public void adaptAndSimplifyTest() {
		Double[] inArr = { 1., 4., 6. };
		Double modulus = 3.;

		Double[] expected = { 1., 1., 0. };
		Double[] actual = ops.op("test.math.modulus").arity2().input(inArr, modulus)
			.outType(Double[].class).apply();
		Assertions.assertArrayEquals(expected, actual);
	}

}
