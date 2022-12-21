
package org.scijava.ops.engine.yaml;

import java.util.Map;

import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;

public class YAMLJavaClassInfoCreator implements YAMLOpInfoCreator {

	@Override
	public boolean canCreateFrom(String source, String identifier) {
		return source.equals("Java") //
		 && identifier.indexOf('$') == -1 //
		 && identifier.indexOf('(') == -1;
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
		String srcString = (String) yaml.get("source");
		Class<?> src =
				Thread.currentThread().getContextClassLoader().loadClass(srcString);
		return new OpClassInfo(src, version, null, priority, names);
	}
}
