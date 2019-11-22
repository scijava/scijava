package org.scijava.ops;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Before;
import org.scijava.Context;

public abstract class AbstractTestEnvironment {

	protected static Context context;
	protected static OpService ops;

	@Before
	public void setUp() {
		if(context == null) setUpSafe();
	}
	
	private synchronized void setUpSafe() {
		if(context != null) return; 
		Context ctx = createContext();
		ops = ctx.service(OpService.class);
		context = ctx;
	}
	
	protected Context createContext() {
		return new Context(OpService.class);
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
