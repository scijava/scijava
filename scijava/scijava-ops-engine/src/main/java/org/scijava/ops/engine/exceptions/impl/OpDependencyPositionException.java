
package org.scijava.ops.engine.exceptions.impl;

import java.lang.reflect.Method;

import org.scijava.ops.engine.exceptions.InvalidOpException;

/**
 * Exception thrown when an Op written as a {@link Method} declares an
 * {@link org.scijava.ops.spi.OpDependency} <b>after</b> an Op parameter. All
 * {@link org.scijava.ops.spi.OpDependency} parameters must come before any Op
 * parameters.
 * 
 * @author Gabriel Selzer
 */
public class OpDependencyPositionException extends InvalidOpException {

	public OpDependencyPositionException(final Method opMethod) {
		super("Op Dependencies in Op method " + opMethod +
			" must come before any other parameters!");
	}

}
