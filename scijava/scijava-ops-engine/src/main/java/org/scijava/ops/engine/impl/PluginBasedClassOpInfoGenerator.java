
package org.scijava.ops.engine.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.scijava.Priority;
import org.scijava.discovery.Discoverer;
import org.scijava.log2.Logger;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpHints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.spi.Op;
import org.scijava.plugin.Plugin;
import org.scijava.util.VersionUtils;

public class PluginBasedClassOpInfoGenerator extends
	DiscoveryBasedOpInfoGenerator
{

	public PluginBasedClassOpInfoGenerator(Logger log, Discoverer... d) {
		super(log, d);
	}

	public PluginBasedClassOpInfoGenerator(Logger log, Collection<Discoverer> d) {
		super(log, d);
	}

	private static double priorityFromAnnotation(Class<?> annotationBearer) {
		final Plugin opAnnotation = annotationBearer.getAnnotation(Plugin.class);
		return opAnnotation == null ? Priority.NORMAL : opAnnotation.priority();
	}

	private Hints formHints(OpHints h) {
		if (h == null) return new DefaultHints();
		return new DefaultHints(h.hints());
	}

	@Override
	protected Class<?> implClass() {
		return Op.class;
	}

	@Override
	protected List<OpInfo> processClass(Class<?> c) {
		Plugin p = c.getAnnotation(Plugin.class);
		if (p == null) return Collections.emptyList();
		String[] parsedOpNames = OpUtils.parseOpNames(p.name());
		String version = VersionUtils.getVersion(c);
		Hints hints = formHints(c.getAnnotation(OpHints.class));
		double priority = priorityFromAnnotation(c);
		return Collections.singletonList(new OpClassInfo(c, version, hints,
			priority, parsedOpNames));
	}
}
