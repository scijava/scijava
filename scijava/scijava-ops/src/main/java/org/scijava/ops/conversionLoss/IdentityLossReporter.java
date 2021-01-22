
package org.scijava.ops.conversionLoss;

import org.scijava.ops.core.Op;
import org.scijava.ops.hints.BaseOpHints.Simplification;
import org.scijava.ops.hints.OpHints;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;
import org.scijava.types.Nil;

/**
 * A {@link LossReporter} used when a type is not simplified.
 * 
 * @author Gabriel Selzer
 * @param <T> - the type that is not being simplified.
 */
@OpHints(hints = {Simplification.FORBIDDEN})
@Plugin(type = Op.class, name = "lossReporter")
@Parameter(key = "fromType")
@Parameter(key = "toType")
@Parameter(key = "maximumLoss")
public class IdentityLossReporter<T> implements LossReporter<T, T> {

	@Override
	public Double apply(Nil<T> t, Nil<T> u) {
		return 0.;
	}

}
