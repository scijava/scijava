
package net.imagej.ops2.tutorial;

import org.scijava.function.Functions;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.engine.DefaultOpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;
import org.scijava.ops.spi.Optional;

/**
 * One powerful feature of SciJava Ops is the ability to transform individual
 * Ops to match user requests. In this way, SciJava Ops can satisfy many Op
 * calls with just one implementation.
 * <p>
 * Another transformation is called "reduction". This type of transformation
 * allows marked "Optional" parameters to be omitted from Op requests, <b>in
 * a right-to-left order</b>. For example, if the rightmost parameter to an Op
 * is marked as Optional, then it can be provided, or omitted, in Op signatures.
 * If the rightmost <b>two</b> parameters are both marked as Optional, then
 * both can be provided, both can be omitted, or only the rightmost parameter
 * can be omitted.
 * <p>
 * Within Ops declaring Optional parameters, omitted parameters are given
 * {@code null} arguments. The Op is thus responsible for null-checking any
 * parameters it declares as Optional.
 * <p>
 * Below, we can see how this works by calling the above Method Op, normally
 * requiring three parameters, with only two parameters.
 */
public class OpReduction implements OpCollection {

	@OpMethod(names = "tutorial.reduce", type = Functions.Arity3.class)
	public static Double optionalMethod(Double in1, Double in2, @Optional Double in3) {
		if (in3 == null) in3 = 0.;
		return in1 + in2 + in3;
	}

	public static void main(String... args) {
		// Create the OpEnvironment
		OpEnvironment ops = new DefaultOpEnvironment();
		// Call the Op on some inputs
		Double first = 1.;
		Double second = 2.;
		// Ask for an Op of name "tutorial.reduce"
		Double resultWithout = ops.op("tutorial.reduce") //
			// With our two Double inputs
			.input(first, second, second) //
			// And get a Double out
			.outType(Double.class) //
			// Note that we can call this Op with only two arguments!
			.apply();

		System.out.println(resultWithout);

	}

}
