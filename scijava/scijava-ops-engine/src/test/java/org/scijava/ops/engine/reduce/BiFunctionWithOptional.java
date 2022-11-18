
package org.scijava.ops.engine.reduce;

import org.scijava.function.Functions;
import org.scijava.ops.spi.Optional;

@FunctionalInterface
public interface BiFunctionWithOptional<I1, I2, I3, O> extends
	Functions.Arity3<I1, I2, I3, O>
{

	@Override
	O apply(I1 in1, I2 in2, @Optional I3 in3);
}
