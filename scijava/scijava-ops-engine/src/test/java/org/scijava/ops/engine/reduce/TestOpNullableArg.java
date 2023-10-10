
package org.scijava.ops.engine.reduce;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;

@OpClass(names = "test.nullableAdd")
public class TestOpNullableArg implements
	Functions.Arity3<Double, Double, Double, Double>, Op
{

	/**
	 * @param t the first input
	 * @param u the second input
	 * @param v the third input
	 * @return t[+u[+v]]
	 */
	@Override
	public Double apply(Double t, @Nullable Double u, @Nullable Double v) {
		if (u == null) u = 0.;
		if (v == null) v = 0.;
		return t + u + v;
	}

}
