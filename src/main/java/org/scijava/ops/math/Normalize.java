package org.scijava.ops.math;

import java.util.Arrays;
import java.util.function.Function;

import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class Normalize {

	public static final String NAMES = "math.minmax";

	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "numbers")
	@Parameter(key = "normalized", itemIO = ItemIO.OUTPUT)
	public static class MathMinMaxNormalizeFunction implements Function<double[], double[]> {

		@Parameter(itemIO=ItemIO.INPUT)
		private Double newMin;
		
		@Parameter(itemIO=ItemIO.INPUT, required = false)
		private Double newMax;

		@Override
		public double[] apply(double[] t) {
			if (newMax == null) {
				newMax = 1.0;
			}
			if (newMin >= newMax) {
				throw new IllegalStateException("Min must be smaller than max.");
			}
			
			double min = Arrays.stream(t).min().getAsDouble();
			double max = Arrays.stream(t).max().getAsDouble();
			
			return Arrays.stream(t).map(d -> norm(d, min, max)).toArray();
		}
		
		private double norm(double d, double dataMin, double dataMax) {
			return newMin + (((d - dataMin)*(newMax - newMin))/(dataMax - dataMin));
		}
	}

	
}
