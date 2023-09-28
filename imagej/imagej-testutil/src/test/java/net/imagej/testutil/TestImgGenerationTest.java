package net.imagej.testutil;

import net.imglib2.Cursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestImgGenerationTest {

	@Test
	public void verifyImages() {
		ensureNotEmpty(TestImgGeneration.byteArray(true, 10, 10), new ByteType());
		ensureNotEmpty(TestImgGeneration.unsignedByteArray(true, 10, 10), new UnsignedByteType());
		ensureNotEmpty(TestImgGeneration.intArray(true, 10, 10), new IntType());
		ensureNotEmpty(TestImgGeneration.unsignedIntArray(true, 10, 10), new UnsignedIntType());
		ensureNotEmpty(TestImgGeneration.floatArray(true, 10, 10), new FloatType());
		ensureNotEmpty(TestImgGeneration.doubleArray(true, 10, 10), new DoubleType());
		ensureNotEmpty(TestImgGeneration.bitArray(true, 10, 10), new BitType());
		ensureNotEmpty(TestImgGeneration.longArray(true, 10, 10), new LongType());
		ensureNotEmpty(TestImgGeneration.unsignedLongArray(true, 10, 10), new UnsignedLongType());
		ensureNotEmpty(TestImgGeneration.shortArray(true, 10, 10), new ShortType());
		ensureNotEmpty(TestImgGeneration.unsignedShortArray(true, 10, 10), new UnsignedShortType());
		ensureNotEmpty(TestImgGeneration.unsigned2BitArray(true, 10, 10), new Unsigned2BitType());
		ensureNotEmpty(TestImgGeneration.unsigned4BitArray(true, 10, 10), new Unsigned4BitType());
		ensureNotEmpty(TestImgGeneration.unsigned12BitArray(true, 10, 10), new Unsigned12BitType());
		ensureNotEmpty(TestImgGeneration.unsigned128BitArray(true, 10, 10), new Unsigned128BitType());
	}

	private void ensureNotEmpty(ArrayImg img, NativeType nt) {
				Cursor<?> cursor = img.cursor();
				boolean foundNonZero = false;
				while (cursor.hasNext()) {
					foundNonZero = (!cursor.next().equals(nt)) || foundNonZero;
				}
				assertTrue(foundNonZero, "Randomly generated " + nt.getClass() + " array contains all 0's");
	}
}
