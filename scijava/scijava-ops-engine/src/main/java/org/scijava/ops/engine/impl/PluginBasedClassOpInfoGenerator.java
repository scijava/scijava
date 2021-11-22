
package org.scijava.ops.engine.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.scijava.Priority;
import org.scijava.discovery.Discoverer;
import org.scijava.log2.Logger;
import org.scijava.ops.api.*;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.spi.Op;
import org.scijava.plugin.Plugin;
import org.scijava.util.VersionUtils;

public class PluginBasedClassOpInfoGenerator implements OpInfoGenerator {
	private static double priorityFromAnnotation(Class<?> annotationBearer) {
		final Plugin opAnnotation = annotationBearer.getAnnotation(Plugin.class);
		return opAnnotation == null ? Priority.NORMAL : opAnnotation.priority();
	}

	private Hints formHints(OpHints h) {
		if (h == null)
			return new DefaultHints();
		return new DefaultHints(h.hints());
	}

	protected List<OpInfo> processClass(Class<?> c) {
		Plugin p = c.getAnnotation(Plugin.class);
		if (p == null)
			return Collections.emptyList();
		String[] parsedOpNames = OpUtils.parseOpNames(p.name());
		String version = VersionUtils.getVersion(c);
		Hints hints = formHints(c.getAnnotation(OpHints.class));
		double priority = priorityFromAnnotation(c);
		return Collections.singletonList(new OpClassInfo(c, version, hints, priority, parsedOpNames));
	}

	@Override
	public boolean canGenerateFrom(Object o) {
		if (!o.getClass().isAnnotationPresent(Plugin.class))
			return false;
		return o.getClass().getAnnotation(Plugin.class).type() == Op.class;
	}

	@Override
	public List<OpInfo> generateInfosFrom(Object o) {
		return processClass(o.getClass());
	}
}
