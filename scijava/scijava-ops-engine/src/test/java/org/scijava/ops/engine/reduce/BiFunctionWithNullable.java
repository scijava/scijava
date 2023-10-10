
package org.scijava.ops.engine.reduce;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;

@FunctionalInterface
public interface BiFunctionWithNullable<I1, I2, I3, O> extends
	Functions.Arity3<I1, I2, I3, O>
{

	@Override
	O apply(I1 in1, I2 in2, @Nullable I3 in3);
}
