package org.scijava.legacy.service;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

@Plugin(type= Service.class)
public class DefaultOpService extends AbstractService implements OpService{

	private final OpEnvironment env = OpEnvironment.build();

	@Override
	public OpEnvironment env() {
		return env;
	}
}
