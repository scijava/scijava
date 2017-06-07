package org.scijava.ops;

import java.util.function.BiConsumer;

import org.scijava.plugin.SciJavaPlugin;

/** Binary inplace computation mutating the second input. */
@FunctionalInterface
public interface BiInplace2Op<I1, IO> extends SciJavaPlugin, BiConsumer<I1, IO>
{
}