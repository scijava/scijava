package org.scijava.ops.python;

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
		NDArray sum = env.op("create.img").input(2, 3).outType(NDArray.class)
				.apply();
	}

}
