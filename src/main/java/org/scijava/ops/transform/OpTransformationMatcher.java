
package org.scijava.ops.transform;

import java.util.List;

import org.scijava.ops.OpEnvironment;
import org.scijava.ops.matcher.OpRef;

/**
 * Matches {@link OpTransformer}s that transform {@link OpRef}s and Ops.
 *
 * @author David Kolb
 */
public interface OpTransformationMatcher {

	/**
	 * Retrieve a list of {@link OpTransformation}s that describe a transformation
	 * that is able to transform an Op matching an {@link OpRef} into another Op
	 * matching the specified {@link OpRef}. This can be used to find Ops that are
	 * transformable into the specified {@link OpRef}.
	 *
	 * @param opRef the ref which should be the target of the transformations to
	 *          look for
	 */
	List<OpTransformation> getTransformationsTo(Iterable<OpTransformer> transformers, OpRef opRef, int currentChainLength);

	/**
	 * Attempts to find an {@link AdaptedOp}, transforming an
	 * existing Op into another Op that matches the specified {@link OpRef}. This
	 * can be used if no Op matching the specified ref is available, however there
	 * are Op transformations that are able to transform an existing Op into the
	 * requested one. E.g. a Computer is requested, however there is only a
	 * Function operating on the same types and a transformer from Function to
	 * Computer available. Hence, one can take the available Function and
	 * transform it into the requested Computer and return it.
	 *
	 * @param opEnv the env to supply ops
	 * @param ref the ref which should be the target of the transformation to look
	 *          for
	 */
	AdaptedOp findTransformation(OpEnvironment opEnv, Iterable<OpTransformer> transformers, OpRef ref);
}
