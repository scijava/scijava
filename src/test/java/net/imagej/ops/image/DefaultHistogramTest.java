
package net.imagej.ops.image;

import net.imagej.ops.AbstractOpTest;
import net.imagej.ops.image.histogram.HistogramCreate;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link HistogramCreate}
 * 
 * @author Gabe Selzer
 */
public class DefaultHistogramTest extends AbstractOpTest {

	byte[] data = { 96, 41, 2, 82, 43, 28, 76, 103, 96, 81, 121, 53, 115, 47, 119,
		41, 4, 87, 102, 45, 63, 109, 70, 127, 94, 97, 119, 88, 5, 45, 106, 66, 9,
		50, 102, 76, 49, 51, 57, 127, 21, 33, 0, 127, 88, 21, 61, 99, 41, 113, 117,
		110, 88, 45, 64, 28, 18, 119, 91, 37, 86, 30, 45, 63 };

	/**
	 * Simple regression test. A lot of this is just testing {@link Histogram1d}
	 * however theoretically if {@link HistogramCreate} is changed it would
	 * reflect in the values.
	 */
	@Test
	public void testRegression() {
		Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(data, 8, 8);
		Histogram1d<UnsignedByteType> histogram =
			(Histogram1d<UnsignedByteType>) ops.run("image.histogram", img, 10);

		Assert.assertEquals(false, histogram.hasTails());
		Assert.assertEquals(0.078125, histogram.relativeFrequency(5, false),
			0.00001d);
		Assert.assertEquals(10, histogram.getBinCount());

		UnsignedByteType type = new UnsignedByteType();
		histogram.getLowerBound(5, type);
		Assert.assertEquals(64.0, type.getRealDouble(), 0.00001d);
		histogram.getUpperBound(5, type);
		Assert.assertEquals(76.0, type.getRealDouble(), 0.00001d);
	}

}
