
package org.scijava.ops.engine.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.scijava.ops.api.*;
import org.scijava.ops.engine.OpUtils;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.util.ClassUtils;
import org.scijava.util.VersionUtils;

public class OpCollectionInfoGenerator implements OpInfoGenerator {

	private Hints formHints(OpHints h) {
		if (h == null) return new DefaultHints();
		return new DefaultHints(h.hints());
	}

	protected List<OpInfo> processClass(Class<?> cls) {
		String version = VersionUtils.getVersion(cls);
		List<OpInfo> collectionInfos = new ArrayList<>();

		// add OpFieldInfos
		final List<Field> fields = ClassUtils.getAnnotatedFields(cls,
			OpField.class);
		final Optional<Object> instance = getInstance(cls);
		if (instance.isPresent()) {
			final List<OpFieldInfo> fieldInfos = //
				fields.parallelStream() //
					.map(f -> generateFieldInfo(f, instance.get(), version)) //
					.collect(Collectors.toList());
			collectionInfos.addAll(fieldInfos);
		}
		// add OpMethodInfos
		final List<OpMethodInfo> methodInfos = //
			ClassUtils.getAnnotatedMethods(cls, OpMethod.class).parallelStream() //
				.map(m -> generateMethodInfo(m, version)) //
				.collect(Collectors.toList());
		collectionInfos.addAll(methodInfos);
		return collectionInfos;
	}

	private Optional<Object> getInstance(Class<?> c) {
		try {
			return Optional.of(c.getDeclaredConstructor().newInstance());
		}
		catch (Exception exc) {
			return Optional.empty();
		}
	}

	private OpFieldInfo generateFieldInfo(Field field, Object instance,
		String version)
	{
		final boolean isStatic = Modifier.isStatic(field.getModifiers());
		OpField annotation = field.getAnnotation(OpField.class);
		String unparsedOpNames = annotation.names();
		String[] parsedOpNames = OpUtils.parseOpNames(unparsedOpNames);
		double priority = annotation.priority();
		Hints hints = formHints(field.getAnnotation(OpHints.class));
		return new OpFieldInfo(isStatic ? null : instance, field, version, hints,
			priority, parsedOpNames);
	}

	private OpMethodInfo generateMethodInfo(Method method, String version) {
		OpMethod annotation = method.getAnnotation(OpMethod.class);
		Class<?> opType = annotation.type();
		String unparsedOpNames = annotation.names();
		String[] parsedOpNames = OpUtils.parseOpNames(unparsedOpNames);
		Hints hints = formHints(method.getAnnotation(OpHints.class));
		double priority = annotation.priority();
		return new OpMethodInfo(method, opType, version, hints, priority,
			parsedOpNames);
	}

	@Override public boolean canGenerateFrom(Object o) {
		return o instanceof OpCollection;
	}

	@Override public List<OpInfo> generateInfosFrom(Object o) {
		return processClass(o.getClass());
	}
}
