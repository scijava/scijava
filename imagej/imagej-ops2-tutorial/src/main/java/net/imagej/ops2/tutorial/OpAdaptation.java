
package net.imagej.ops2.tutorial;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

/**
 * One powerful feature of SciJava Ops is the ability to transform individual
 * Ops to match user requests. In this way, SciJava Ops can satisfy many
 * different Op calls with just one implementation.
 * <p>
 * The simplest type of transformation is termed "adaptation". Adaptation
 * involves transforming an Op into a different functional type. A set of
 * "adapt" Ops exist to perform this transformation, taking in any Op of
 * functional type X and returning an Op of functional type Y.
 * <p>
 * Adaptation can be used to call a Function like a Computer, or to call an Op
 * that operates on Doubles like an Op that operates on a List of Doubles.
 * <p>
 * Below, we can see how this works by calling the above Field Op, supposed to
 * work on Doubles, on an array of Doubles[]
 */
public class OpAdaptation implements OpCollection {

	/**
	 * A simple Op, written as a {@link Field}, that performs a simple
	 * calculation.
	 */
	@OpField(names = "tutorial.adapt")
	public final BiFunction<Double, Double, Double> fieldOp = (a, b) -> {
		return a * 2 + b;
	};

	public static void main(String... args) {
		// Create the OpEnvironment
		OpEnvironment ops = OpEnvironment.getEnvironment();
		// Call the Op on some inputs
		Double[] firstArray = new Double[] { 1., 2., 3. };
		Double[] secondArray = new Double[] { 1., 2., 3. };
		// Ask for an Op of name "tutorial.adapt"
		Double[] result = ops.binary("tutorial.adapt") //
			// With our two ARRAY inputs
			.input(firstArray, secondArray) //
			// And get a Double[] out
			.outType(Double[].class) //
			// Note that we can call this Op on arrays, even though it wasn't
			// meant to be!
			.apply();
		System.out.println(result);

	}

}
