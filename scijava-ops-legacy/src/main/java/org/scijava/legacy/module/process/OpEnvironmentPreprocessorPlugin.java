
package org.scijava.legacy.module.process;

import org.scijava.Priority;
import org.scijava.legacy.service.OpEnvironmentService;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.process.AbstractPreprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * A {@link PreprocessorPlugin} used to inject an {@link OpEnvironment}, created
 * by the {@link OpEnvironmentService}, into {@link Module}s.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.HIGH)
public class OpEnvironmentPreprocessorPlugin extends
	AbstractPreprocessorPlugin
{

	@Parameter
	private OpEnvironmentService opEnvironmentService;

	@Override
	public void process(Module module) {
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			if (!input.isAutoFill()) continue;
			if (module.isInputResolved(input.getName())) continue;
			final Class<?> type = input.getType();
			if (OpEnvironment.class.equals(type)) {
				@SuppressWarnings("unchecked")
				final ModuleItem<OpEnvironment> envInput =
					(ModuleItem<OpEnvironment>) input;
				envInput.setValue(module, opEnvironmentService.env());
				module.resolveInput(input.getName());
			}
		}
	}
}
