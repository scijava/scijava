package org.scijava.ops;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.scijava.Context;

public abstract class AbstractTestEnvironment {

	protected static Context context;
	protected static OpService ops;

	@BeforeClass
	public static void setUp() {
		context = new Context(OpService.class);
		ops = context.service(OpService.class);
	}

	@AfterClass
	public static void tearDown() {
		context.dispose();
		context = null;
		ops = null;
	}
	
	protected static boolean arrayEquals(double[] arr1, Double... arr2) {
		return Arrays.deepEquals(Arrays.stream(arr1).boxed().toArray(Double[]::new), arr2);
	}
}
