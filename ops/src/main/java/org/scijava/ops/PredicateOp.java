
package org.scijava.ops;

import java.util.function.Predicate;

/** {@link Predicate}, as a plugin. */
@FunctionalInterface
public interface PredicateOp<IO> extends Predicate<IO> {}
