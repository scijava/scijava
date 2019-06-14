
package org.scijava.ops.transform.functional;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.scijava.ops.OpUtils;
import org.scijava.ops.core.computer.Computer3;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpTransformationException;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;

/**
 * Convenience interface for transformers converting between different
 * categories of functional Op types (e.g., functions to computers or vice
 * versa).
 * <P>
 * This interface mostly exists to reduce boilerplate code and does not serve
 * any conceptual purpose within the type hierarchy, that is why it is not
 * public.
 *
 * @author David Kolb
 * @author Marcel Wiedenmann
 */
interface FunctionalTypeTransformer extends OpTransformer {

	/**
	 * The default implementation determines the arity of the target functional
	 * interface, retrieves source functional interfaces that match this arity,
	 * and adds each of them to the list of potential sources if it can substitute
	 * the target interface.
	 */
	@Override
	default Collection<OpRef> getRefsTransformingTo(final OpRef targetRef) {
		final Class<?> targetFunctionalRawType = OpUtils.findFirstImplementedFunctionalInterface(targetRef);
		if (targetFunctionalRawType != null) {
			final Integer targetArity = getTargetArity(targetFunctionalRawType);
			if (targetArity != null) {
				// NB: Multiple possible sources per target when transforming inplaces.
				final List<Class<?>> srcFunctionalRawTypes = getSourceFunctionalInterfaces(targetArity);
				for (final Class<?> srcFunctionalRawType : srcFunctionalRawTypes) {
					if (srcFunctionalRawType != null) {
						final Type[] targetOpTypes = targetRef.getTypes();
						final List<Type> srcOpTypes = new ArrayList<>(targetOpTypes.length);
						for (final Type targetOpType : targetOpTypes) {
							final Type srcOpType = getSourceOpType(targetOpType, targetFunctionalRawType, srcFunctionalRawType);
							if (srcOpType != null) {
								srcOpTypes.add(srcOpType);
							}
						}
						if (!srcOpTypes.isEmpty()) {
							final Type[] srcInputParamTypes = getSourceInputParameterTypes(targetRef, targetArity);
							return Collections.singleton(OpRef.fromTypes(targetRef.getName(), srcOpTypes.toArray(new Type[0]),
								targetRef.getOutType(), srcInputParamTypes));
						}
					}
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * @return The arity of the given functional interface of the target category
	 *         (e.g., {@code 1} for {@link Function} or {@code 3} for
	 *         {@link Computer3}), or {@code null} if the functional interface is
	 *         invalid or unknown.
	 */
	Integer getTargetArity(Class<?> targetFunctionalRawType);

	/**
	 * @return The functional interfaces of the source category that correspond to
	 *         the given <em>target</em> arity. (In practice, source and target
	 *         arity will be equivalent, however.) Returns an empty collection if
	 *         there are no matching source interfaces.
	 */
	List<Class<?>> getSourceFunctionalInterfaces(int targetArity);

	/**
	 * The default implementation simply replaces the raw functional interface of
	 * the target by the source one while keeping the type arguments.
	 *
	 * @return The source Op type or {@code null} if it could not be inferred.
	 * @see InplaceToFunctionTransformer#getSourceOpType(Type, Class, Class)
	 */
	default Type getSourceOpType(final Type targetOpType, final Class<?> targetFunctionalRawType,
		final Class<?> srcFunctionalRawType)
	{
		final Type[] targetOpTypeArray = new Type[] { targetOpType };
		final boolean hit = TypeModUtils.replaceRawTypes(targetOpTypeArray, targetFunctionalRawType, srcFunctionalRawType);
		return hit ? targetOpTypeArray[0] : null;
	}

	/**
	 * Extracts the input parameters of the <em>source</em> from the
	 * <em>target</em> {@link OpRef}. This is necessary because, e.g., the output
	 * parameter of a computer is also part of its input parameters, which is not
	 * the case for functions.
	 */
	Type[] getSourceInputParameterTypes(OpRef targetRef, int targetArity);

	default OpTransformationException createCannotTransformException(final Object src, final OpRef targetRef,
		final String problem, final Throwable cause)
	{
		final String message = "Cannot transform source Op:\n" + src.getClass().getName() + " into target:\n" + targetRef +
			"\nusing transformer: " + this.getClass().getName() + ".\n" + problem;
		return cause != null //
			? new OpTransformationException(message, cause) //
			: new OpTransformationException(message);
	}
}
