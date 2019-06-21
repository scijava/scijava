package net.imagej.ops.types;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsRandomValueFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.types.TypeExtractor;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Tests various {@link TypeExtractor}s.
 * 
 * @author Gabriel Selzer
 *
 */
@Plugin(type = OpCollection.class)
public class TypeExtractorTests extends AbstractOpTest {

	@OpField(names = "test.oobcvfTypeExtractor")
	@Parameter(key = "oobf")
	@Parameter(key = "output", type = ItemIO.BOTH)
	private Function<OutOfBoundsConstantValueFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>>, UnsignedByteType> func = (
			oobf) -> oobf.getValue();

	@Test
	public void testOutOfBoundsConstantValueFactoryTypeExtractors() {
		OutOfBoundsFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>> oobf = new OutOfBoundsConstantValueFactory<>(
				new UnsignedByteType(5));

		UnsignedByteType output = (UnsignedByteType) ops.run("test.oobcvfTypeExtractor", oobf);
		assert output.getRealDouble() == 5;
	}

	@OpField(names = "test.oobrvfTypeExtractor")
	@Parameter(key = "oobf")
	@Parameter(key = "output", type = ItemIO.BOTH)
	private BiFunction<OutOfBoundsRandomValueFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>>, RandomAccessibleInterval<UnsignedByteType>, UnsignedByteType> funcRandom = (
			oobf, rai) -> oobf.create(rai).get();

	@Test
	public void testOutOfBoundsRandomValueFactoryTypeExtractors() {
		OutOfBoundsFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>> oobf = new OutOfBoundsRandomValueFactory<>(new UnsignedByteType(7), 7, 7);

		Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(new long[] {10, 10});
		UnsignedByteType output = (UnsignedByteType) ops.run("test.oobrvfTypeExtractor", oobf, img);
		assert output.getRealDouble() == 7;
	}

}
