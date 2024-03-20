
package org.scijava.ops.image.describe;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import org.scijava.types.Nil;

import java.util.function.Function;

public class ImgLib2Descriptors< //
		A, //
		C extends ComplexType<C>, //
		I extends IntegerType<I>, //
		T extends RealType<T> //
> {

	/**
	 * @input the type to describe
	 * @output the description
	 * @implNote op name="engine.describe"
	 */
	public final Function<Nil<IterableInterval<A>>, String> iiDesc = //
		in -> "image";

	/**
	 * @input the type to describe
	 * @output the description
	 * @implNote op name="engine.describe", priority="100."
	 */
	public final Function<Nil<RandomAccessibleInterval<A>>, String> raiDesc = //
		in -> "image";

	/**
	 * @input the type to describe
	 * @output the description
	 * @implNote op name="engine.describe", priority="1000."
	 */
	public final Function<Nil<ImgLabeling<A, I>>, String> imgLabelDesc = //
		in -> "labels";

	/**
	 * @input the type to describe
	 * @output the description
	 * @implNote op name="engine.describe", priority='100.'
	 */
	public final Function<Nil<T>, String> realTypeDesc = //
		in -> "number";

	/**
	 * @input the type to describe
	 * @output the description
	 * @implNote op name="engine.describe"
	 */
	public final Function<Nil<C>, String> complexTypeDesc = //
		in -> "complex number";
}
