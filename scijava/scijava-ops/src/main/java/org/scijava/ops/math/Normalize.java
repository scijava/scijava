package org.scijava.ops.math;

import java.util.Arrays;

import org.scijava.ops.core.Op;
import org.scijava.functions.Functions;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class Normalize {

	public static final String NAMES = "math.minmax";

	@Plugin(type = Op.class, name = NAMES)
	@Parameter(key = "numbers")
	@Parameter(key = "newMin")
	@Parameter(key = "newMax")
	@Parameter(key = "normalized")
	public static class MathMinMaxNormalizeFunction implements Functions.Arity3<double[], Double, Double, double[]> {

		@Override
		public double[] apply(double[] t, Double newMin, Double newMax) {
			if (newMax == null) {
				newMax = 1.0;
			}
			if (newMin >= newMax) {
				throw new IllegalStateException("Min must be smaller than max.");
			}
			
			double min = Arrays.stream(t).min().getAsDouble();
			double max = Arrays.stream(t).max().getAsDouble();
			double nMin = newMin;
			double nMax = newMax;
			
			return Arrays.stream(t).map(d -> norm(d, min, max, nMin, nMax)).toArray();
		}
		
		private double norm(double d, double dataMin, double dataMax, double newMin, double newMax) {
			return newMin + (((d - dataMin)*(newMax - newMin))/(dataMax - dataMin));
		}
	}

	
}
