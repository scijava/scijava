package org.scijava.ops.transform;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpMatchingException;

/**
 * Wrapper class to match a {@link OpTransformation} with a matching {@link OpCandidate}.
 * 
 * @author David Kolb
 */
public class OpTransformationCandidate {

	OpTransformation transformation;
	OpCandidate srcOp;

	public OpTransformationCandidate(OpCandidate scrOp, OpTransformation transformation) {
		this.srcOp = scrOp;
		this.transformation = transformation;
	}
	
	public Object exceute(OpService opService, Object... secondaryArgs) {
		try {
			Object op = srcOp.createOp(secondaryArgs);
			return transformation.execute(op, opService);
		} catch (OpMatchingException e) {
			return null;
		}
	}
}
