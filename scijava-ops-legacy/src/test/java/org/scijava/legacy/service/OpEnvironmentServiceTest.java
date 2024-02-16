
package org.scijava.legacy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.script.ScriptService;

/**
 * Tests {@link OpEnvironmentService} API.
 *
 * @author Gabriel Selzer
 */
public class OpEnvironmentServiceTest {

	/**
	 * Tests that an {@link OpEnvironmentService} produces an
	 * {@link OpEnvironment} with some Ops in it.
	 */
	@Test
	public void testOpEnvironmentService() {
		Context ctx = new Context(OpEnvironmentService.class);
		OpEnvironmentService ops = ctx.getService(OpEnvironmentService.class);
		Double result = ops.env().binary("math.add") //
			.input(2., 3.) //
			.outType(Double.class) //
			.apply();
		Assertions.assertEquals(5., result);
		ctx.dispose();
	}

	/**
	 * Test that when a {@link ScriptService} is present, the "OpEnvironment"
	 * alias points to {@link OpEnvironment}.
	 */
	@Test
	public void testOpEnvironmentServiceAliases() {
		Context ctx = new Context(OpEnvironmentService.class, ScriptService.class);
		ScriptService script = ctx.getService(ScriptService.class);
		// Assert the correct alias for OpEnvironment
		Assertions.assertEquals( //
			OpEnvironment.class, //
			script.getAliases().get("OpEnvironment") //
		);
		// Assert no alias for OpEnvironmentService
		Assertions.assertNull(script.getAliases().get("OpEnvironmentService"));
		ctx.dispose();
	}

}
