
package org.scijava.ops;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.scijava.plugin.SciJavaPlugin;

/** (flat)map -- 1->N */
@FunctionalInterface
public interface MapOp<I, O> extends SciJavaPlugin,
	BiConsumer<I, Consumer<O>>
{}
