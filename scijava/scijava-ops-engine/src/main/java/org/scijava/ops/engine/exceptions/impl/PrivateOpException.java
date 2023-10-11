
package org.scijava.ops.engine.exceptions.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.scijava.ops.engine.exceptions.InvalidOpException;

/**
 * Exception thrown when an Op is declared as private. This is unallowed as the
 * Ops engine cannot access it.
 *
 * @author Gabriel Selzer
 */
public class PrivateOpException extends InvalidOpException {

	public PrivateOpException(final Field field) {
		super("Field " + field + " must be public.");
	}

	public PrivateOpException(final Method method) {
		super("Method " + method + " must be public.");
	}

}
