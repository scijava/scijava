package org.scijava.ops;

import org.scijava.plugin.SciJavaPlugin;

/** Binary computer. */
@FunctionalInterface
public interface BiComputerOp<I1, I2, O> extends SciJavaPlugin, TriConsumer<I1, I2, O>
{
}