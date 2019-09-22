package org.scijava.ops.transform;

import java.util.Arrays;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;

/**
 * Class used to describe an Op transformation possibly chained with other transformations. 
 * 
 * @author David Kolb
 */
public class OpTransformation {

	private OpRef srcRef;
	private OpRef targetRef;
	private OpTransformer transformation;
	
	private OpTransformation child;

	public OpTransformation(OpRef from, OpRef to, OpTransformer transformer) {
		this.srcRef = from;
		this.targetRef = to;
		this.transformation = transformer;
	}

	/**
	 * Returns the source ref of this transformation.
	 * 
	 * @return
	 */
	public OpRef getSource() {
		return srcRef;
	}

	/**
	 * Returns the target ref of this transformation.
	 * 
	 * @return
	 */
	public OpRef getTarget() {
		return targetRef;
	}
	
	/**
	 * Returns the transformer.
	 * 
	 * @return
	 */
	public OpTransformer getTransformer() {
		return transformation;
	}
	
	/**
	 * Get the child transformation if this transformation is part of a chain.
	 * 
	 * @return
	 */
	public OpTransformation getChild() {
		return this.child;
	}

	/**
	 * Executes this transformation on the specified object.
	 * If this transformation describes a chain, the whole chain will
	 * be executed.
	 * 
	 * @param obj
	 * @param opService
	 * @return
	 * @throws OpTransformationException 
	 */
	public Object execute(Object obj, OpService opService) throws OpTransformationException {
		Object candidate = obj;
		OpTransformation c = this;
		do {
			candidate = c.getTransformer().transform(opService, candidate, c.targetRef);
			c = c.getChild();
		} while (c != null);
		
		return candidate;
	}
	
	/**
	 * Chains the specified with this transformation. If this transformation is executed,
	 * the chained transformation will be executed after this one.
	 * 
	 * @param transformation
	 * @return
	 */
	public OpTransformation chain(OpTransformation transformation) {
		child = transformation;
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		int i = 0;
		OpTransformation c = this;
		do {
			s.append(i + ")");
			s.append("\tFrom:\t");
			s.append(Arrays.deepToString(c.getSource().getTypes()));
			s.append("\n\tTo:\t\t");
			s.append(Arrays.deepToString(c.getTarget().getTypes()));
			s.append("\n\tWith:\t");
			s.append(c.getTransformer().getClass().getName());
			s.append("\n\n");
			c = c.getChild();
			i++;
		} while (c != null);
		
		return s.toString();
	}
}
