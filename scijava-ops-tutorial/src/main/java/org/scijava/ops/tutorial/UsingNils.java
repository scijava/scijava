
package org.scijava.ops.tutorial;

import org.scijava.function.Producer;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * ImageJ Ops2 allows users to take advantage of the strong type safety of the
 * Java language, which can provide benefits in certainty and efficiency. The
 * downside of this is verbosity, especially in obtaining generically-typed
 * outputs from OpBuilder calls.
 * <p>
 * Luckily, SciJava Ops allows you to specify the generic types of outputs using
 * the {@link Nil} type, as shown in this tutorial.
 *
 * @author Gabriel Selzer
 */
public class UsingNils implements OpCollection {

	/**
	 * This Op returns a 10x10 image of unsigned bytes
	 */
	@OpField(names = "tutorial.nils")
	public final Producer<Img<UnsignedByteType>> imgOfBytes = //
		() -> ArrayImgs.unsignedBytes(10, 10);

	/**
	 * This Op returns a 10x10 image of doubles
	 */
	@OpField(names = "tutorial.nils")
	public final Producer<Img<DoubleType>> imgOfDoubles = //
		() -> ArrayImgs.doubles(10, 10);

	public static void main(String... args) {
		OpEnvironment ops = OpEnvironment.getEnvironment();

		// The following is the syntax used to create a Nil that encodes the type we
		// want. Namely, if we want to ensure that the return is an Img<DoubleType>,
		// we put that type as the generic type of the Nil:
		Nil<Img<DoubleType>> outType = new Nil<>() {};

		// By passing a Nil as the output type, instead of as a class, we guarantee
		// the generic typing of the output. Note that the Nil allows us to
		// differentiate between the two Ops above, one of which would instead
		// return an ArrayImg of unsigned bytes.
		Img<DoubleType> out = ops.nullary("tutorial.nils").outType(outType)
			.create();

		System.out.println("Found an image " + out + " of doubles!");
	}

}
