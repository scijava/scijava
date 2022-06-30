
package org.scijava.ops.engine.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.scijava.discovery.Discoverer;
import org.scijava.log2.Logger;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpHints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.util.VersionUtils;

public class OpClassBasedClassOpInfoGenerator extends
	DiscoveryBasedOpInfoGenerator
{

	public OpClassBasedClassOpInfoGenerator(Logger log, Discoverer... d) {
		super(log, d);
	}

	public OpClassBasedClassOpInfoGenerator(Logger log,
		Collection<Discoverer> d)
	{
		super(log, d);
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
		OpClass p = c.getAnnotation(OpClass.class);
		if (p == null) return Collections.emptyList();

		String[] parsedOpNames = OpUtils.parseOpNames(p.names());
		String version = VersionUtils.getVersion(c);
		Hints hints = formHints(c.getAnnotation(OpHints.class));
		double priority = p.priority();
		return Collections.singletonList(new OpClassInfo(c, version, hints,
			priority, parsedOpNames));
	}

}
