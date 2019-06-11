package org.scijava.ops;

/**
 * Thrown to indicate that an Op failed in its execution
 * 
 * @author Gabriel Selzer
 *
 */
public class OpExecutionException extends RuntimeException {

	/**
	 * Constructs a <code>OpExecutionException</code> with the specified reason for
	 * failure.
	 * 
	 * @param s
	 *            the reason for the failure
	 */
	public OpExecutionException(String s) {
		super(s);
	}

	/**
	 * Constructs a <code>OpExecutionException</code> with the specified cause.
	 * 
	 * @param cause
	 *            the cause of the failure
	 */
	public OpExecutionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a <code>OpExecutionException</code> with the specified reason for
	 * failure and cause.
	 * 
	 * @param message
	 *            the reason for the failure
	 * @param cause
	 *            the cause of the failure
	 */
	public OpExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

}
