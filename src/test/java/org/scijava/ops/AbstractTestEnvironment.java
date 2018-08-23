package org.scijava.ops;

import org.junit.After;
import org.junit.Before;
import org.scijava.Context;
import org.scijava.ops.base.OpService;

public abstract class AbstractTestEnvironment {

	protected Context context;
	protected OpService ops;

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
}
