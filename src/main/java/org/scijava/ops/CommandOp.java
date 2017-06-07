package org.scijava.ops;

import org.scijava.plugin.SciJavaPlugin;

@FunctionalInterface
public interface CommandOp<I, O> extends SciJavaPlugin, Runnable {}
