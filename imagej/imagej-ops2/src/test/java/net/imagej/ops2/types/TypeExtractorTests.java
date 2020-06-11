package net.imagej.ops2.types;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsRandomValueFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.plugin.Plugin;
import org.scijava.types.TypeExtractor;

/**
 * Tests various {@link TypeExtractor}s.
 * 
 * @author Gabriel Selzer
 *
 */
@Plugin(type = OpCollection.class)
public class TypeExtractorTests extends AbstractOpTest {

	@OpField(names = "test.oobcvfTypeExtractor", params = "oobf, output")
	public final Function<OutOfBoundsConstantValueFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>>, String> func = (
			oobf) -> "oobcvf";

	@Test
	public void testOutOfBoundsConstantValueFactoryTypeExtractors() {
		OutOfBoundsFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>> oobf = new OutOfBoundsConstantValueFactory<>(
				new UnsignedByteType(5));

		String output = (String) ops.op("test.oobcvfTypeExtractor").input(oobf).apply();
		// make sure that output matches the return from the Op above, specific to the
		// type of OOBF we passed through.
		assert output.equals("oobcvf");
	}

	// Test Op returns a string different from the one above
	@OpField(names = "test.oobrvfTypeExtractor", params = "oobf, input, output")
	public final BiFunction<OutOfBoundsRandomValueFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>>, RandomAccessibleInterval<UnsignedByteType>, String> funcRandom = ( oobf, rai) -> "oobrvf"; 
	
	@Test public void testOutOfBoundsRandomValueFactoryTypeExtractors() { 
		OutOfBoundsFactory<UnsignedByteType, RandomAccessibleInterval<UnsignedByteType>> oobf = new OutOfBoundsRandomValueFactory<>( 
				new UnsignedByteType(7), 7, 7);
		Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(new long[] { 10, 10 }); 
		String output = (String) ops.op("test.oobrvfTypeExtractor").input(oobf, img).apply(); // make sure that output matches the return from the Op above, specific to the // type of OOBF we passed through. 
		assert output.equals("oobrvf"); } 
	}
