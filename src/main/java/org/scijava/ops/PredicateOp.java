
package org.scijava.ops;

import java.util.function.Predicate;

import org.scijava.plugin.SciJavaPlugin;

/** {@link Predicate}, as a plugin. */
@FunctionalInterface
public interface PredicateOp<IO> extends Predicate<IO>, SciJavaPlugin {
}
