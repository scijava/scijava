
package org.scijava.ops.engine.struct;

import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.ParamJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.scijava.function.Producer;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.MemberParser;
import org.scijava.struct.Struct;
import org.scijava.struct.ValidityException;

/**
 * Looks for {@link OpDependency} annotations on
 * {@link java.lang.reflect.Parameter}s. For each such parameter with an
 * {@link OpDependency} annotation, this class creates a
 * {@link MethodParameterOpDependencyMember}.
 *
 * @author Gabriel Selzer
 */
public class MethodOpDependencyMemberParser implements
	MemberParser<Method, MethodParameterOpDependencyMember<?>>
{

	private static final Map<Method, MethodJavadoc> methodDocMap = new ConcurrentHashMap<>();

	/**
	 * Parses out the {@link MethodParameterOpDependencyMember}s from {@code
	 * source}
	 * 
	 * @param source the {@link Object} to parse
	 * @param structType TODO
	 * @return a {@link List} of all {@link MethodParameterOpDependencyMember}s
	 *         described via the {@link OpDependency} annotation in {@code source}
	 * @throws ValidityException
	 */
	@Override
	public List<MethodParameterOpDependencyMember<?>> parse(Method source,
		Type structType) throws ValidityException
	{
		if (source == null) return null;

		source.setAccessible(true);

		final ArrayList<MethodParameterOpDependencyMember<?>> items = new ArrayList<>();

		// Parse method level @OpDependency annotations.
		parseMethodOpDependencies(items, source);

		return items;
	}

	private static MethodJavadoc getDocData(Method annotatedMethod) {
		return methodDocMap.computeIfAbsent(annotatedMethod,
			RuntimeJavadoc::getJavadoc);
	}

	private static void parseMethodOpDependencies(
		final List<MethodParameterOpDependencyMember<?>> items,
		final Method annotatedMethod)
	{
		// If the Op method has no dependencies, return false without looping
		// through parameters
		boolean hasOpDependencies = Arrays.stream(annotatedMethod.getParameters()) //
			.anyMatch(param -> param.isAnnotationPresent(OpDependency.class));
		if (!hasOpDependencies) return;

		final java.lang.reflect.Parameter[] methodParams = annotatedMethod
			.getParameters();

		for (int i = 0; i < methodParams.length; i++) {
			final OpDependency dependency = methodParams[i].getAnnotation(
				OpDependency.class);
			if (dependency == null) continue;

			final int j = i;
			Producer<String> nameGenerator = () -> {
				List<ParamJavadoc> params = getDocData(annotatedMethod).getParams();
				if (params.size() <= j) return methodParams[j].getName();
				return getDocData(annotatedMethod).getParams().get(j).getName();
			};

			Producer<String> descriptionGenerator = () -> {
				List<ParamJavadoc> params = getDocData(annotatedMethod).getParams();
				if (params.size() <= j) return "";
				return getDocData(annotatedMethod).getParams().get(j).getComment()
					.toString();
			};
			final Type methodParamType = methodParams[i].getParameterizedType();
			final MethodParameterOpDependencyMember<?> item =
				new MethodParameterOpDependencyMember<>(nameGenerator,
					descriptionGenerator, methodParamType, dependency);
			items.add(item);
		}
	}

}
