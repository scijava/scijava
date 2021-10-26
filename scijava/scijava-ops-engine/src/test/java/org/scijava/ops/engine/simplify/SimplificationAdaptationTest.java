package org.scijava.ops.engine.simplify;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.function.Computers;
import org.scijava.ops.engine.BarebonesTestEnvironment;
import org.scijava.ops.engine.adapt.functional.ComputersToFunctionsViaFunction;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class SimplificationAdaptationTest<T> extends BarebonesTestEnvironment implements OpCollection {

	@BeforeClass
	public static void AddNeededOps() {
		discoverer.register(SimplificationAdaptationTest.class, "opcollection");
		discoverer.register(PrimitiveSimplifiers.class, "opcollection");
		discoverer.register(PrimitiveArraySimplifiers.class, "opcollection");
		discoverer.register(CopyOpCollection.class, "opcollection");
		discoverer.register(CreateOpCollection.class, "opcollection");
		discoverer.registerAll(ComputersToFunctionsViaFunction.class.getDeclaredClasses(), "op");
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
		Double[] actual = ops.op("test.math.modulus").input(inArr, modulus)
			.outType(Double[].class).apply();
		Assert.assertArrayEquals(expected, actual);
	}

}
