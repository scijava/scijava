
package net.imagej.ops2.image.histogram;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.types.Nil;

/**
 * Tests {@link HistogramCreate}
 * 
 * @author Gabe Selzer
 */
public class DefaultHistogramTest extends AbstractOpTest {

	byte[] data = { 96, 41, 2, 82, 43, 28, 76, 103, 96, 81, 121, 53, 115, 47, 119, 41, 4, 87, 102, 45, 63, 109, 70, 127,
			94, 97, 119, 88, 5, 45, 106, 66, 9, 50, 102, 76, 49, 51, 57, 127, 21, 33, 0, 127, 88, 21, 61, 99, 41, 113,
			117, 110, 88, 45, 64, 28, 18, 119, 91, 37, 86, 30, 45, 63 };

	/**
	 * Simple regression test. A lot of this is just testing {@link Histogram1d}
	 * however theoretically if {@link HistogramCreate} is changed it would reflect
	 * in the values.
	 */
	@Test
	public void testRegression() {
		Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(data, 8, 8);
		Histogram1d<UnsignedByteType> histogram = op("image.histogram").input(img, 10)
				.outType(new Nil<Histogram1d<UnsignedByteType>>() {}).apply();

		Assertions.assertEquals(false, histogram.hasTails());
		Assertions.assertEquals(0.078125, histogram.relativeFrequency(5, false), 0.00001d);
		Assertions.assertEquals(10, histogram.getBinCount());

		UnsignedByteType type = new UnsignedByteType();
		histogram.getLowerBound(5, type);
		Assertions.assertEquals(64.0, type.getRealDouble(), 0.00001d);
		histogram.getUpperBound(5, type);
		Assertions.assertEquals(76.0, type.getRealDouble(), 0.00001d);
	}

}
