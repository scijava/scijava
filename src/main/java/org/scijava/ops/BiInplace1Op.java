package org.scijava.ops;

import java.util.function.BiConsumer;

import org.scijava.plugin.SciJavaPlugin;

/** Binary inplace computation mutating the first input. */
@FunctionalInterface
public interface BiInplace1Op<IO, I2> extends SciJavaPlugin, BiConsumer<IO, I2>
{
}