package org.scijava.ops.python;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.features.YAMLOpInfoCreator;

public class PythonYAMLOpInfoCreator implements YAMLOpInfoCreator {

	@Override public boolean canCreateFrom(URI identifier) {
		return identifier.getScheme().equals("pythonFunction");
	}

	@Override public OpInfo create(URI identifier, Map<String, Object> yaml)
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
		// Parse path - start after the leading slash
		final String path = identifier.getPath().substring(1);
		// Parse source
		final String srcString = path.substring(0, path.indexOf('/'));
		// Parse version
		final String version = path.substring(path.indexOf('/') + 1);

		List<Map<String, Object>> params =
				(List<Map<String, Object>>) yaml.get("parameters");

		return new PythonOpInfo(Arrays.asList(names), opType, priority, version, srcString, params);
	}
}
