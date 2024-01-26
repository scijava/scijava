package org.scijava.legacy.module.process;

import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.legacy.service.OpEnvironmentService;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Parameter;

import java.util.concurrent.ExecutionException;

public class OpEnvironmentPreprocessorTest {

	@Test
	public void testOpEnvironmentPreprocessor()
		throws ExecutionException, InterruptedException
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
			if (env == null) {
				throw new IllegalArgumentException(
					"OpEnvironment not properly injected!");
			}
		}
	}

}
