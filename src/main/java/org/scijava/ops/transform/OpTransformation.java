package org.scijava.ops.transform;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpMatchingException;

public class OpTransformation {

	OpTransformationInfo transformation;
	OpCandidate srcOp;

	public OpTransformation(OpCandidate scrOp, OpTransformationInfo transformation) {
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
