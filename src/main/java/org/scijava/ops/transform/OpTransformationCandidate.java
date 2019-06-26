
package org.scijava.ops.transform;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.matcher.OpMatchingException;
import org.scijava.ops.matcher.OpRef;
import org.scijava.struct.Member;

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

		OpInfo srcInfo = srcOp.opInfo();
		OpRef srcRef = new OpRef(srcOp.getRef().getName(), new Type[] { srcInfo.opType() }, srcInfo.output().getType(),
			srcInfo.inputs().stream().map(Member::getType).toArray(Type[]::new));
		OpRef targetRef = transformation.getTransformer().substituteAnyInTargetRef(srcRef, transformation.getTarget());

		return transformation.execute(op, targetRef, opService);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Source Op:\n\n" + srcOp + "\n");
		s.append("With transformation: \n" + transformation);
		return s.toString();
	}
}
