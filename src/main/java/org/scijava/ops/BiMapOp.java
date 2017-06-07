
package org.scijava.ops;

import java.util.function.Consumer;

import org.scijava.plugin.SciJavaPlugin;

/** (flat)map -- 1->N */
@FunctionalInterface
public interface BiMapOp<I1, I2, O> extends SciJavaPlugin,
	TriConsumer<I1, I2, Consumer<O>>
{}
