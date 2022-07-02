
package org.scijava.discovery.test;

import java.util.function.BiFunction;

import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class ServiceBasedMultipliers implements OpCollection {

	@OpField(names = "math.multiply")
	public final BiFunction<Number, Number, Double> fieldMultiplier = (in1,
		in2) -> in1.doubleValue() * in2.doubleValue();

}
