package org.scijava.ops.simplify;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.function.Computers;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpField;
import org.scijava.ops.api.OpField;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class SimplificationAdaptationTest<T> extends AbstractTestEnvironment {

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
