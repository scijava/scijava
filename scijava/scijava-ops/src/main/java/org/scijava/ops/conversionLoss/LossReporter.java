
package org.scijava.ops.conversionLoss;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import org.scijava.types.Nil;

/**
 * {@link BiFunction} reporting the <b>worst-case loss</b> of a conversion
 * from a {@link Type} from a {@link Type} {@code t} to a {@code Type r}.
 * 
 * @author Gabriel Selzer
 * @param <T> - the {@code Type} that we are converting <b>from</b>
 * @param <R> - the {@code Type} that we are converting <b>to</b>
 */
@FunctionalInterface
public interface LossReporter<T, R> extends
	BiFunction<Nil<T>, Nil<R>, Double>
{
	@Override
	Double apply(Nil<T> from, Nil<R> to);
}
