
package org.scijava.ops.engine.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ServiceLoader;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.discovery.therapi.TherapiDiscoverer;
import org.scijava.discovery.therapi.TaggedElement;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.parse2.Parser;
import org.scijava.util.VersionUtils;

/**
 * Generates {@link OpInfo}s using a {@link TaggedElement}. The tag syntax is
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
 * @author Gabriel Selzer
 */
public class TagBasedOpInfoDiscoverer extends TherapiDiscoverer {

	private static final String TAGTYPE = "op";

	public TagBasedOpInfoDiscoverer() {
		super(ServiceLoader.load(Parser.class).findFirst().get());
	}

	private OpInfo opClassGenerator(Class<?> cls, double priority, String[] names) {
		String version = VersionUtils.getVersion(cls);
		return new OpClassInfo(cls, version, new DefaultHints(), priority, names);
	}

	private OpInfo opMethodGenerator(Method m, String opType, double priority, String[] names) {
		Class<?> cls;
		try {
			cls = Context.getClassLoader().loadClass(opType);
		} catch (ClassNotFoundException exc) {
			return null;
		}
		String version = VersionUtils.getVersion(m.getDeclaringClass());
		return new OpMethodInfo(m, cls, version, new DefaultHints(), priority, names);
	}

	private OpInfo opFieldGenerator(Field f, double priority, String[] names) {
		String version = VersionUtils.getVersion(f.getDeclaringClass());
		Object instance;
		try {
			instance = f.getDeclaringClass().getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException exc) {
			return null;
		}
		return new OpFieldInfo(instance, f, version, new DefaultHints(), priority, names);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <U> U convert(TaggedElement discovery, Class<U> info) {
		// Obtain op metadata
		String[] names;
		String opType;
		double priority;

		try {
			names = getOpNames(discovery);
			opType = getOpType(discovery);
			priority = getOpPriority(discovery);
		} catch (IllegalArgumentException e) {
			return null;
		}

		// Delegate to proper constructor
		AnnotatedElement e = discovery.discovery();
		if (e instanceof Class) {
			return (U) opClassGenerator((Class<?>) e, priority, names);
		} else if (e instanceof Method) {
			return (U) opMethodGenerator((Method) e, opType, priority, names);
		} else if (e instanceof Field) {
			return (U) opFieldGenerator((Field) e, priority, names);
		} else
			return null;
	}

	private String[] getOpNames(TaggedElement d) {
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

	private static double getOpPriority(TaggedElement d) {
		String priority = d.option("priority");
		return priority.isEmpty() ? Priority.NORMAL : Double.parseDouble(priority);
	}

	private static String getOpType(TaggedElement d) {
		return d.option("type");
	}

	@Override
	public boolean canDiscover(Class<?> cls) {
		return cls == OpInfo.class;
	}

	@Override
	public String tagType() {
		return TAGTYPE;
	}

}
