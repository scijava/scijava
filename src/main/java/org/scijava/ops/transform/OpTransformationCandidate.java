package org.scijava.ops.transform;

import java.util.List;

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
	
	public OpCandidate getSourceOp() {
		return srcOp;
	}
	
//	private void substituteAnyInTransformation() {
//		// obtain the type info from the matched Op
//		OpInfo srcInfo = srcOp.opInfo();
//		List<Member<?>> inputs = srcInfo.inputs();
//		Member<?> output = srcInfo.output();
//		
//		//work through the deepest transformation level any resolve the Any types. TODO: how many levels through do we have to go?
//		OpRef deepestRef = transformation.getTarget();
//		deepestRef.
//		
//	}

	public Object exceute(OpService opService, List<?> dependencies, Object... secondaryArgs)
		throws OpMatchingException, OpTransformationException
	{
		Object op = srcOp.createOp(dependencies, secondaryArgs);
//		substituteAnyInTransformation();
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
