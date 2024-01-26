package org.scijava.legacy.service;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

@Plugin(type= Service.class)
public class DefaultOpEnvironmentService extends AbstractService implements
	OpEnvironmentService
{

	@Parameter(required=false)
	private ScriptService scriptService;

	private OpEnvironment env;

	@Override
	public void initialize() {
 		env = OpEnvironment.build();

		if (scriptService != null) {
			scriptService.addAlias(OpEnvironment.class);
		}
	}

	@Override
	public OpEnvironment env() {
		return env;
	}
}
