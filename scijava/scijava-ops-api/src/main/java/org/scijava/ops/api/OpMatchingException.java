package org.scijava.ops.api;

/**
 * Indicates that no op satisfying an {@link OpRequest} could be retrieved.
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
