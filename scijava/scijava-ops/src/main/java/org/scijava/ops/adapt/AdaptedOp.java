
package org.scijava.ops.adapt;

import java.lang.reflect.Type;

import org.scijava.ops.matcher.OpAdaptationInfo;
import org.scijava.ops.matcher.OpInfo;

/**
 * Wrapper class combining an adapted Op with its adapted type and associated
 * {@link OpInfo}s
 * 
 * @author Gabriel Selzer
 */
public class AdaptedOp {

	private Object op;
	private Type type;
	private OpInfo srcInfo;
	private OpInfo adaptorInfo;

	private OpInfo opInfo;

	public AdaptedOp(Object op, Type type, OpInfo srcInfo, OpInfo adaptorInfo) {
		this.op = op;
		this.type = type;
		this.srcInfo = srcInfo;
		this.adaptorInfo= adaptorInfo;
		this.opInfo = new OpAdaptationInfo(srcInfo, this.type);

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
	
	public OpInfo opInfo() {
		return opInfo;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Source Op:\n\n" + srcInfo + "\n");
		s.append("With transformation: \n" + adaptorInfo);
		return s.toString();
	}
}
