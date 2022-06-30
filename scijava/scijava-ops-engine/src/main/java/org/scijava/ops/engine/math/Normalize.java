package org.scijava.ops.engine.math;

import java.util.Arrays;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;

public class Normalize {

	public static final String NAMES = "math.minmax";

	@OpClass(names = NAMES)
	public static class MathMinMaxNormalizeFunction implements Functions.Arity3<double[], Double, Double, double[]>, Op {

			/**
		 * TODO
		 * 
		 * @param t
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
