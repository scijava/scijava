
package net.imagej.ops2.tutorial;

import java.lang.reflect.Method;

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
 * allows marked "Optional" parameters to be omitted from Op requests, <b>in a
 * right-to-left order</b>. For example, if the rightmost parameter to an Op is
 * marked as Optional, then it can be provided, or omitted, in Op signatures. If
 * the rightmost <b>two</b> parameters are both marked as Optional, then both
 * can be provided, both can be omitted, or only the rightmost parameter can be
 * omitted.
 * <p>
 * Within Ops declaring Optional parameters, omitted parameters are given
 * {@code null} arguments. The Op is thus responsible for null-checking any
 * parameters it declares as Optional.
 * <p>
 * Below, we can see how this works by calling the above Method Op, normally
 * requiring three parameters, with only two parameters.
 */
public class OpReduction implements OpCollection {

	/**
	 * An {@link Method} annotated to be an Op.
	 * 
	 * @param in1 the first input
	 * @param in2 the second input. OPTIONAL.
	 * @param in3 the third input. OPTIONAL.
	 * @return the sum of the passed numbers.
	 */
	@OpMethod(names = "tutorial.reduce", type = Functions.Arity3.class)
	public static Double optionalMethod(Double in1, @Optional Double in2,
		@Optional Double in3)
	{
		// neither were given
		if (in2 == null && in3 == null) {
			System.out.println("Op called with in1");
			in2 = 0.;
			in3 = 0.;
		}
		// only in2 was given
		else if (in3 == null) {
			System.out.println("Op called with in1, in2");
			in3 = 0.;
		}
		// both were given
		else {
			System.out.println("Op called with in1, in2, in3");
		}
		// Then comes the normal computation
		return in1 + in2 + in3;
	}

	public static void main(String... args) {
		// Create the OpEnvironment
		OpEnvironment ops = new DefaultOpEnvironment();

		// Define some data
		Double first = 1.;
		Double second = 2.;
		Double third = 3.;

		// -- CALL WITH ALL THREE PARAMETERS -- //

		// Ask for an Op of name "tutorial.reduce"
		Double noOptional = ops.ternary("tutorial.reduce") //
			// With our two Double inputs
			.input(first, second, third) //
			// And get a Double out
			.outType(Double.class) //
			// Note that we can call this Op with only two arguments!
			.apply();

		// -- CALL WITH ONE OPTIONAL PARAMETER -- //

		// Ask for an Op of name "tutorial.reduce"
		Double oneOptional = ops.binary("tutorial.reduce") //
			// With our two Double inputs
			.input(first, second) //
			// And get a Double out
			.outType(Double.class) //
			// Note that we can call this Op with only two arguments!
			.apply();

		// -- CALL WITH TWO OPTIONAL PARAMETERS -- //

		// Ask for an Op of name "tutorial.reduce"
		Double twoOptional = ops.unary("tutorial.reduce") //
			// With our two Double inputs
			.input(first) //
			// And get a Double out
			.outType(Double.class) //
			// Note that we can call this Op with only two arguments!
			.apply();
	}

}
