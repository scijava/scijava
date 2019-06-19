package org.scijava.ops.transform.functional;

import java.lang.reflect.Type;

import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.util.Types;

/**
 * Interface for transformers converting between functional Op types.</br>
 * E.g. Function -> Computer, BiComputer -> BiFunction
 * 
 * @author David Kolb
 */
public interface FunctionalTypeTransformer extends OpTransformer {

	@Override
	default OpRef getRefTransformingTo(OpRef toRef) {
		Type[] refTypes = toRef.getTypes();
		if(Types.raw(refTypes[0]) != targetClass()) return null;
		boolean hit = TypeModUtils.replaceRawTypes(refTypes, targetClass(), srcClass());
		if (hit) {
			return OpRef.fromTypes(toRef.getName(), refTypes, getTransformedOutputType(toRef),
					getTransformedArgTypes(toRef));
		}
		return null;
	}

	/**
	 * Get the raw class of the target type to transform to.
	 * 
	 * @return
	 */
	Class<?> targetClass();

	/**
	 * Get arg types of an Op that this transformer would be able to convert.
	 * The arg types should be inferred from the specified target ref.
	 * 
	 * @param toRef
	 * @return
	 * @see OpTransformer#getRefTransformingTo(OpRef)
	 */
	Type[] getTransformedArgTypes(OpRef targetRef);

	/**
	 * Get output types of an Op that this transformer would be able to convert.
	 * The output types should be inferred from the specified target ref.
	 * 
	 * @param toRef
	 * @return
	 * @see OpTransformer#getRefTransformingTo(OpRef)
	 */
	Type getTransformedOutputType(OpRef targetRef);
}
