package org.scijava.ops.api.features;

import org.scijava.ops.api.OpRef;

/**
 * Indicates that no op matching a given {@link OpRef} could be found.
 * 
 * @author David Kolb
 */
public class OpMatchingException extends RuntimeException {
	
	private static final long serialVersionUID = 2334342967056340218L;
	
	public OpMatchingException(String message) {
		super(message);
	}

	public OpMatchingException(Throwable cause) {
		super(cause);
	}

	public OpMatchingException(String message, Throwable cause) {
		super(message, cause);
	}

}
