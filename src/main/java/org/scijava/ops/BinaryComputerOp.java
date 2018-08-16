package org.scijava.ops;

@FunctionalInterface
public interface BinaryComputerOp<I1, I2, O> {
	void compute( I1 in1, I2 in2, O out );
}
