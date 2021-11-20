
package org.scijava.ops.engine.conversionLoss.impl;

import org.scijava.ops.api.OpHints;
import org.scijava.ops.engine.BaseOpHints.Simplification;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.ops.spi.Op;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;

/**
 * A {@link LossReporter} used when a type is not simplified.
 * 
 * @author Gabriel Selzer
 * @param <T> - the type that is not being simplified.
 */
@OpHints(hints = {Simplification.FORBIDDEN})
@Plugin(type = Op.class, name = "lossReporter")
public class IdentityLossReporter<T> implements LossReporter<T, T> {

	/**
	 * @param t the Nil describing the type that is being converted from
	 * @param u the Nil describing the type that is being converted to
	 * @return the worst-case loss converting from type T to type T (i.e. 0)
	 */
	@Override
	public Double apply(Nil<T> t, Nil<T> u) {
		return 0.;
	}

}
