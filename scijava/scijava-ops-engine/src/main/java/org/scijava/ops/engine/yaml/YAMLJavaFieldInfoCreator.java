
package org.scijava.ops.engine.yaml;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;

public class YAMLJavaFieldInfoCreator implements YAMLOpInfoCreator {

	@Override
	public boolean canCreateFrom(String source, String identifier) {
		return source.equals("Java") && identifier.indexOf('$') != -1;
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
		String clsString = srcString.substring(0, srcString.indexOf('$'));
		Class<?> cls = cl.loadClass(clsString);
		Object instance = cls.getConstructor().newInstance();
		String fieldString = srcString.substring(srcString.indexOf('$') + 1);
		Field field = cls.getDeclaredField(fieldString);
		return new OpFieldInfo(instance, field, version, null, priority, names);
	}
}
