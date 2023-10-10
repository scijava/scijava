
package org.scijava.ops.engine.impl;

import java.util.Collections;
import java.util.List;

import org.scijava.common3.validity.ValidityException;
import org.scijava.meta.Versions;
import org.scijava.ops.api.Hints;
import org.scijava.ops.spi.OpHints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpInfoGenerator;
import org.scijava.ops.engine.OpUtils;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;

public class OpClassOpInfoGenerator implements OpInfoGenerator
{

	private Hints formHints(OpHints h) {
		if (h == null) return new Hints();
		return new Hints(h.hints());
	}

	protected List<OpInfo> processClass(Class<?> c) {
		OpClass p = c.getAnnotation(OpClass.class);
		if (p == null) return Collections.emptyList();

		String[] parsedOpNames = OpUtils.parseOpNames(p.names());
		String version = Versions.getVersion(c);
		Hints hints = formHints(c.getAnnotation(OpHints.class));
		double priority = p.priority();
		try {
			return Collections.singletonList(
					new OpClassInfo(c, version, hints, priority, parsedOpNames));
		} catch (ValidityException e) {
			// TODO: Log exception
			return Collections.emptyList();
		}
	}

	@Override public boolean canGenerateFrom(Object o) {
		boolean isOp = o instanceof Op;
		boolean isOpClass = o.getClass().isAnnotationPresent(OpClass.class);
		return isOp && isOpClass;
	}

	@Override public List<OpInfo> generateInfosFrom(Object o) {
		return processClass(o.getClass());
	}
}
