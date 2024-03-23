
package org.scijava.ops.benchmarks.matching;

import net.imglib2.img.array.ArrayImgs;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpEnvironment;

/**
 * A simple test ensuring that the Ops used in benchmarks get matched.
 *
 * @author Gabriel Selzer
 */
public class BenchmarkingOpsTest {

	@Test
	public void testImageConversion() {
		OpEnvironment env = OpEnvironment.build();
		var simpleIn = ArrayImgs.bytes(1000, 1000);
		var out = ArrayImgs.bytes(simpleIn.dimensionsAsLongArray());
		env.binary("benchmark.match") //
			.input(simpleIn, (byte) 1) //
			.output(out) //
			.compute();
	}

	@Test
	public void testImageConversionAdaptation() {
		OpEnvironment env = OpEnvironment.build();
		var simpleIn = ArrayImgs.bytes(1000, 1000);
		env.binary("benchmark.match") //
			.input(simpleIn, 1.0) //
			.apply();
	}
}
