package org.scijava.ops;

@FunctionalInterface
public interface OutputAware<I, O> {
	O createOutput(I in);
}
