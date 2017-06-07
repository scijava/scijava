package org.scijava.ops;

import java.util.function.Consumer;

import org.scijava.plugin.SciJavaPlugin;

/** Unary inplace computation. */
@FunctionalInterface
public interface InplaceOp<IO> extends SciJavaPlugin, Consumer<IO>
{
}