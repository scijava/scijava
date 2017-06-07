
package org.scijava.ops;

import org.scijava.plugin.SciJavaPlugin;

/** Unary function, as a plugin. */
@FunctionalInterface
public interface FunctionOp<I, O> extends java.util.function.Function<I, O>,
	SciJavaPlugin
{}
