
package org.scijava.ops.engine.yaml.impl;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;

import org.scijava.common3.Classes;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.yaml.AbstractYAMLOpInfoCreator;
import org.scijava.ops.engine.yaml.YAMLOpInfoCreator;

/**
 * A {@link YAMLOpInfoCreator} specialized for Java {@link Field}s.
 *
 * @author Gabriel Selzer
 */
public class JavaFieldYAMLOpInfoCreator extends AbstractYAMLOpInfoCreator {

	@Override
	public boolean canCreateFrom(URI identifier) {
		return identifier.getScheme().startsWith("javaField");
	}

	@Override
	protected OpInfo create(String identifier, String[] names, double priority, String version, Map<String, Object> yaml) throws Exception
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
		return new OpFieldInfo(instance, field, version, new Hints(), priority, names);
	}
}
