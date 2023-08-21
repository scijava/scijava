
package org.scijava.ops.engine.exceptions.impl;

import org.scijava.ops.engine.exceptions.InvalidOpException;

/**
 * An {@link InvalidOpException} arising when an Op declares multiple outputs.
 * This is not allowed as an Op, by definition, can only have one output.
 *
 * @author Gabriel Selzer
 */
public class MultipleOutputsOpException extends InvalidOpException {

	public MultipleOutputsOpException(Object op) {
		super("Multiple output parameters specified for Op " + op +
			" - Only a single output is allowed.");
	}
}
