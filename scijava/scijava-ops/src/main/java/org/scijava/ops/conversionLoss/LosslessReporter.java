
package org.scijava.ops.conversionLoss;

import org.scijava.types.Nil;

/**
 * A subtype of {@link LossReporter} that reports 0 loss. This makes declaring
 * lossless {@code LossReporter}s cleaner.
 * 
 * @author G
 * @param <T>
 * @param <R>
 */
public interface LosslessReporter<T, R> extends LossReporter<T, R> {

	@Override
	default Double apply(Nil<T> from, Nil<R> to) {
		return 0.;
	}

}
