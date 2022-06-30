
package org.scijava.ops.engine.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.ValidityProblem;
import org.scijava.ops.api.OpUtils;
import org.scijava.struct.MemberParser;
import org.scijava.struct.ValidityException;
import org.scijava.types.Types;

public class ClassParameterMemberParser implements
	MemberParser<Class<?>, SynthesizedParameterMember<?>>
{

	@Override
	public List<SynthesizedParameterMember<?>> parse(Class<?> source, Type structType)
		throws ValidityException
	{
		if (source == null) return null;

		final ArrayList<SynthesizedParameterMember<?>> items = new ArrayList<>();
		final ArrayList<ValidityProblem> problems = new ArrayList<>();

		// NB: Reject abstract classes.
		org.scijava.struct.Structs.checkModifiers(source.getName() + ": ", problems, source.getModifiers(), true, Modifier.ABSTRACT);

		// Obtain source's Op method.
		Method opMethod;
		try {
			opMethod = getDeclaredOpMethod(source);
		}
		catch (NoSuchMethodException e1) {
			problems.add(new ValidityProblem("OpClass " + source +
				" does not have a functional method!"));
			throw new ValidityException(problems);
		}

		// obtain a parameterData
		ParameterData paramData = new LazilyGeneratedMethodParameterData(opMethod);

		FunctionalParameters.parseFunctionalParameters(items, problems, source,
			paramData);

		// Fail if there were any problems.
		if (!problems.isEmpty()) throw new ValidityException(problems);

		return items;
	}

	/**
	 * Returns the declared {@link FunctionalInterface} method implemented by the Op
	 * {@code c}, or, as a fallback, the functional method w.r.t. its declaration.
	 * 
	 * @param c the Op {@link Class}
	 * @return the {@link Method} of the {@link FunctionalInterface} implemented
	 *         by {@code c}
	 * @throws NoSuchMethodException when {@code c} does not implement its
	 *           functional method
	 */
	private Method getDeclaredOpMethod(Class<?> c) throws NoSuchMethodException {
		// NB this is the functional method w.r.t. the interface, not w.r.t. the Op
		Method fMethod = OpUtils.findFunctionalMethod(c);
		Type[] paramTypes = Types.getExactParameterTypes(fMethod, c);
		Class<?>[] rawParamTypes = Arrays.stream(paramTypes).map(t -> Types.raw(t))
			.toArray(Class[]::new);
		try {
			return c.getMethod(fMethod.getName(), rawParamTypes);
		} catch(NoSuchMethodException e) {
			return fMethod;
		}
	}

}
