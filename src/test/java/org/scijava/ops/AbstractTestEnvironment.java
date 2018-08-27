package org.scijava.ops;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.scijava.Context;
import org.scijava.ops.base.OpService;

public abstract class AbstractTestEnvironment {

	private static Context context;
	private static OpService ops;

	@Before
	public void setUp() {
		context = new Context(OpService.class);
		ops = context.service(OpService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
		context = null;
		ops = null;
	}
	
	static OpService ops() {
		return ops;
	}
	
	static boolean arrayEquals(double[] arr1, Double... arr2) {
		return Arrays.deepEquals(Arrays.stream(arr1).boxed().toArray(Double[]::new), arr2);
	}
}
