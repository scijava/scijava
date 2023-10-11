
package org.scijava.ops.engine.exceptions;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.Op;

/**
 * {@link Exception} denoting that an {@link Op} implementation could not be
 * accepted into an {@link OpEnvironment} because of invalid parameters or
 * configuration.
 * 
 * @author Gabriel Selzer
 */
public abstract class InvalidOpException extends RuntimeException {

	public InvalidOpException(final String message) {
		super(message);
	}

	public InvalidOpException(final Throwable cause) {
		super(cause);
	}

	public InvalidOpException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
