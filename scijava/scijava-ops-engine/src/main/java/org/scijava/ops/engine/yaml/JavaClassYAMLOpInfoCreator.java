
package org.scijava.ops.engine.yaml;

import org.scijava.common3.Classes;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.features.YAMLOpInfoCreator;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;

import java.util.Map;

/**
 * A {@link YAMLOpInfoCreator} specialized for Java {@link Class}es.
 *
 * @author Gabriel Selzer
 */
public class JavaClassYAMLOpInfoCreator extends AbstractYAMLOpInfoCreator {

	@Override
	public boolean canCreateFrom(String source, String identifier) {
		// Ensure Java source
		if (!source.equals("Java")) {
			return false;
		}
		// Ensure not a Method
		if (identifier.indexOf('(') != -1) {
			return false;
		}
		if (identifier.indexOf('$') == -1) {
			return true;
		}
		// If there is a '$' we have to try to load this thing to see if
		// it's a field or a class. If loading works, it's a class
		return Classes.load(identifier, true) != null;
	}

	@Override
	public OpInfo create(final String identifier, final String[] names,
		final double priority, final Hints hints, String version,
		Map<String, Object> yaml) throws Exception
	{
		// parse class
		String cls = identifier;
		if (identifier.indexOf('%') != -1) {
			cls = cls.replace('%', '$');
		}
		Class<?> src = Classes.load(cls);
		// Create the OpInfo
		return new OpClassInfo(src, version, null, priority, names);
	}
}
