
package org.scijava.legacy.service;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default implementation of {@link OpEnvironmentService}
 *
 * @author Gabriel Selzer
 */
@Plugin(type = Service.class, attrs = { @Attr(name = "noAlias") })
public class DefaultOpEnvironmentService extends AbstractService implements
	OpEnvironmentService
{

	@Parameter(required = false)
	private ScriptService scriptService;

	private OpEnvironment env;

	@Override
	public void initialize() {
		// Build the Op Environment
		env = OpEnvironment.build();

		// Set up alias, if ScriptService available
		if (scriptService != null) {
			scriptService.addAlias(OpEnvironment.class);
		}
	}

	@Override
	public OpEnvironment env() {
		return env;
	}
}
