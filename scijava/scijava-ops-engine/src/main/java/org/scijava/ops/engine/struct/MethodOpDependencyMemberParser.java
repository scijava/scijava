
package org.scijava.ops.engine.struct;

import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.ParamJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.ValidityProblem;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.MemberParser;
import org.scijava.struct.ValidityException;

public class MethodOpDependencyMemberParser implements
	MemberParser<Method, MethodParameterOpDependencyMember<?>>
{

	@Override
	public List<MethodParameterOpDependencyMember<?>> parse(Method source)
		throws ValidityException
	{
		if (source == null) return null;

		source.setAccessible(true);

		final ArrayList<MethodParameterOpDependencyMember<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();

		// Parse method level @OpDependency annotations.
		parseMethodOpDependencies(items, source);

		// Fail if there were any problems.
		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}

	private static void parseMethodOpDependencies(final List<MethodParameterOpDependencyMember<?>> items,
		final Method annotatedMethod)
	{
		boolean hasOpDependencies = Arrays.stream(annotatedMethod.getParameters()) //
				.anyMatch(param -> param.isAnnotationPresent(OpDependency.class));
		if (!hasOpDependencies) return;

		MethodJavadoc javadoc = RuntimeJavadoc.getJavadoc(annotatedMethod);
		List<ParamJavadoc> params = javadoc.getParams();
		final java.lang.reflect.Parameter[] methodParams = annotatedMethod
			.getParameters();

		for (int i = 0; i < methodParams.length; i++) {
			final OpDependency dependency = methodParams[i].getAnnotation(OpDependency.class);
			if (dependency == null) continue;

			final String name = params.size() > i ? params.get(i).getName() : methodParams[i].getName();
			final String description = params.size() > i ? params.get(i).getComment().toString() : "";
			final Type methodParamType = methodParams[i].getParameterizedType();
			final MethodParameterOpDependencyMember<?> item =
				new MethodParameterOpDependencyMember<>(name, description,
					methodParamType, dependency);
			items.add(item);
		}
	}

}
