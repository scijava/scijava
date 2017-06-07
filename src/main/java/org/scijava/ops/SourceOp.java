
package org.scijava.ops;

import java.util.function.Supplier;

import org.scijava.plugin.SciJavaPlugin;

/** {@link Supplier}, as a plugin. */
@FunctionalInterface
public interface SourceOp<O> extends SciJavaPlugin, Supplier<O> {}
