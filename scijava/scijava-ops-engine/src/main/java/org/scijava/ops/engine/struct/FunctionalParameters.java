package org.scijava.ops.engine.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.function.Container;
import org.scijava.function.Mutable;
import org.scijava.ops.engine.util.Ops;
import org.scijava.ops.engine.util.internal.AnnotationUtils;
import org.scijava.ops.spi.Nullable;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Structs;
import org.scijava.types.Types;

public class FunctionalParameters {

	public static void parseFunctionalParameters(
		final ArrayList<SynthesizedParameterMember<?>> items,
		Type type, ParameterData data)
	{
		// Search for the functional method of 'type' and map its signature to
		// ItemIO
		List<FunctionalMethodType> fmts = FunctionalParameters.findFunctionalMethodTypes(type);

		// Synthesize members
		List<SynthesizedParameterMember<?>> fmtMembers = data.synthesizeMembers(fmts);
	
		for (SynthesizedParameterMember<?> m : fmtMembers) {
			final Class<?> itemType = Types.raw(m.getType());
			if ((m.getIOType() == ItemIO.MUTABLE || m
				.getIOType() == ItemIO.CONTAINER) && Structs.isImmutable(itemType))
			{
				// NB: The MUTABLE and CONTAINER types signify that the parameter
				// will be written to, but immutable parameters cannot be changed in
				// such a manner, so it makes no sense to label them as such.
				throw new IllegalArgumentException("Immutable " + m.getIOType() +
					" parameter: " + m.getKey() + " (" + itemType.getName() +
					" is immutable)");
			}
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
		Method functionalMethod = Ops.findFunctionalMethod(Types.raw(functionalType));
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


	public static Boolean hasNullableAnnotations(Method m) {
		return Arrays.stream(m.getParameters()).anyMatch(p -> p.isAnnotationPresent(
				Nullable.class));
	}

	public static Boolean[] findParameterNullability(Method m) {
		return Arrays.stream(m.getParameters()).map(p -> p.isAnnotationPresent(
				Nullable.class)).toArray(Boolean[]::new);
	}

	public static List<Method> fMethodsWithNullable(Class<?> opClass) {
		Method superFMethod = Ops.findFunctionalMethod(opClass);
		return Arrays.stream(opClass.getMethods()) //
				.filter(m -> m.getName().equals(superFMethod.getName())) //
				.filter(m -> m.getParameterCount() == superFMethod.getParameterCount()) //
				.filter(m -> hasNullableAnnotations(m)) //
				.collect(Collectors.toList());
	}

	public static Boolean[] generateAllRequiredArray(int num) {
		Boolean[] arr = new Boolean[num];
		Arrays.fill(arr, false);
		return arr;
	}

}
