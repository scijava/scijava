/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpMatchingException;

/**
 * Tests the ability to interact with an {@link OpEnvironment} to perform
 * all of the abilities of SciJava Ops, using only the API.
 */
public class OpsAPITest {

	public static void testOpEnvironmentObtainable() {
		Assertions.assertNotNull(OpEnvironment.build());
	}

	/**
	 * Tests that an Op can be matched and run
	 */
	public static void testOpExecutions() {
		OpEnvironment ops = OpEnvironment.build();
		var sum = ops.op("math.add").arity2().input(5., 6.).apply();
		Assertions.assertEquals(sum, 11.);
	}

	/**
	 * Tests that descriptions can be obtained
	 */
	public static void testOpHelp() {
		OpEnvironment ops = OpEnvironment.build();
		var descriptions = ops.help("math.add");
		Assertions.assertInstanceOf(String.class, descriptions);
	}

	/**
	 * Tests that hints can be declared and used to alter Op matching
	 */
	public static void testOpHints() {
		long in = 5;
		long exponent = 5;
		OpEnvironment ops = OpEnvironment.build();
		// Assert there are no "math.pow" Ops that deal with longs
		var help = ops.help("math.pow");
		Assertions.assertNotEquals("No Ops found matching this request.", help,
			"Expected at least one math.pow Op");
		Assertions.assertFalse(help.toLowerCase().contains("long"),
			"Found a math.pow Op that deals with Longs - testing the hints won't work here!");
		// Ensure an Op matches without simplification
		// NB this call must come first, or the cache will be hit based on the previous call.
		Hints h = new Hints("simplification.FORBIDDEN");
		Assertions.assertThrows(OpMatchingException.class, () -> ops.op("math.pow", h).arity2().input(in, exponent).outType(Long.class).apply());

		// Ensure an Op matches with simplification
		var power = ops.op("math.pow").arity2().input(in, exponent).outType(Long.class).apply();
		Assertions.assertEquals((long) Math.pow(in, exponent), power);
	}

	public static void main(String[] args) {
		try {
			testOpEnvironmentObtainable();
			testOpExecutions();
			testOpHelp();
			testOpHints();
		}
		catch (final Throwable t) {
			t.printStackTrace(System.err);
			System.exit(1);
		}
	}


}
