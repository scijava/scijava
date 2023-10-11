
package org.scijava.ops.engine.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.ops.engine.util.Ops;
import org.scijava.struct.MemberParser;
import org.scijava.struct.Structs;
import org.scijava.types.Types;

public class ClassParameterMemberParser implements
	MemberParser<Class<?>, SynthesizedParameterMember<?>>
{

	@Override
	public List<SynthesizedParameterMember<?>> parse(Class<?> source, Type structType)
	{
		if (source == null) return null;

		final ArrayList<SynthesizedParameterMember<?>> items = new ArrayList<>();

		// NB: Reject abstract classes.
		Structs.checkModifiers(source.getName() + ": ", source.getModifiers(), true, Modifier.ABSTRACT);

		// Obtain source's Op method.
		Method opMethod;
		try {
			opMethod = getDeclaredOpMethod(source);
		}
		catch (NoSuchMethodException e1) {
			throw new IllegalArgumentException("Class " + source.getName() +
				" does not have a functional method!", e1);
		}

		// obtain a parameterData
		Class<?> fIface = Ops.findFunctionalInterface(source);
		ParameterData paramData = new LazilyGeneratedMethodParameterData(opMethod, fIface);

		try {
			FunctionalParameters.parseFunctionalParameters(items, source, paramData);
		} catch (IllegalArgumentException exc) {
			throw new FunctionalTypeOpException(source, exc);
		}

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
		Method fMethod = Ops.findFunctionalMethod(c);
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
