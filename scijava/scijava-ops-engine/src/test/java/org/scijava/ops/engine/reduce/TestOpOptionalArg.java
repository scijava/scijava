
package org.scijava.ops.engine.reduce;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.Optional;

@OpClass(names = "test.optionalAdd")
public class TestOpOptionalArg implements
	Functions.Arity3<Double, Double, Double, Double>, Op
{

	/**
	 * @param t the first input
	 * @param u the second input
	 * @param v the third input
	 * @return t[+u[+v]]
	 */
	@Override
	public Double apply(Double t, @Optional Double u, @Optional Double v) {
		if (u == null) u = 0.;
		if (v == null) v = 0.;
		return t + u + v;
	}

}
