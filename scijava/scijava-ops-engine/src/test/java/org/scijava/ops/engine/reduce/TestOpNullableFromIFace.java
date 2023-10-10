
package org.scijava.ops.engine.reduce;

import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;

@OpClass(names = "test.nullableOr")
public class TestOpNullableFromIFace implements //
		BiFunctionWithNullable<Boolean, Boolean, Boolean, Boolean>, Op
{

	/**
	 * @param in1 the first input
	 * @param in2 the second input
	 * @param in3 the third input
	 * @return the output
	 */
	@Override
	public Boolean apply(Boolean in1, Boolean in2, Boolean in3) {
		if (in3 == null) in3 = false;
		return in1 | in2 | in3;
	}

}
