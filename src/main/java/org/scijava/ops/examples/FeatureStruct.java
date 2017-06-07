package org.scijava.ops.examples;

import org.scijava.param.Parameter;

/** Example data structure intended for use as ops inputs and/or outputs. */
public class FeatureStruct {

	@Parameter
	private long sum;
	
	@Parameter
	private long count;

	@Parameter
	private String label;

}
