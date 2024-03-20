
package org.scijava.ops.image.describe;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;
import org.scijava.types.Types;

import java.lang.reflect.ParameterizedType;

/**
 * Tests {@link ImgLib2Descriptors}.
 *
 * @author Gabriel Selzer
 */
public class ImgLib2DescriptorsTest extends AbstractOpTest {

	/**
	 * @implNote op name=test.describeRealType, type=Inplace
	 */
	public static <T extends RealType<T>> void realType(T in) {
		in.mul(in);
	}

	@Test
	public void testRealTypeDescription() {
		var expected = "test.describeRealType:\n" + "\t- (@MUTABLE number) -> None";
		var actual = ops.help("test.describeRealType");
		Assertions.assertEquals(expected, actual);
	}

	/**
	 * @implNote op name=test.describeComplexType, type=Inplace
	 */
	public static <T extends ComplexType<T>> void complexType(T in) {
		in.mul(in);
	}

	@Test
	public void testComplexTypeDescription() {
		var expected = "test.describeComplexType:\n" +
			"\t- (@MUTABLE complex-number) -> None";
		var actual = ops.help("test.describeComplexType");
		Assertions.assertEquals(expected, actual);
	}

	/**
	 * @implNote op name=test.RAImutator, type=Inplace
	 */
	public static <T extends RealType<T>> void randomAccessibleInterval(
		RandomAccessibleInterval<T> in)
	{
		LoopBuilder.setImages(in).forEachPixel(i -> i.mul(i));
	}

	@Test
	public void testRAIDescription() {
		var expected = "test.RAImutator:\n" + "\t- (@MUTABLE image) -> None";
		var actual = ops.help("test.RAImutator");
		Assertions.assertEquals(expected, actual);
	}

	/**
	 * @implNote op name=test.IIMutator, type=Inplace
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
		var expected = "test.IIMutator:\n" + "\t- (@MUTABLE image) -> None";
		var actual = ops.help("test.IIMutator");
		Assertions.assertEquals(expected, actual);
	}

	/**
	 * @implNote op name=test.ImgLabelingMutator, type=Inplace
	 */
	public static <T, I extends IntegerType<I>> void imgLabeling(
		ImgLabeling<T, I> in)
	{}

	@Test
	public void testImgLabelingDescription() {
		var expected = "test.ImgLabelingMutator:\n" +
			"\t- (@MUTABLE labeling) -> None";
		var actual = ops.help("test.ImgLabelingMutator");
		Assertions.assertEquals(expected, actual);
	}

	/**
	 * @implNote op name=test.ArrayImgMutator, type=Inplace
	 */
	public static <T extends NativeType<T>, A extends DataAccess> void arrayImg(
		ArrayImg<T, A> in)
	{}

	/**
	 * This test ensures description extensibility for ImgLib2 image types
	 */
	@Test
	public void testArrayImgDescription() {
		// First, ensure there is no descriptor FOR ArrayImgs
		for (var info : ops.infos("engine.describe")) {
			var in = info.inputTypes().get(0);
			Assertions.assertInstanceOf(ParameterizedType.class, in);
			var descriptorType = ((ParameterizedType) in).getActualTypeArguments()[0];
			Assertions.assertFalse( //
				Types.isAssignable(descriptorType, ArrayImg.class) //
			);
		}

		// Then, ensure that we get a description anyways
		var expected = "test.ArrayImgMutator:\n" + "\t- (@MUTABLE image) -> None";
		var actual = ops.help("test.ArrayImgMutator");
		Assertions.assertEquals(expected, actual);
	}

}
