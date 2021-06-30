//
//package org.scijava.ops.struct;
//
//import java.lang.reflect.AnnotatedElement;
//import java.lang.reflect.Method;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.scijava.function.Container;
//import org.scijava.function.Mutable;
//import org.scijava.ops.util.AnnotationUtils;
//import org.scijava.param.FunctionalMethodType;
//import org.scijava.param.SynthesizedParameterMember;
//import org.scijava.param.ValidityProblem;
//import org.scijava.struct.ItemIO;
//import org.scijava.struct.Member;
//import org.scijava.types.Types;
//
//public class FunctionalParameterMemberParser implements
//	MemberParser<AnnotatedElement, SynthesizedParameterMember<?>>
//{
//
//	@Override
//	public List<SynthesizedParameterMember<?>> parse(AnnotatedElement source) {
//		// Search for the functional method of 'type' and map its signature to
//		// ItemIO
//		List<FunctionalMethodType> fmts;
//		try {
//			fmts = findFunctionalMethodTypes(type);
//		}
//		catch (IllegalArgumentException e) {
//			problems.add(new ValidityProblem("Could not find functional method of " +
//				type.getTypeName()));
//			return;
//		}
//
//		// Synthesize members
//		List<Member<?>> fmtMembers = data.synthesizeMembers(fmts);
//
//		for (Member<?> m : fmtMembers) {
//			String key = m.getKey();
//			final Type itemType = m.getType();
//
//			final boolean valid = checkValidity(m, key, Types.raw(itemType), false,
//				names, problems);
//			if (!valid) continue;
//			items.add(m);
//			names.add(m.getKey());
//		}
//	}
//
//	/**
//	 * Returns a list of {@link FunctionalMethodType}s describing the input and
//	 * output types of the functional method of the specified functional type. In
//	 * doing so, the return type of the method will me marked as
//	 * {@link ItemIO#OUTPUT} and the all method parameters as
//	 * {@link ItemIO#OUTPUT}, except for parameters annotated with
//	 * {@link Container} or {@link Mutable} which will be marked as
//	 * {@link ItemIO#CONTAINER} or {@link ItemIO#MUTABLE} respectively. If the
//	 * specified type does not have a functional method in its hierarchy,
//	 * {@code null} will be returned.<br>
//	 * The order will be the following: method parameters from left to right, then
//	 * return type.
//	 * 
//	 * @param functionalType
//	 * @return
//	 */
//	public static List<FunctionalMethodType> findFunctionalMethodTypes(
//		Type functionalType)
//	{
//		Method functionalMethod = findFunctionalMethod(Types.raw(functionalType));
//		if (functionalMethod == null) throw new IllegalArgumentException("Type " +
//			functionalType +
//			" is not a functional type, thus its functional method types cannot be determined");
//
//		Type paramfunctionalType = functionalType;
//		if (functionalType instanceof Class) {
//			paramfunctionalType = Types.parameterizeRaw((Class<?>) functionalType);
//		}
//
//		List<FunctionalMethodType> out = new ArrayList<>();
//		int i = 0;
//		for (Type t : Types.getExactParameterTypes(functionalMethod,
//			paramfunctionalType))
//		{
//			final ItemIO ioType;
//			if (AnnotationUtils.getMethodParameterAnnotation(functionalMethod, i,
//				Container.class) != null) ioType = ItemIO.CONTAINER;
//			else if (AnnotationUtils.getMethodParameterAnnotation(functionalMethod, i,
//				Mutable.class) != null) ioType = ItemIO.MUTABLE;
//			else ioType = ItemIO.INPUT;
//			out.add(new FunctionalMethodType(t, ioType));
//			i++;
//		}
//
//		Type returnType = Types.getExactReturnType(functionalMethod,
//			paramfunctionalType);
//		if (!returnType.equals(void.class)) {
//			out.add(new FunctionalMethodType(returnType, ItemIO.OUTPUT));
//		}
//
//		return out;
//	}
//
//}
