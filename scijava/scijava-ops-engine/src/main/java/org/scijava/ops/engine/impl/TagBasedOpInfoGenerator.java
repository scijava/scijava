
package org.scijava.ops.engine.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.Discovery;
import org.scijava.log.LogService;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.util.VersionUtils;

/**
 * Generates {@link OpInfo}s using a {@link Discovery}. The tag syntax is
 * expected to be as follows:
 * <p>
 * {@code @implNote op names=<op names, comma delimited> [priority=<op priority>]}
 * <p>
 * For example, an op wishing to be discoverable with names "foo.bar" and
 * "foo.baz", with a priority of 100, should be declared as
 * <p>
 * {@code @implNote op names=foo.bar,foo.baz priority=100}
 * <p>
 * 
 * @author
 */
public class TagBasedOpInfoGenerator implements OpInfoGenerator {

	private static final String TAGTYPE = "op";

	private final LogService log;
	private final List<Discoverer> discoverers;

	public TagBasedOpInfoGenerator(final LogService log, Discoverer... d) {
		this.log = log;
		this.discoverers = Arrays.asList(d);
	}

	private OpInfo opClassGenerator(Class<?> cls, double priority,
		String[] names)
	{
		String version = VersionUtils.getVersion(cls);
		return new OpClassInfo(cls, version, new DefaultHints(), priority, names);
	}

	private OpInfo opMethodGenerator(Method m, String opType, double priority,
		String[] names)
	{
		Class<?> cls;
		try {
			cls = Context.getClassLoader().loadClass(opType);
		}
		catch (ClassNotFoundException exc) {
			log.warn("Skipping method " + m + ": Cannot load Class" + opType);
			return null;
		}
		String version = VersionUtils.getVersion(m.getDeclaringClass());
		return new OpMethodInfo(m, cls, version, new DefaultHints(), priority,
			names);
	}

	private OpInfo opFieldGenerator(Field f, double priority, String[] names) {
		String version = VersionUtils.getVersion(f.getDeclaringClass());
		Object instance;
		try {
			instance = f.getDeclaringClass().getDeclaredConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException exc)
		{
			return null;
		}
		return new OpFieldInfo(instance, f, version, new DefaultHints(), priority,
			names);
	}

	@Override
	public List<OpInfo> generateInfos() {
		try {
		List<OpInfo> infos = discoverers.stream() //
			.flatMap(d -> d.elementsTaggedWith(TAGTYPE).stream()) //
			.map(discovery -> {
				// Obtain op metadata
				String[] names;
				String opType;
				double priority;

				try {
					names = getOpNames(discovery);
					opType = getOpType(discovery);
					priority = getOpPriority(discovery);
				}
				catch (IllegalArgumentException e) {
					log.warn("Skipping Op Discovery " + discovery + ": " + e
						.getMessage());
					return null;
				}

				// Delegate to proper constructor
				AnnotatedElement e = discovery.discovery();
				if (e instanceof Class) {
					return opClassGenerator((Class<?>) e, priority, names);
				}
				else if (e instanceof Method) {
					return opMethodGenerator((Method) e, opType, priority, names);
				}
				else if (e instanceof Field) {
					return opFieldGenerator((Field) e, priority, names);
				}
				else return null;
			}) //
			.filter(Objects::nonNull) //
			.collect(Collectors.toList());
		return infos;
		} catch(NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String[] getOpNames(Discovery<AnnotatedElement> d) {
		String names = d.option("names");
		String name = d.option("name");
		if (names.isEmpty() && name.isEmpty()) {
			throw new IllegalArgumentException("Op discovery " + d + " does not record any names!");
		}
		if (!names.isEmpty()) {
			return OpUtils.parseOpNames(names);
		}
		return OpUtils.parseOpNames(name);
	}

	private static double getOpPriority(Discovery<AnnotatedElement> d) {
		String priority = d.option("priority");
		return priority.isEmpty() ? Priority.NORMAL : Double.parseDouble(priority);
	}

	private static String getOpType(Discovery<AnnotatedElement> d) {
		return d.option("type");
	}

}
