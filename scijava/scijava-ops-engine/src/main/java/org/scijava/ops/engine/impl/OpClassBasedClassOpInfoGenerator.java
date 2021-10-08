
package org.scijava.ops.engine.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpHints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.util.VersionUtils;

public class OpClassBasedClassOpInfoGenerator implements OpInfoGenerator {

	private final List<Discoverer> discoverers;

	public OpClassBasedClassOpInfoGenerator(Discoverer... d) {
		this.discoverers = Arrays.asList(d);
	}

	@Override
	public List<OpInfo> generateInfos() {
		List<OpInfo> infos = discoverers.stream() //
			.flatMap(d -> d.implsOfType(Op.class).stream()) //
			.filter(cls -> cls.getAnnotation(OpClass.class) != null) //
			.map(cls -> {
				OpClass p = cls.getAnnotation(OpClass.class);
				String[] parsedOpNames = OpUtils.parseOpNames(p.names());
				String version = VersionUtils.getVersion(cls);
				Hints hints = formHints(cls.getAnnotation(OpHints.class));
				double priority = p.priority();
				return new OpClassInfo(cls, version, hints, priority, parsedOpNames);
			}) //
			.collect(Collectors.toList());
		return infos;
	}

	private Hints formHints(OpHints h) {
		if (h == null) return new DefaultHints();
		return new DefaultHints(h.hints());
	}

}
