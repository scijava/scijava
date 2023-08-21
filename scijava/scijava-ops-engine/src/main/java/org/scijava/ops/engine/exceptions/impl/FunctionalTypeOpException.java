
package org.scijava.ops.engine.exceptions.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.scijava.ops.engine.exceptions.InvalidOpException;

/**
 * Exception thrown when an Op written as a {@link Method} does not conform to a
 * functional {@link Type} (i.e. implementing an interface with a single
 * abstract {@link Method}). This is not allowed, as if there are multiple
 * methods, Ops will not know which method to call!
 *
 * @author Gabriel Selzer
 */
public class FunctionalTypeOpException extends InvalidOpException {

	/**
	 * Standard constructor.
	 * 
	 * @param op An Op implementation
	 * @param fIface the {@link FunctionalInterface} that the Op should conform
	 *          to.
	 */
	public FunctionalTypeOpException(final Object op, final Type fIface) {
		super("The signature of method " + op +
			" does not conform to the op type " + fIface);
	}

	/**
	 * Constructor used when another {@link Exception} indicates an issue with the
	 * functional type
	 *
	 * @param op An Op implementation
	 * @param cause the {@link Throwable} identifying a bad Op type.
	 */
	public FunctionalTypeOpException(final Object op, final Throwable cause) {
		super("The signature of op " + op + " did not have a suitable Op type.",
			cause);
	}

}
