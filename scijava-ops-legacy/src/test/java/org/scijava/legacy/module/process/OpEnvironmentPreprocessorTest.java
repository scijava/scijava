package org.scijava.legacy.module.process;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.legacy.service.OpService;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Parameter;

import java.util.concurrent.ExecutionException;

public class OpEnvironmentPreprocessorTest {

	@Test
	public void testOpEnvironmentPreprocessor()
		throws ExecutionException, InterruptedException
	{
		final Context context = new Context(CommandService.class, OpService.class);
		final CommandService commandService = context.service(CommandService.class);
		var command = commandService.run(CommandWithOpEnvironment.class, true).get();
		Assertions.assertDoesNotThrow(command::run);
	}

	public static class CommandWithOpEnvironment implements Command {

		@Parameter
		public OpEnvironment env;

		@Override
		public void run() {
			if (env == null) {
				throw new IllegalArgumentException("OpEnvironment not properly injected!");
			}
		}
	}

}
