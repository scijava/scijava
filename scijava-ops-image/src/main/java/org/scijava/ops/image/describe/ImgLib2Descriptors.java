
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

/**
 * {@code engine.describe} Ops pertaining to ImgLib2 types.
 * <p>
 * Note that each input is a {@code Nil<T>}, where {@code T} is a type variable
 * bounded by the type we actually want to describe. This provides
 * extensibility, as it allows e.g. {@link #raiDesc(Nil)} to be used for e.g.
 * {@link net.imglib2.img.array.ArrayImg}s
 * </p>
 *
 * @author Gabriel Selzer
 */
public class ImgLib2Descriptors {

	/**
	 * @param in the type to describe
	 * @return the description
	 * @implNote op name="engine.describe"
	 */
	public static <A, T extends IterableInterval<A>> String iiDesc( //
		Nil<T> in //
	) {
		return "image";
	}

	/**
	 * @param in the type to describe
	 * @return the description
	 * @implNote op name="engine.describe", priority='100.'
	 */
	public static <A, T extends RandomAccessibleInterval<A>> String raiDesc( //
		Nil<T> in //
	) {
		return "image";
	}

	/**
	 * @param in the type to describe
	 * @return the description
	 * @implNote op name="engine.describe", priority='10000.'
	 */
	public static <A, I extends IntegerType<I>, T extends ImgLabeling<A, I>>
		String labelDesc(Nil<T> in)
	{
		return "labels";
	}

	/**
	 * @param in the type to describe
	 * @return the description
	 * @implNote op name="engine.describe", priority='100.'
	 */
	public static <T extends RealType<T>> String realTypeDesc( //
		Nil<T> in //
	) {
		return "number";
	}

	/**
	 * @param in the type to describe
	 * @return the description
	 * @implNote op name="engine.describe"
	 */
	public static <T extends ComplexType<T>> String complexTypeDesc( //
		Nil<T> in //
	) {
		return "complex number";
	}
}
