
package org.scijava.ops.test.test;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpRetrievalException;

/**
 * Tests the ability to interact with an {@link OpEnvironment} to perform
 * all of the abilities of SciJava Ops, using only the API.
 */
public class OpEnvironmentTest {

	@Test
	public void testOpEnvironmentObtainable() {
		Assertions.assertNotNull(OpEnvironment.getEnvironment());
	}

	/**
	 * Tests that an Op can be matched and run
	 */
	@Test
	public void testOpExecutions() {
		OpEnvironment ops = OpEnvironment.getEnvironment();
		var sum = ops.op("math.add").arity2().input(5., 6.).apply();
		Assertions.assertEquals(sum, 11.);
	}

	/**
	 * Tests that descriptions can be obtained
	 */
	@Test
	public void testOpDescriptions() {
		OpEnvironment ops = OpEnvironment.getEnvironment();
		var descriptions = ops.descriptions("math.add");
		Assertions.assertInstanceOf(List.class, descriptions);
		Assertions.assertInstanceOf(String.class, descriptions.get(0));
	}

	/**
	 * Tests that hints can be declared and used to alter Op matching
	 */
	@Test
	public void testOpHints() {
		long in = 5;
		long exponent = 5;
		OpEnvironment ops = OpEnvironment.getEnvironment();
		// Assert there are no "math.pow" Ops that deal with longs
		var descriptions = ops.descriptions("math.pow");
		Assertions.assertNotEquals(0, descriptions.size(),
			"Expected at least one math.pow Op");
		for (var description : descriptions) {
			Assertions.assertFalse(description.toLowerCase().contains("long"),
				"Found a math.pow Op that deals with Longs - testing the hints won't work here!");
		}
		// Ensure an Op matches without simplification
		// NB this call must come first, or the cache will be hit based on the previous call.
		Hints h = new Hints("simplification.FORBIDDEN");
		Assertions.assertThrows(OpRetrievalException.class, () -> ops.op("math.pow", h).arity2().input(in, exponent).outType(Long.class).apply());

		// Ensure an Op matches with simplification
		var power = ops.op("math.pow").arity2().input(in, exponent).outType(Long.class).apply();
		Assertions.assertEquals((long) Math.pow(in, exponent), power);
	}


}
