package org.scijava.ops;

import java.util.function.BiPredicate;

/**
 * An op that works like a {@link BiPredicate}.
 * 
 * @author Gabriel Einsdorf
 * 
 * @param <I1>
 * @param <I2>
 */
@FunctionalInterface
public interface BiPredicateOp<I1, I2> extends BiFunctionOp<I1, I2, Boolean>, BiPredicate<I1, I2> {

	@Override
	default Boolean apply(I1 in1, I2 in2) {
		return test(in1, in2);
	}
}
