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
		OpInfo opifyOpInfo = ops.opify(OpifyOp.class, OpifyOp.class.getName());
		Assertions.assertEquals(OpifyOp.class.getName(), opifyOpInfo.implementationName());
		// assert default priority
		Assertions.assertEquals(Priority.NORMAL, opifyOpInfo.priority(), 0.);
	}

	@Test
	public void testClassOpificationWithPriority() {
		OpInfo opifyOpInfo = ops.opify(OpifyOp.class, Priority.HIGH, OpifyOp.class.getName());
		Assertions.assertEquals(OpifyOp.class.getName(), opifyOpInfo.implementationName());
		// assert default priority
		Assertions.assertEquals(Priority.HIGH, opifyOpInfo.priority(), 0.);
	}

	@Test
	public void testRegister() {
		String opName = "test.opifyOp";
		OpInfo opifyOpInfo = ops.opify(OpifyOp.class, Priority.HIGH, opName);
		ops.register(opifyOpInfo);

		String actual = ops.op(opName).arity0().outType(String.class).create();

		String expected = new OpifyOp().getString();
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testHelpVerbose() {
		// NB We use a new OpEnvironment here for a clean list of Ops.
		OpEnvironment helpEnv = barebonesEnvironment();
		helpEnv.register( //
			helpEnv.opify(OpifyOp.class, Priority.HIGH, "help.verbose1", "help.verbose2") //
		);

		// Test that helpEnv.help() returns just "test"
		String descriptions = helpEnv.helpVerbose();
		String expected = "Namespaces:\n\t> help";
		Assertions.assertEquals(expected, descriptions);

		// Test that helpEnv.help("test") returns both of our namespaces
		descriptions = helpEnv.helpVerbose("help");
		expected = "Names:\n\t> help.verbose1\n\t> help.verbose2";
		Assertions.assertEquals(expected, descriptions);

		// Get the Op matching the description
		descriptions = helpEnv.helpVerbose("help.verbose1");
		expected = "Ops:\n\t> help.verbose1(\n\t\t Inputs:\n\t\t Output:\n\t\t\tjava.lang.String output1\n\t)\n\t";
		Assertions.assertEquals(expected, descriptions);

		// Finally assert a message is thrown when no Ops match
		descriptions = helpEnv.unary("help.verbose1").helpVerbose();
		expected = "No Ops found matching this request.";
		Assertions.assertEquals(expected, descriptions);
	}

	@Test
	public void testInternalNamespaceHelp() {
		// NB We use a new OpEnvironment here for a clean list of Ops.
		OpEnvironment helpEnv = barebonesEnvironment();
		// Register an Op under an "internal" namespace and an "external" namespace
		helpEnv.register( //
				helpEnv.opify(OpifyOp.class, Priority.HIGH, "engine.adapt", "help") //
		);
		// Make sure that only the "external" namespaces are visible
		var actual = helpEnv.help();
		String expected = "Namespaces:\n\t> help";
		Assertions.assertEquals(expected, actual);
		// ...but make sure that if we really need help with the internal namespace, we can get it
		actual = helpEnv.help("engine.adapt");
		expected = "Ops:\n\t> engine.adapt(\n\t\t Inputs:\n\t\t Output:\n\t\t\tString output1\n\t)\n\t";
		Assertions.assertEquals(expected, actual);
	}

}

/**
 * Test class to be opified (and added to the {@link OpEnvironment})
 *
 * TODO: remove @Parameter annotation when it is no longer necessary
 *
 * @author Gabriel Selzer
 */
class OpifyOp implements Producer<String> {

	@Override
	public String create() {
		return getString();
	}

	public String getString() {
		return "This Op tests opify!";
	}

}
