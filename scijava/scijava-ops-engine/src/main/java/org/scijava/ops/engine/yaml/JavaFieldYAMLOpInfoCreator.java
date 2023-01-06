
package org.scijava.ops.engine.yaml;

import java.lang.reflect.Field;
import java.util.Map;

import org.scijava.common3.Classes;
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
		if (!source.equals("Java") || identifier.indexOf('$') == -1) {
			return false;
		}

		// If there is a '$' we have to try to load this thing to see if
		// it's a field or a class. If loading fails, it's a field
		return Classes.load(identifier, true) == null;
	}

	@Override
	OpInfo create(String identifier, String[] names, double priority, Hints hints,
		String version, Map<String, Object> yaml) throws Exception
	{
		// parse class
		int clsIndex = identifier.indexOf('$');
		String clsString = identifier.substring(0, clsIndex);
		Class<?> cls = Classes.load(clsString);
		Object instance = cls.getConstructor().newInstance();
		// parse Field
		String fieldString = identifier.substring(clsIndex + 1);
		Field field = cls.getDeclaredField(fieldString);
		// Create the OpInfo
		return new OpFieldInfo(instance, field, version, null, priority, names);
	}
}
