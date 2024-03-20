
package org.scijava.ops.image.describe;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import org.scijava.types.Nil;

import java.util.Random;
import java.util.function.Function;

/**
 * {@code engine.describe} Ops pertaining to ImgLib2 types.
 * <p>
 * Note the heavy use of wildcards which provides extensibility, as it allows
 * e.g. {@link #raiDesc(Nil)} to be used for e.g.
 * {@link net.imglib2.img.array.ArrayImg}s
 * </p>
 *
 * @author Gabriel Selzer
 */
public class ImgLib2Descriptors {

	/**
	 * @param inType the type (some {@link IterableInterval}) subclass to describe
	 * @return the description
	 * @implNote op name="engine.describe", priority='-100.'
	 */
	public static String iiDesc( //
		Nil<? extends IterableInterval<?>> inType //
	) {
		return "image";
	}

	/**
	 * @param inType the type (some {@link RandomAccessibleInterval} subclass) to
	 *          describe
	 * @return the description
	 * @implNote op name="engine.describe"
	 */
	public static String raiDesc( //
		Nil<? extends RandomAccessibleInterval<?>> inType //
	) {
		return "image";
	}

	/**
	 * @param inType the type (some {@link ImgLabeling} subclass) to describe
	 * @return the description
	 * @implNote op name="engine.describe", priority='100.'
	 */
	public static String labelDesc(Nil<? extends ImgLabeling<?, ?>> inType) {
		return "labeling";
	}

	/**
	 * @param inType the type (some {@link RealType} subclass) to describe
	 * @return the description
	 * @implNote op name="engine.describe"
	 */
	public static String realTypeDesc( //
		Nil<? extends RealType<?>> inType //
	) {
		return "number";
	}

	/**
	 * @param inType the type (some {@link ComplexType} subclass) to describe
	 * @return the description
	 * @implNote op name="engine.describe", priority='-100.'
	 */
	public static String complexTypeDesc( //
		Nil<? extends ComplexType<?>> inType //
	) {
		return "complex-number";
	}
}
