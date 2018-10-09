package org.scijava.ops.transform;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpMatchingException;

/**
 * Wrapper class to match a {@link OpTransformation} with a matching
 * {@link OpCandidate}.
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

	public Object exceute(OpService opService, Object... secondaryArgs)
			throws OpMatchingException, OpTransformationException {
		Object op = srcOp.createOp(secondaryArgs);
		return transformation.execute(op, opService);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Source Op:\n\n" + srcOp + "\n");
		s.append("With transformation: \n" + transformation);
		return s.toString();
	}
}
