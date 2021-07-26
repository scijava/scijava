package org.scijava.ops.engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.scijava.InstantiableException;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.spi.Op;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;


public class PluginBasedClassOpInfoGenerator implements OpInfoGenerator {

	private final PluginService service;

	public PluginBasedClassOpInfoGenerator(PluginService service) {
		this.service = service;
	}

	@Override
	public List<OpInfo> generateInfos() {
		List<OpInfo> infos = new ArrayList<>();
		for (PluginInfo<Op> info : service.getPluginsOfType(Op.class) ) {
			Class<?> opClass;
			try {
				opClass = info.loadClass();
				String[] parsedOpNames = OpUtils.parseOpNames(info.getName());
				infos.add(new OpClassInfo(opClass, parsedOpNames));
			}
			catch (InstantiableException exc) {
				exc.printStackTrace();
			}
			
		}
		return infos;
	}

}
