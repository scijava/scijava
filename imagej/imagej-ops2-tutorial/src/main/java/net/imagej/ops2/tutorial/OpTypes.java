
package net.imagej.ops2.tutorial;

import java.util.function.BiFunction;

import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.types.Nil;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.type.logic.BitType;

/**
 * A tutorial describing the major flavors of Ops
 *
 * @author Gabriel Selzer
 */
public class OpTypes {

	public static void main(String... args) {
		/*
		While there are no restrictions on the FunctionalInterface that an Op,
		implements, there are three main categories used by the majority of Ops.
		
		We will showcase each of the types below.
		 */
		OpEnvironment ops = OpEnvironment.getEnvironment();

		/**
		 * The most basic category of Ops are Functions.
		 *
		 * Function Ops encapsulate their functionality within a method named "apply",
		 * which takes in some number of inputs and returns a single output.
		 */
		@OpClass(names = "tutorial.add")
		class SampleFunction implements Op, BiFunction<Double, Double, Double> {

			@Override
			public Double apply(Double d1, Double d2) {
				return d1 + d2;
			}
		}

		ops.registerInfosFrom(SampleFunction.class);

		BiFunction<Double, Double, Double> sumOp = ops //
			// Look for a "math.add" op
			.binary("tutorial.add") //
			// There should be two Double inputs
			.inType(Double.class, Double.class) //
			// And one Double output
			.outType(Double.class) //
			// And we want back the function
			.function(); //

		/*
		To execute a Function, call the "apply" method with our two Double inputs.
		 */
		Double onePlusTwo = sumOp.apply(1., 2.);
		System.out.println("One plus Two is " + onePlusTwo);

		/*
		Computer Ops differ from Functions in that Computers require the user
		pass a pre-allocated output, which will then be populated by the Op.

		One situation where a Computer can be more useful than a Function is when
		you plan to call the Computer many times - if the output takes time to
		create, reusing a single output, if possible, can improve performance.

		The request for a Computer is very similar to the request for a Function:
		 */
		Computers.Arity2<double[], Double, double[]> powerOp = ops //
				// Look for a "math.power" Op
				.binary("math.power") //
				//
				.inType(double[].class, Double.class) //
				.outType(double[].class) //
				.computer();

		double[] bases = new double[] {1, 2, 3, 4};
		double exponent = 2.;
		double[] powers = new double[4];

		/*
		Inplace Ops improve on the performance of Computers by directly mutating
		one of the inputs - you don't even need to create an output!

		Of course, you will lose your input data in the process.

		The request for an Inplace is very similar to the request for the others.
		Note that the INDEX of the mutable input is specified in the
		 */

		Inplaces.Arity4_1<RandomAccessibleInterval<BitType>, Interval, Shape, Integer> openOp =
		ops.quaternary("morphology.open") //
				.inType(new Nil<RandomAccessibleInterval<BitType>>() {}, Nil.of(Interval.class), Nil.of(Shape.class), Nil.of(Integer.class)).inplace1();

	}
}
