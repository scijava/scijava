
package org.scijava.ops.engine.exceptions.impl;

import java.lang.reflect.Method;
import java.util.List;

import org.scijava.ops.engine.exceptions.InvalidOpException;
import org.scijava.ops.spi.Nullable;

/**
 * Exception thrown when an Op declares {@link Nullable} parameters in multiple
 * places (i.e. both on the implementation method and on an abstract interface
 * method). This is not allowed to avoid action-at-a-distance errors.
 *
 * @author Gabriel Selzer
 */
public class NullablesOnMultipleMethodsException extends InvalidOpException {

	public NullablesOnMultipleMethodsException(final Object source,
		final List<Method> functionalMethodsWithNullables)
	{
		super("Only one method in the hierarchy of Op " + source +
			" can declare @Nullable parameters, however this Op has multiple: " +
			functionalMethodsWithNullables);
	}

}
