package org.scijava.ops.engine.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.util.ClassUtils;
import org.scijava.util.VersionUtils;


public class OpCollectionInfoGenerator implements OpInfoGenerator {

	private final List<Discoverer> discoverers;

	public OpCollectionInfoGenerator(Discoverer... d) {
		this.discoverers = Arrays.asList(d);
	}

	@Override
	public List<OpInfo> generateInfos() {
		List<OpInfo> infos = discoverers.stream() //
			.flatMap(d -> d.implsOfType(OpCollection.class).stream()) //
			.map(cls -> {
			try {
				String version = VersionUtils.getVersion(cls);
				List<OpInfo> collectionInfos = new ArrayList<>();
				final List<Field> fields = ClassUtils.getAnnotatedFields(cls, OpField.class);
				Object instance = null;
				for (Field field : fields) {
					final boolean isStatic = Modifier.isStatic(field.getModifiers());
					if (!isStatic && instance == null) {
						instance = field.getDeclaringClass().newInstance();
					}
					OpField annotation = field.getAnnotation(OpField.class);
					String unparsedOpNames = annotation.names();
					String[] parsedOpNames = OpUtils.parseOpNames(unparsedOpNames);
					double priority = annotation.priority();
					Hints hints = formHints(field.getAnnotation(OpHints.class));
					collectionInfos.add(new OpFieldInfo(isStatic ? null : instance, field, version, hints, priority,
						parsedOpNames));
				}
				final List<Method> methods = ClassUtils.getAnnotatedMethods(cls, OpMethod.class);
				for (final Method method: methods) {
					OpMethod annotation = method.getAnnotation(OpMethod.class);
					Class<?> opType = annotation.type();
					String unparsedOpNames = annotation.names();
					String[] parsedOpNames = OpUtils.parseOpNames(unparsedOpNames);
					Hints hints = formHints(method.getAnnotation(OpHints.class));
					double priority = annotation.priority();
					collectionInfos.add(new OpMethodInfo(method, opType, version, hints, priority, parsedOpNames));
				}
				return collectionInfos;
			} catch (InstantiationException | IllegalAccessException exc) {
				// TODO: Consider how best to handle this.
				return null;
			}
			
			}) //
			.filter(list -> list!= null) //
			.flatMap(list -> list.stream()) //
			.collect(Collectors.toList());
		return infos;
	}

	private Hints formHints(OpHints h) {
		if (h == null) return new DefaultHints();
		return new DefaultHints(h.hints());
	}


}
