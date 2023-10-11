
package org.scijava.ops.engine.exceptions.impl;

import org.scijava.ops.engine.exceptions.InvalidOpException;

/**
 * Exception thrown when an Op is not visible to the Op engine. This is not
 * allowed as without visibility the Ops Engine cannot instantiate the Op.
 *
 * @author Gabriel Selzer
 */
public class UnreadableOpException extends InvalidOpException {

	public UnreadableOpException(final String packageName) {
		super("Package " + packageName +
			" is not opened to SciJava Ops Engine. Please ensure that " +
			packageName + " is opened or exported to SciJava Ops Engine");
	}

}
