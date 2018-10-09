package org.scijava.ops.transform;

/**
 * Indicates that an exception occurred during Op transformation.
 * 
 * @author David Kolb
 */
public class OpTransformationException extends Exception {
	
	private static final long serialVersionUID = -2389578684593501547L;

	public OpTransformationException(String message) {
		super(message);
	}
	
	public OpTransformationException(String message, Throwable e) {
		super(message, e);
	}
	
	public OpTransformationException(Throwable e) {
		super(e);
	}
}
