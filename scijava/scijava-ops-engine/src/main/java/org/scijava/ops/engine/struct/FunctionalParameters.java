package org.scijava.ops.engine.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.scijava.ValidityProblem;
import org.scijava.function.Container;
import org.scijava.function.Mutable;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.engine.util.internal.AnnotationUtils;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.types.Types;

public class FunctionalParameters {

	public static void parseFunctionalParameters(
		final ArrayList<SynthesizedParameterMember<?>> items,
		final ArrayList<ValidityProblem> problems, Type type, ParameterData data)
	{
		// Search for the functional method of 'type' and map its signature to
		// ItemIO
		List<FunctionalMethodType> fmts;
		try {
			fmts = FunctionalParameters.findFunctionalMethodTypes(type);
		}
		catch (IllegalArgumentException e) {
			problems.add(new ValidityProblem("Could not find functional method of " +
				type.getTypeName()));
			return;
		}
	
		// Synthesize members
		List<SynthesizedParameterMember<?>> fmtMembers = data.synthesizeMembers(fmts);
	
		for (SynthesizedParameterMember<?> m : fmtMembers) {
			final Type itemType = m.getType();
	
			final boolean valid = org.scijava.struct.Structs.checkValidity(m, () -> m.getKey(), Types.raw(itemType), false,
				problems);
			if (!valid) continue;
			items.add(m);
		}
	}

	/**
	 * Returns a list of {@link FunctionalMethodType}s describing the input and
	 * output types of the functional method of the specified functional type. In
	 * doing so, the return type of the method will me marked as
	 * {@link ItemIO#OUTPUT} and the all method parameters as
	 * {@link ItemIO#OUTPUT}, except for parameters annotated with
	 * {@link Container} or {@link Mutable} which will be marked as
	 * {@link ItemIO#CONTAINER} or {@link ItemIO#MUTABLE} respectively. If the
	 * specified type does not have a functional method in its hierarchy,
	 * {@code null} will be returned.<br>
	 * The order will be the following: method parameters from left to right, then
	 * return type.
	 * 
	 * @param functionalType
	 * @return
	 */
	public static List<FunctionalMethodType> findFunctionalMethodTypes(
		Type functionalType)
	{
		Method functionalMethod = OpUtils.findFunctionalMethod(Types.raw(functionalType));
		if (functionalMethod == null) throw new IllegalArgumentException("Type " +
			functionalType +
			" is not a functional type, thus its functional method types cannot be determined");
	
		Type paramfunctionalType = functionalType;
		if (functionalType instanceof Class) {
			paramfunctionalType = Types.parameterizeRaw((Class<?>) functionalType);
		}
	
		List<FunctionalMethodType> out = new ArrayList<>();
		int i = 0;
		for (Type t : Types.getExactParameterTypes(functionalMethod,
			paramfunctionalType))
		{
			final ItemIO ioType;
			if (AnnotationUtils.getMethodParameterAnnotation(functionalMethod, i,
				Container.class) != null) ioType = ItemIO.CONTAINER;
			else if (AnnotationUtils.getMethodParameterAnnotation(functionalMethod, i,
				Mutable.class) != null) ioType = ItemIO.MUTABLE;
			else ioType = ItemIO.INPUT;
			out.add(new FunctionalMethodType(t, ioType));
			i++;
		}
	
		Type returnType = Types.getExactReturnType(functionalMethod,
			paramfunctionalType);
		if (!returnType.equals(void.class)) {
			out.add(new FunctionalMethodType(returnType, ItemIO.OUTPUT));
		}
	
		return out;
	}

}
