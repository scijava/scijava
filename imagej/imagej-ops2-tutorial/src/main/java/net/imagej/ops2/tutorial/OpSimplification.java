
package net.imagej.ops2.tutorial;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.engine.DefaultOpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

/**
 * One powerful feature of SciJava Ops is the ability to transform individual
 * Ops to match user requests. In this way, SciJava Ops can satisfy many Op
 * calls with just one implementation. The simplest type of transformation is
 * showcased in {@link OpAdaptation}.
 * <p>
 * A more complex type of transformation is called "simplification". This type
 * involves transforming some subset of Op inputs into a different, but similar
 * type. This process makes use of two different Op types:
 * <ul>
 * <li>"simplify" Ops transform user inputs into a broader data type</li>
 * <li>"focus" Ops transform that broader type into the type used by the Op</li>
 * </ul>
 * functional type X and returning an Op of functional type Y.
 * <p>
 * Adaptation can be used to call a Function like a Computer, or to call an Op
 * that operates on Doubles like an Op that operates on a List of Doubles.
 * <p>
 * Below, we can see how this works by calling the above Field Op, supposed to
 * work on Doubles, on an array of Doubles[]
 */
public class OpSimplification implements OpCollection {

	/**
	 * A simple Op, written as a {@link Field}, that performs a simple
	 * calculation.
	 */
	@OpField(names = "tutorial.simplify")
	public final BiFunction<Double, Double, Double> fieldOp = (a, b) -> {
		return a * 2 + b;
	};

	public static void main(String... args) {
		// Create the OpEnvironment
		OpEnvironment ops = new DefaultOpEnvironment();
		// Call the Op on some inputs
		Integer first = 1;
		Integer second = 2;
		// Ask for an Op of name "tutorial.simplify"
		Integer result = ops.binary("tutorial.simplify") //
			// With our two Integer inputs
			.input(first, second) //
			// And get an Integer out
			.outType(Integer.class) //
			// Note that we can call this Op on Integers, even though it is written
			// for Doubles!
			.apply();

		/*
		The simplification routine works as follows:
		1. SciJava Ops determines that there are no existing "tutorial.simplify" Ops
		that work on Integers
		
		2. SciJava Ops finds a "focus" Op that can make Doubles from Numbers. Thus,
		if the inputs are Numbers, SciJava Ops could use this "focus" Op to make
		those Numbers into Doubles
		
		3. SciJava Ops finds a "simplify" Op that can make Numbers from Integers.
		Thus, if the inputs are Integers, SciJava Ops can use this "simplify" Op
		to make those Integers into Numbers
		
		4. By using the "simplify" and then the "focus" Op, SciJava Ops can convert
		the Integer inputs into Double inputs, and by creating a similar chain
		in reverse, can convert the Double output into an Integer output.
		Thus, we get an Op that can run on Integers!
		 */
		System.out.println(result);

	}

}
