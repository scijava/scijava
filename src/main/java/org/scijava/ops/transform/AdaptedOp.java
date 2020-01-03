
package org.scijava.ops.transform;

import org.scijava.ops.matcher.OpCandidate;
import org.scijava.ops.matcher.OpInfo;

/**
 * Wrapper class to match a {@link OpTransformation} with a matching
 * {@link OpCandidate}.
 * 
 * @author David Kolb
 * @author Gabriel Selzer
 */
public class AdaptedOp {

	Object op;
	OpInfo srcInfo;
	OpInfo adaptorInfo;

	public AdaptedOp(Object op,  OpInfo srcInfo, OpInfo adaptorInfo) {
		this.op = op;
		this.srcInfo = srcInfo;
		this.adaptorInfo= adaptorInfo;
	}
	
	public Object op() {
		return op;
	}

	public OpInfo srcInfo() {
		return srcInfo;
	}

	public OpInfo adaptorInfo() {
		return adaptorInfo;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Source Op:\n\n" + srcInfo + "\n");
		s.append("With transformation: \n" + adaptorInfo);
		return s.toString();
	}
}
