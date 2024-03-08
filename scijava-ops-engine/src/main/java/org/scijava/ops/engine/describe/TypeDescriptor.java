
package org.scijava.ops.engine.describe;

import org.scijava.types.Nil;

import java.util.function.Function;

/**
 * Convenience interface for describing an Op that turns a type into a
 * {@link String}
 *
 * @param <T> the type that this {@code TypeDescriptor} describes
 * @author Gabriel Selzer
 */
public interface TypeDescriptor<T> extends Function<Nil<T>, String> {}
