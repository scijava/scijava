
package org.scijava.ops.engine.yaml;

import org.scijava.common3.Classes;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.features.YAMLOpInfoCreator;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;

import java.net.URI;
import java.util.Map;

/**
 * A {@link YAMLOpInfoCreator} specialized for Java {@link Class}es.
 *
 * @author Gabriel Selzer
 */
public class JavaClassYAMLOpInfoCreator extends AbstractYAMLOpInfoCreator {

	@Override
	public boolean canCreateFrom(URI identifier) {
		return identifier.getScheme().startsWith("javaClass");
	}

	@Override
	public OpInfo create(final String identifier, final String[] names,
		final double priority, final Hints hints, String version,
		Map<String, Object> yaml) throws Exception
	{
		// parse class
		Class<?> src = Classes.load(identifier);
		// Create the OpInfo
		return new OpClassInfo(src, version, null, priority, names);
	}
}
