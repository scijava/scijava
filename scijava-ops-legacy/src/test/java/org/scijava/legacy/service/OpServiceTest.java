package org.scijava.legacy.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.Context;

public class OpServiceTest {

	protected static Context context;

	protected static OpService ops;

	@BeforeAll
	public static void setUp() {
		context = new Context(OpService.class);
		ops = context.getService(OpService.class);
	}

	@AfterAll
	public static void tearDown() {
		context.dispose();
	}

	@Test
	public void testOpService() {
		Double result = ops.env().binary("math.add").input(2., 3.).outType(Double.class).apply();
	}



}
