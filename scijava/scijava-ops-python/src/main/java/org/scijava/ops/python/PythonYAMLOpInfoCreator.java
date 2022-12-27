package org.scijava.ops.python;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.features.YAMLOpInfoCreator;

public class PythonYAMLOpInfoCreator implements YAMLOpInfoCreator {

	@Override public boolean canCreateFrom(String source, String identifier) {
		return source.toLowerCase().trim().equals("python");
	}

	@Override public OpInfo create(Map<String, Object> yaml, String version)
			throws Exception
	{
		final String[] names;
		if (yaml.containsKey("name")) {
			names = new String[]{(String) yaml.get("name")};
		} else {
			String namesString = (String) yaml.get("names");
			names = namesString.split("\\s*,\\s*");
		}
		// parse op type
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String typeString = (String) yaml.get("type");
		Class<?> opType = cl.loadClass(typeString);

		double priority = (double) yaml.get("priority");
		String source = (String) yaml.get("source");

		List<Map<String, Object>> params =
				(List<Map<String, Object>>) yaml.get("parameters");

		return new PythonOpInfo(Arrays.asList(names), opType, priority, version, source, params);
	}
}
