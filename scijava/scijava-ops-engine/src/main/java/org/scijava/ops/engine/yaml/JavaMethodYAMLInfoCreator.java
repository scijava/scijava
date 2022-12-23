
package org.scijava.ops.engine.yaml;

import java.lang.reflect.Method;
import java.util.Map;

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
	public boolean canCreateFrom(String source, String identifier) {
		return source.equals("Java") && identifier.indexOf('(') != -1;
	}

	@Override
	OpInfo create(String identifier, String[] names, double priority, Hints hints,
		String version, Map<String, Object> yaml) throws Exception
	{
		// parse class
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		int clsIndex = identifier.lastIndexOf('.', identifier.indexOf('('));
		String clsString = identifier.substring(0, clsIndex);
		Class<?> src = cl.loadClass(clsString);
		// parse method
		String methodString = identifier.substring(clsIndex + 1, identifier.indexOf(
			'('));
		String[] paramStrings = identifier.substring(identifier.indexOf('(') + 1,
			identifier.indexOf(')')).split("\\s*,\\s*");
		Class<?>[] paramClasses = new Class<?>[paramStrings.length];
		for (int i = 0; i < paramStrings.length; i++) {
			paramClasses[i] = cl.loadClass(paramStrings[i]);
		}
		Method method = src.getMethod(methodString, paramClasses);
		// parse op type
		String typeString = (String) yaml.get("type");
		Class<?> opType = cl.loadClass(typeString);
		// create the OpInfo
		return new OpMethodInfo(method, opType, null, priority, names);
	}
}
