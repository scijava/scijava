
package org.scijava.ops.engine.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.Discovery;
import org.scijava.function.Functions;
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

	private final List<Discoverer> discoverers;

	public TagBasedOpInfoGenerator(Discoverer... d) {
		this.discoverers = Arrays.asList(d);
	}

	Functions.Arity3<Class<?>, Double, String[], OpClassInfo> opClassGenerator = //
		(cls, priority, names) -> {
			String version = VersionUtils.getVersion(cls);
			return new OpClassInfo(cls, version, new DefaultHints(), priority, names);
		};

	Functions.Arity3<Method, Double, String[], OpMethodInfo> opMethodGenerator = //
		(m, priority, names) -> {
			String version = VersionUtils.getVersion(m.getDeclaringClass());
			return new OpMethodInfo(m, version, new DefaultHints(), names);
		};

	Functions.Arity3<Field, Double, String[], OpFieldInfo> opFieldGenerator = //
		(f, priority, names) -> {
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
			return new OpFieldInfo(instance, f, version, new DefaultHints(), names);
		};

	@Override
	public List<OpInfo> generateInfos() {
		List<OpInfo> infos = discoverers.stream() //
			.flatMap(d -> d.elementsTaggedWith("op").stream()) //
			.map(discovery -> {
				AnnotatedElement e = discovery.discovery();
				String[] names = getOpNames(discovery.tag());
				double priority = getOpPriority(discovery.tag());
				if (e instanceof Class) {
					return opClassGenerator.apply((Class<?>) e, priority, names);
				}
				else if (e instanceof Method) {
					return opMethodGenerator.apply((Method) e, priority, names);
				}
				else if (e instanceof Field) {
					return opFieldGenerator.apply((Field) e, priority, names);
				}
				else return null;
			}) //
			.filter(Objects::nonNull) //
			.collect(Collectors.toList());
		return infos;
	}

	private static String[] getOpNames(String tag) {
		int tagTypeIndex = tag.indexOf(TAGTYPE + "=");
		String joinedNames = tag.substring(tagTypeIndex + TAGTYPE.length() + 1)
			.split(" ")[0];
		return OpUtils.parseOpNames(joinedNames);
	}

	private static double getOpPriority(String tag) {
		int tagTypeIndex = tag.indexOf("priority=");
		if (tagTypeIndex == -1) return Priority.NORMAL;
		return Double.parseDouble(tag.substring(tagTypeIndex + "priority=".length())
			.split(" ")[0]);
	}

}
