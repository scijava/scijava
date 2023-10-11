
package org.scijava.ops.engine.exceptions.impl;

import java.lang.reflect.Method;

import org.scijava.ops.engine.exceptions.InvalidOpException;

/**
 * Exception thrown when an Op declared as a {@link Method} is an instance
 * method of some class. This is not allowed, as the Op engine doesn't know how
 * to instantiate the declaring class of an instance method.
 * 
 * @author Gabriel Selzer
 */
public class InstanceOpMethodException extends InvalidOpException {

	/**
	 * Default constructor
	 * 
	 * @param method the instance method
	 */
	public InstanceOpMethodException(final Method method) {
		super("Method " + method + " must be static");
	}

}
