
package org.scijava.ops;

import org.scijava.plugin.SciJavaPlugin;

/** Binary function, as a plugin. */
@FunctionalInterface
public interface BiFunctionOp<I1, I2, O> extends SciJavaPlugin,
	java.util.function.BiFunction<I1, I2, O>
{}