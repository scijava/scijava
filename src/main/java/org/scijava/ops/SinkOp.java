
package org.scijava.ops;

import java.util.function.Consumer;

import org.scijava.plugin.SciJavaPlugin;

/** {@link Consumer}, as a plugin. */
@FunctionalInterface
public interface SinkOp<I> extends SciJavaPlugin, Consumer<I> {}
