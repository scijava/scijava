package org.scijava.ops.engine.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.scijava.discovery.therapi.TaggedElement;
import org.scijava.discovery.therapi.TherapiDiscoveryUtils;
import org.scijava.meta.Versions;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.engine.OpUtils;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.parse2.Parser;

public class TherapiOpInfoGenerator implements OpInfoGenerator {

	private final Parser parser;

	public TherapiOpInfoGenerator() {
		Optional<Parser> optional = ServiceLoader.load(Parser.class).findFirst();
		if (optional.isEmpty())
			throw new IllegalStateException("No Parser available through ServiceLoader!");
		this.parser = optional.get();
	}

	@Override
	public boolean canGenerateFrom(Object o) {
		return TherapiDiscoveryUtils.hasJavadoc(o.getClass());
	}

	@Override
	public List<OpInfo> generateInfosFrom(Object o) {
		List<TaggedElement> tags = TherapiDiscoveryUtils.taggedElementsFrom(o.getClass(), "op", parser);
		return tags.parallelStream() //
				.map(TherapiOpInfoGenerator::infoFrom) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.collect(Collectors.toList());
	}

	public static Optional<OpInfo> infoFrom(TaggedElement t) {
		// get the name of the op - could be under 'name' or 'names'
		String[] names = getNames(t);
		if (names == null)
			return Optional.empty();

		// get the priority of the op
		double priority = getPriority(t);

		// Delegate to proper constructor
		AnnotatedElement e = t.discovery();
		if (e instanceof Class) {
			return Optional.of(opClassGenerator((Class<?>) e, priority, names));
		} else if (e instanceof Method) {
			String opType = t.option("type");
			return Optional.ofNullable(opMethodGenerator((Method) e, opType, priority, names));
		} else if (e instanceof Field) {
			return Optional.ofNullable(opFieldGenerator((Field) e, priority, names));
		}
		return Optional.empty();
	}

	private static double getPriority(TaggedElement t) {
		String p = t.option("priority");
		return p.isEmpty() ? 0. : Double.parseDouble(p);
	}

	private static String[] getNames(TaggedElement t) {
		String name = t.option("name");
		if (name.isEmpty())
			name = t.option("names");
		if (name.isEmpty())
			return null;
		return OpUtils.parseOpNames(name);
	}

	private static OpInfo opClassGenerator(Class<?> cls, double priority, String[] names) {
		String version = Versions.getVersion(cls);
		return new OpClassInfo(cls, version, new DefaultHints(), priority, names);
	}

	private static OpInfo opMethodGenerator(Method m, String opType, double priority, String[] names) {
		Class<?> cls;
		try {
			cls = TherapiOpInfoGenerator.class.getClassLoader().loadClass(opType);
		} catch (ClassNotFoundException exc) {
			return null;
		}
		String version = Versions.getVersion(m.getDeclaringClass());
		return new OpMethodInfo(m, cls, version, new DefaultHints(), priority, names);
	}

	private static OpInfo opFieldGenerator(Field f, double priority, String[] names) {
		String version = Versions.getVersion(f.getDeclaringClass());
		Object instance;
		try {
			instance = f.getDeclaringClass().getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException exc) {
			return null;
		}
		return new OpFieldInfo(instance, f, version, new DefaultHints(), priority, names);
	}
}
