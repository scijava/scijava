package net.imagej.ops2.tutorial;

import org.scijava.Context;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.types.Nil;

import io.scif.img.ImgOpener;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class OpsIntro {

	public static void main(String... args) {
		/*
		ImageJ Ops2 is a library for scientific image processing. The fundamental
		currency of this library are individual algorithms called Ops.

		All Ops share a few traits that establish a baseline of expected behavior:

		1. Ops are stateless - with no internal state, calling an Op two times on
		the same inputs will produce the same output.

		2. Ops are limited to one output - by limiting Ops to one output, we ensure
		that the algorithms we write are reusable building blocks. Of course,
		algorithms always need inputs, and there are no limitations on the number
		of inputs.

		3. Ops have a strong functional type - Java has a strong type system, and
		Ops utilizes this to maximize expressiveness and efficiency.

		4. Ops have at least one name - this name conveys an Op's purpose
		 */


		/*
		The OpEnvironment is the core of ImageJ Ops2. It is a collection of Ops,
		along with API for accessing Ops, adding new Ops, and other utilities.
		
		The easiest way to obtain an OpEnvironment is to use the following static
		method.
		*/
		OpEnvironment ops = OpEnvironment.getEnvironment();

		/*
		OpEnvironments contain Ops, and there are a lot of them.
		 */
		int numberOfOps = count(ops.infos());
		System.out.println("There are " + numberOfOps + " ops in the OpEnvironment");

		/*
		Because each Op has at least one name, we can search for all Ops by name:
		 */
		int numberOfGaussianOps = count(ops.infos("filter.gauss"));
		System.out.println("There are " + numberOfGaussianOps + " gaussian filters in the OpEnvironment");
		
		/*
		The OpEnvironment also provides an access point into the OpBuilder syntax.
		(For an explanation of the OpBuilder syntax, please see the OpBuilder
		tutorial).
		 */
		Double result = ops.binary("math.add") //
			.input(1., 1.) //
			.outType(Double.class) //
			.apply();
		System.out.println("1+1=" + result);

		// open an image and do ops with it
		var img = openLymp();
		var shape = new RectangleShape(5, false);
		var oobf = new OutOfBoundsBorderFactory<>();
		var uByteType = new Nil<Img<UnsignedByteType>>(){};
		var mean = ops.ternary("filter.mean").input(img, shape, oobf).outType(uByteType).apply();
		var multResult = ops.binary("create.img").input(img, new IntType()).apply();
		ops.binary("math.multiply").input(img, mean).output(multResult).compute();

		// To see information about available ops, we use the OpEnvironment#descriptions method
		// First, print all available ops
		System.out.println("--- All Ops!!! ---");
		ops.descriptions().stream().forEach(System.out::println);

		// Printing all the ops is a lot, so usually it's more helpful to check the
		// available signatures for a particular op:
		System.out.println("--- Available 'math.multiply' ops ---");
		ops.descriptions("math.multiply").stream().forEach(System.out::println);
	}

	/**
	 * Static utility function to count the number of elements in an
	 * {@link Iterable}.
	 * @param iterable the {@link Iterable} to enumerate
	 * @return the number of elements in {@code iterable}
	 */
	private static int count(Iterable<?> iterable) {
		int num = 0;
		for (Object ignored : iterable) {
			num++;
		}
		return num;
	}

	/**
	 * Helper method to open the lymp.tif ImageJ sample data. It encapsulates an IJ2 context.
	 *
	 * @return The opened image
	 */
	private static Img<?> openLymp() {
		Img<?> img = null;
		try (Context ctx = new Context()) {
			ImgOpener opener = new ImgOpener(ctx);
			img = opener.openImgs("https://imagej.net/images/lymp.tif").get(0).getImg();
		}
		return img;
	}
}
