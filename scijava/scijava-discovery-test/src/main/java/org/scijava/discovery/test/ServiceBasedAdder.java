
package org.scijava.discovery.test;

import java.util.function.BiFunction;

import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;

@OpClass(names = "math.add")
public class ServiceBasedAdder implements BiFunction<Number, Number, Double>,
	Op
{

	@Override
	public Double apply(Number t, Number u) {
		return t.doubleValue() + u.doubleValue();
	}

}
