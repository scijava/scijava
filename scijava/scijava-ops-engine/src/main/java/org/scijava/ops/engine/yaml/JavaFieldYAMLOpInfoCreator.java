
package org.scijava.ops.engine.yaml;

import java.lang.reflect.Field;
import java.util.Map;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.features.YAMLOpInfoCreator;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;

/**
 * A {@link YAMLOpInfoCreator} specialized for Java {@link Field}s.
 *
 * @author Gabriel Selzer
 */
public class JavaFieldYAMLOpInfoCreator extends AbstractYAMLOpInfoCreator {

	@Override
	public boolean canCreateFrom(String source, String identifier) {
		return source.equals("Java") && identifier.indexOf('$') != -1;
	}

	@Override
	OpInfo create(String identifier, String[] names, double priority, Hints hints,
		String version, Map<String, Object> yaml) throws Exception
	{
		// parse class
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		int clsIndex = identifier.indexOf('$');
		String clsString = identifier.substring(0, clsIndex);
		Class<?> cls = cl.loadClass(clsString);
		Object instance = cls.getConstructor().newInstance();
		// parse Field
		String fieldString = identifier.substring(clsIndex + 1);
		Field field = cls.getDeclaredField(fieldString);
		// Create the OpInfo
		return new OpFieldInfo(instance, field, version, null, priority, names);
	}
}
