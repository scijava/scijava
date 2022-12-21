
package org.scijava.ops.engine.yaml;

import java.lang.reflect.Method;
import java.util.Map;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;

public class YAMLJavaMethodInfoCreator implements YAMLOpInfoCreator {

	@Override
	public boolean canCreateFrom(String source, String identifier) {
		return source.equals("Java") && identifier.indexOf('(') != -1;
	}

	@Override
	public OpInfo create(final Map<String, Object> yaml, final String version) throws Exception {
		final String[] names;
		if (yaml.containsKey("name")) {
			names = new String[] { (String) yaml.get("name") };
		}
		else {
			String namesString = (String) yaml.get("names");
			names = namesString.split("\\s*,\\s*");
		}

		double priority = 0.0;
		if (yaml.containsKey("priority")) {
			Object p = yaml.get("priority");
			if (p instanceof Number) priority = ((Number) p).doubleValue();
			else if (p instanceof String) {
				priority = Double.parseDouble((String) p);
			}
			else {
				throw new IllegalArgumentException("Op priority not parsable");
			}
		}
		// parse class
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String srcString = (String) yaml.get("source");
		int clsIndex  = srcString.lastIndexOf('.', srcString.indexOf('('));
		String clsString = srcString.substring(0, clsIndex);
		String methodString = srcString.substring(clsIndex+1, srcString.indexOf('('));
		String[] paramStrings = srcString.substring(srcString.indexOf('(') + 1, srcString.indexOf(')')).split("\\s*,\\s*");
		Class<?>[] paramClasses = new Class<?>[paramStrings.length];
		for(int i = 0; i < paramStrings.length; i++) {
			paramClasses[i] = cl.loadClass(paramStrings[i]);
		}
		Class<?> src = cl.loadClass(clsString);
		Method method = src.getMethod(methodString, paramClasses);
		String typeString = (String) yaml.get("type");
		Class<?> opType = cl.loadClass(typeString);
		return new OpMethodInfo(method, opType, null, priority, names);
	}
}
