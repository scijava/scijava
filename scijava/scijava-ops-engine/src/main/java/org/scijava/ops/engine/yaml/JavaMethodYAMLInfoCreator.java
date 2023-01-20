
package org.scijava.ops.engine.yaml;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import org.scijava.common3.Classes;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.features.YAMLOpInfoCreator;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;

/**
 * A {@link YAMLOpInfoCreator} specialized for Java {@link Method}s.
 *
 * @author Gabriel Selzer
 */
public class JavaMethodYAMLInfoCreator extends AbstractYAMLOpInfoCreator {

	@Override
	public boolean canCreateFrom(URI identifier) {
		return identifier.getScheme().startsWith("javaMethod");
	}

	@Override
	OpInfo create(String identifier, String[] names, double priority, Hints hints,
		String version, Map<String, Object> yaml) throws Exception
	{
		// parse class
		int clsIndex = identifier.lastIndexOf('.', identifier.indexOf('('));
		String clsString = identifier.substring(0, clsIndex);
		Class<?> src = Classes.load(clsString);
		// parse method
		String methodString = identifier.substring(clsIndex + 1, identifier.indexOf(
			'('));
		String[] paramStrings = identifier.substring(identifier.indexOf('(') + 1,
			identifier.indexOf(')')).split("\\s*,\\s*");
		Class<?>[] paramClasses = new Class<?>[paramStrings.length];
		for (int i = 0; i < paramStrings.length; i++) {
			paramClasses[i] = Classes.load(paramStrings[i]);
		}
		Method method = src.getMethod(methodString, paramClasses);
		// parse op type
		String typeString = (String) yaml.get("type");
		Class<?> opType = Classes.load(typeString);
		// create the OpInfo
		return new OpMethodInfo(method, opType, null, priority, names);
	}
}
