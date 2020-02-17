package org.scijava.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.Before;
import org.scijava.Context;
import org.scijava.ops.core.builder.OpBuilder;

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
	
	protected static OpBuilder op(String name) {
		return new OpBuilder(ops, name);
	}
	
	protected static boolean arrayEquals(double[] arr1, Double... arr2) {
		return Arrays.deepEquals(Arrays.stream(arr1).boxed().toArray(Double[]::new), arr2);
	}

	protected static <T> void assertIterationsEqual(final Iterable<T> expected,
		final Iterable<T> actual)
	{
		final Iterator<T> e = expected.iterator();
		final Iterator<T> a = actual.iterator();
		while (e.hasNext()) {
			assertTrue("Fewer elements than expected", a.hasNext());
			assertEquals(e.next(), a.next());
		}
		assertFalse("More elements than expected", a.hasNext());
	}
}
