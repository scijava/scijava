/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

package org.scijava.ops.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.function.Producer;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.priority.Priority;

/**
 * Test class for {@link OpEnvironment} methods. NB this class does not test any
 * <em>particular</em> implementation of {@link OpEnvironment}, but instead
 * ensures expected behavior in the {@link OpEnvironment}
 *
 * @author Gabriel Selzer
 */
public class OpEnvironmentTest extends AbstractTestEnvironment {

	@Test
	public void testClassOpification() {
		OpInfo opifyOpInfo = ops.opify(OpifyOp1.class, OpifyOp1.class.getName());
		Assertions.assertEquals(OpifyOp1.class.getName(), opifyOpInfo
			.implementationName());
		// assert default priority
		Assertions.assertEquals(Priority.NORMAL, opifyOpInfo.priority(), 0.);
	}

	@Test
	public void testClassOpificationWithPriority() {
		OpInfo opifyOpInfo = ops.opify(OpifyOp1.class, Priority.HIGH, OpifyOp1.class
			.getName());
		Assertions.assertEquals(OpifyOp1.class.getName(), opifyOpInfo
			.implementationName());
		// assert default priority
		Assertions.assertEquals(Priority.HIGH, opifyOpInfo.priority(), 0.);
	}

	@Test
	public void testRegister() {
		String opName = "test.opifyOp";
		OpInfo opifyOpInfo = ops.opify(OpifyOp1.class, Priority.HIGH, opName);
		ops.register(opifyOpInfo);

		String actual = ops.op(opName).outType(String.class).create();

		String expected = new OpifyOp1().getString();
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testHelpVerboseNoNS() {
		OpEnvironment helpEnv = makeHelpEnv("help.verbose1", "help.verbose2");

		// Test that our namespace is found
		String descriptions = helpEnv.helpVerbose();
		String expected = "Namespaces:\n\t> help";
		Assertions.assertEquals(expected, descriptions);
	}

	@Test
	public void testHelpVerboseInvalid() {
		OpEnvironment helpEnv = makeHelpEnv("help.verbose1", "help.verbose2");

		// Test that our namespace is found
		String descriptions = helpEnv.helpVerbose("ichneumon");
		String expected = "Not a valid Op name or namespace:\n\t> ichneumon";
		Assertions.assertEquals(expected, descriptions);
	}

	@Test
	public void testHelpVerboseNS() {
		OpEnvironment helpEnv = makeHelpEnv("help.verbose1", "help.verbose2");

		// Test that both of our ops are found in the namespace
		String descriptions = helpEnv.helpVerbose("help");
		String expected = "Names:\n\t> help.verbose1\n\t> help.verbose2";
		Assertions.assertEquals(expected, descriptions);
	}

	@Test
	public void testHelpVerboseOp() {
		OpEnvironment helpEnv = makeHelpEnv("help.verbose1", "help.verbose2");

		// Get the Op matching the description
		String descriptions = helpEnv.helpVerbose("help.verbose1");
		String expected =
			"help.verbose1:\n\t- org.scijava.ops.engine.OpifyOp1\n\t\tReturns : java.lang.String";
		Assertions.assertEquals(expected, descriptions);
	}

	@Test
	public void testHelpVerboseNotFound() {
		OpEnvironment helpEnv = makeHelpEnv("help.verbose1", "help.verbose2");
		// Finally assert a message is thrown when no Ops match
		String descriptions = helpEnv.op("help.verbose1").input(null).helpVerbose();
		String expected = OpDescriptionGenerator.NO_OP_MATCHES;
		Assertions.assertEquals(expected, descriptions);
	}

	@Test
	public void testInternalNamespaceHelp() {
		// Make sure that only the "external" namespaces are visible
		OpEnvironment helpEnv = makeHelpEnv("engine.adapt", "help.verbose1");
		var actual = helpEnv.help();
		String expected = "Namespaces:\n\t> help";
		Assertions.assertEquals(expected, actual);
		// ...but make sure that if we really need help with the internal namespace,
		// we can get it
		actual = helpEnv.help("engine.adapt");
		expected = "engine.adapt:\n\t- () -> String";
		Assertions.assertEquals(expected, actual);
	}

	private OpEnvironment makeHelpEnv(String n1, String n2) {
		// NB We use a new OpEnvironment here for a clean list of Ops.
		OpEnvironment helpEnv = barebonesEnvironment();
		// Register an Op under an "internal" namespace and an "external" namespace
		helpEnv.register( //
			helpEnv.opify(OpifyOp1.class, Priority.HIGH, n1) //
		);
		helpEnv.register( //
			helpEnv.opify(OpifyOp2.class, Priority.HIGH, n2) //
		);
		return helpEnv;
	}

}

// -- Test classes to be registered --

class OpifyOp1 implements Producer<String> {

	@Override
	public String create() {
		return getString();
	}

	public String getString() {
		return "This Op tests opify!";
	}

}

class OpifyOp2 implements Producer<String> {

	@Override
	public String create() {
		return getString();
	}

	public String getString() {
		return "This Op tests opify!";
	}
}
