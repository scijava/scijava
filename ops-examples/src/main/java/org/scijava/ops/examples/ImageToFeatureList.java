
package org.scijava.ops.examples;

import org.scijava.ops.FunctionOp;
import org.scijava.param.Parameter;
import org.scijava.util.DoubleArray;

/**
 * Takes a list of doubles as inputs; produces some statistics as output.
 */
public class ImageToFeatureList implements
	FunctionOp<DoubleArray, ImageToFeatureList.Features>
{

	@Parameter
	private double multiplier;

	@Override
	public Features apply(final DoubleArray t) {
		Features result = new Features();
		t.forEach(e -> result.sum += e);
		result.sum *= multiplier;
		result.count = t.size();
		result.label = "Great";
		return result;
	}

	/** Example data structure intended for use as ops inputs and/or outputs. */
	public static class Features {

		@Parameter
		public long sum;

		@Parameter
		public long count;

		@Parameter
		public String label;

	}

}
