
package org.scijava.ops.engine.simplify;

import java.util.function.Function;

import org.scijava.ops.engine.OpHints;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.spi.Op;
import org.scijava.plugin.Plugin;

@OpHints(hints = { Simplification.FORBIDDEN })
@Plugin(type = Op.class, name = "simplify, focus")
public class Identity<T> implements Function<T, T> {

	public Identity() {}

	/**
	 * @param t the object to be simplified
	 * @return the simplified object (since we are doing an identity
	 *         simplification, this is just a reference to the input object).
	 */
	@Override
	public T apply(T t) {
		return t;
	}
}
