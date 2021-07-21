package org.scijava.ops.math;

import java.util.Arrays;

import org.scijava.function.Functions;
import org.scijava.ops.api.Op;
import org.scijava.plugin.Plugin;

public class Normalize {

	public static final String NAMES = "math.minmax";

	@Plugin(type = Op.class, name = NAMES)
	public static class MathMinMaxNormalizeFunction implements Functions.Arity3<double[], Double, Double, double[]> {

			/**
		 * TODO
		 * 
		 * @param numbers
		 * @param newMin
		 * @param newMax
		 */
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
