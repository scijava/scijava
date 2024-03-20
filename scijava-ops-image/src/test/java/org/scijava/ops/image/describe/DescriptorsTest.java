
package org.scijava.ops.image.describe;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

public class DescriptorsTest extends AbstractOpTest {

	/**
	 * @implNote op name=example.describeRealType, type=Inplace
	 */
	public static <T extends RealType<T>> void realType(T in) {
		in.mul(in);
	}

	@Test
	public void testRealTypeDescription() {
		var expected = "example.describeRealType:\n" +
			"\t- (@MUTABLE number) -> None";
		var actual = ops.help("example.describeRealType");
		Assertions.assertEquals(expected, actual);
	}

	/**
	 * @implNote op name=example.describeComplexType, type=Inplace
	 */
	public static <T extends ComplexType<T>> void complexType(T in) {
		in.mul(in);
	}

	@Test
	public void testComplexTypeDescription() {
		var expected = "example.describeComplexType:\n" +
			"\t- (@MUTABLE complex number) -> None";
		var actual = ops.help("example.describeComplexType");
		Assertions.assertEquals(expected, actual);
	}

	/**
	 * @implNote op name=example.describeRAI, type=Inplace
	 */
	public static <T extends RealType<T>> void randomAccessibleInterval(
		RandomAccessibleInterval<T> in)
	{
		LoopBuilder.setImages(in).forEachPixel(i -> i.mul(i));
	}

	@Test
	public void testRAIDescription() {
		var expected = "example.describeRAI:\n" + "\t- (@MUTABLE image) -> None";
		var actual = ops.help("example.describeRAI");
		Assertions.assertEquals(expected, actual);
	}

	/**
	 * @implNote op name=example.describeII, type=Inplace
	 */
	public static <T extends RealType<T>> void iterableInterval(
		IterableInterval<T> in)
	{
		var c = in.cursor();
		while (c.hasNext()) {
			var i = c.next();
			i.mul(i);
		}
	}

	@Test
	public void testIIDescription() {
		var expected = "example.describeII:\n" + "\t- (@MUTABLE image) -> None";
		var actual = ops.help("example.describeII");
		Assertions.assertEquals(expected, actual);
	}

	/**
	 * @implNote op name=example.describeImgLabeling, type=Inplace
	 */
	public static <T, I extends IntegerType<I>> void imgLabeling(
		ImgLabeling<T, I> in)
	{}

	@Test
	public void testImgLabelingDescription() {
		var expected = "example.describeImgLabeling:\n" +
			"\t- (@MUTABLE labels) -> None";
		var actual = ops.help("example.describeImgLabeling");
		Assertions.assertEquals(expected, actual);
	}

}
