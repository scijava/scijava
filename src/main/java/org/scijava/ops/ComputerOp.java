package org.scijava.ops;

import java.util.function.BiConsumer;

import org.scijava.plugin.SciJavaPlugin;

/** Unary computer. */
@FunctionalInterface
public interface ComputerOp<I, O> extends SciJavaPlugin, BiConsumer<I, O>
{
}