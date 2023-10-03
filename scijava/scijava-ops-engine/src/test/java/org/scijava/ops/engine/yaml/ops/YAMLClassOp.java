
package org.scijava.ops.engine.yaml.ops;

import java.util.function.BiFunction;

import org.scijava.ops.api.OpEnvironment;

/**
 * An example Op, registered by YAML into the {@link OpEnvironment}
 * @author Gabriel Selzer
 * @implNote op names=example.add, priority=100.0
 */
public class YAMLClassOp implements BiFunction<Double, Double, Double> {

	/**
	 *
	 * @param aDouble the first input
	 * @param aDouble2 the second input
	 * @return the result
	 */
	@Override
	public Double apply(Double aDouble, Double aDouble2) {
		return aDouble + aDouble2;
	}

	/**
	 * An example inner class Op, registered by YAML into the {@link OpEnvironment}
	 * @implNote op names=example.div
	 * @author Gabriel Selzer
	 * @author Daffy Duck
	 */
	public static class YAMLInnerClassOp implements
		BiFunction<Double, Double, Double>
	{

		/**
		 *
		 * @param aDouble the first input
		 * @param aDouble2 the second input
		 * @return the result
		 */
		@Override
		public Double apply(Double aDouble, Double aDouble2) {
			return aDouble / aDouble2;
		}
	}
}
