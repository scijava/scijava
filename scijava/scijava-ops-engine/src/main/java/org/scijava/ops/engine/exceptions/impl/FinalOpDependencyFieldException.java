
package org.scijava.ops.engine.exceptions.impl;

import java.lang.reflect.Field;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.engine.exceptions.InvalidOpException;
import org.scijava.ops.spi.OpDependency;

/**
 * Exception thrown when an Op has a {@link Field} {@link OpDependency} that is
 * {@code final}. This is not allowed as the {@link OpEnvironment} must inject
 * the {@link OpDependency} at runtime, after instantiation.
 * 
 * @author Gabriel Selzer
 */
public class FinalOpDependencyFieldException extends InvalidOpException {

	public FinalOpDependencyFieldException(final Field f) {
		super("Invalid Op dependency: Field dependency " + f.getName() + " of Op " +
			f.getDeclaringClass() + " cannot be final!");
	}
}
