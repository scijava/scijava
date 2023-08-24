
package org.scijava.ops.engine.yaml.impl.ops;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

/**
 * A final {@link Field}, exposed to Ops via YAML
 * 
 * @author Gabriel Selzer
 */
public class YAMLFieldOp {

	/**
	 * A Field Op
	 * @implNote op name=example.mul
	 * @input a the first double
	 * @input b the first double
	 * @output the product
	 */
	public final BiFunction<Double, Double, Double> multiply = (a, b) -> a * b;

}
