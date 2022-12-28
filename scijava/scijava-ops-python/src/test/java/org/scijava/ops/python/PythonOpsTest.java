package org.scijava.ops.python;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

import jep.DirectNDArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.engine.DefaultOpEnvironment;

import jep.NDArray;

public class PythonOpsTest {

	private static OpEnvironment env;

	/**
	 * Create an {@link OpEnvironment} that discovers only YAML-declared Ops
	 */
	@BeforeAll
	public static void setup() {
		env = new DefaultOpEnvironment();
	}

	@Test
	public void opsTest() {
		List<Integer> size = Arrays.asList(2, 2);
		NDArray sum = env.op("create.img").input(size).outType(NDArray.class)
				.apply();
		Assertions.assertArrayEquals(sum.getDimensions(), new int[] { 2, 2 });

		float[] f = new float[] {2.0f, 1.0f, 1.0f, 2.0f};
		NDArray<float[]> input = new NDArray<>(f, 2, 2);
		Float output = env.op("numpy.linalg.det").input(input).outType(Float.class)
				.apply();
		Assertions.assertEquals(3.0, output,1e-6);

	}

}
