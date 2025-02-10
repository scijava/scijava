/*-
 * #%L
 * Interoperability with legacy SciJava libraries.
 * %%
 * Copyright (C) 2023 - 2025 SciJava developers.
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

package org.scijava.legacy.module.process;

import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.legacy.service.OpEnvironmentService;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Parameter;

import java.util.concurrent.ExecutionException;

/**
 * Tests the ability of {@link OpEnvironmentPreprocessorPlugin} to fill in
 * {@link OpEnvironment} module parameters.
 *
 * @author Gabriel Selzer
 */
public class OpEnvironmentPreprocessorTest {

	@Test
	public void testOpEnvironmentPreprocessor() throws ExecutionException,
		InterruptedException
	{
		var context = new Context(CommandService.class, OpEnvironmentService.class);
		var commandService = context.service(CommandService.class);
		commandService.run(CommandWithOpEnvironment.class, true).get();
		context.dispose();
	}

	public static class CommandWithOpEnvironment implements Command {

		@Parameter
		public OpEnvironment env;

		@Override
		public void run() {
			// Fails the test if the OpEnvironment was not injected
			if (env == null) {
				throw new IllegalArgumentException(
					"OpEnvironment not properly injected!");
			}
		}
	}

}
