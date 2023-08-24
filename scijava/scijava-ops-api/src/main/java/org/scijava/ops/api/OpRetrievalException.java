package org.scijava.ops.api;

/**
 * Indicates that no op satisfying an {@link OpRef} could be retrieved.
 * 
 * @author David Kolb
 */
public class OpRetrievalException extends RuntimeException {

	private static final long serialVersionUID = 2334342967056340218L;

	public OpRetrievalException(String message) {
		super(message);
	}

	public OpRetrievalException(Throwable cause) {
		super(cause);
	}

	public OpRetrievalException(String message, Throwable cause) {
		super(message, cause);
	}

}
