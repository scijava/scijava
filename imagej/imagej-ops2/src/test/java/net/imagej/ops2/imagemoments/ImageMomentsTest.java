/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ops2.imagemoments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link net.imagej.ops2.Ops.ImageMoments}.
 * 
 * @author Daniel Seebacher
 */
public class ImageMomentsTest extends AbstractOpTest {

	private static final double EPSILON = 1e-8;
	private static Img<UnsignedByteType> img;

	@BeforeAll
	public static void createImg() {

		Img<UnsignedByteType> tmp = ArrayImgs.unsignedBytes(new long[] { 100, 100 });

		Random rand = new Random(1234567890L);
		final Cursor<UnsignedByteType> cursor = tmp.cursor();
		while (cursor.hasNext()) {
			cursor.next().set(rand.nextInt((int) tmp.firstElement().getMaxValue()));
		}

		img = tmp;
	}

	/**
	 * Test the Moment Ops.
	 */
	@Test
	public void testMoments() {

		DoubleType moment00 = new DoubleType();
		ops.op("imageMoments.moment00").input(img).output(moment00).compute();
		DoubleType moment10 = new DoubleType();
		ops.op("imageMoments.moment10").input(img).output(moment10).compute();
		DoubleType moment01 = new DoubleType();
		ops.op("imageMoments.moment01").input(img).output(moment01).compute();
		DoubleType moment11 = new DoubleType();
		ops.op("imageMoments.moment11").input(img).output(moment11).compute();
		assertEquals(1277534.0, moment00.getRealDouble(), EPSILON, "ImageMoments.Moment00");
		assertEquals(6.3018047E7, moment10.getRealDouble(), EPSILON, "ImageMoments.Moment10");
		assertEquals(6.3535172E7, moment01.getRealDouble(), EPSILON, "ImageMoments.Moment01");
		assertEquals(3.12877962E9, moment11.getRealDouble(), EPSILON, "ImageMoments.Moment11");
	}

	/**
	 * Test the Central Moment Ops.
	 */
	@Test
	public void testCentralMoments() {
		assertEquals(-5275876.956702709,
				ops.op("imageMoments.centralMoment11").input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON, "ImageMoments.CentralMoment11");
		assertEquals(1.069446988026993E9,
				ops.op("imageMoments.centralMoment02").input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON, "ImageMoments.CentralMoment02");
		assertEquals(1.0585772432642086E9,
				ops.op("imageMoments.centralMoment20").input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON, "ImageMoments.CentralMoment20");
		assertEquals(5478324.271281064,
				ops.op("imageMoments.centralMoment12").input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON, "ImageMoments.CentralMoment12");
		assertEquals(-2.163645568548715E8,
				ops.op("imageMoments.centralMoment21").input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON, "ImageMoments.CentralMoment21");
		assertEquals(1.735560232991217E8,
				ops.op("imageMoments.centralMoment30").input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON, "ImageMoments.CentralMoment30");
		assertEquals(-4.0994213161155105E8,
				ops.op("imageMoments.centralMoment03").input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON, "ImageMoments.CentralMoment03");
	}

	/**
	 * Test the Normalized Central Moment Ops.
	 */
	@Test
	public void testNormalizedCentralMoments() {
		assertEquals(-3.2325832933879204E-6,
				ops.op("imageMoments.normalizedCentralMoment11").input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON, "ImageMoments.NormalizedCentralMoment11");

		assertEquals(6.552610106398286E-4,
				ops.op("imageMoments.normalizedCentralMoment02").input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON, "ImageMoments.NormalizedCentralMoment02");

		assertEquals(6.486010078361372E-4,
				ops.op("imageMoments.normalizedCentralMoment20").input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON, "ImageMoments.NormalizedCentralMoment20");

		assertEquals(2.969727272701925E-9,
				ops.op("imageMoments.normalizedCentralMoment12").input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON, "ImageMoments.NormalizedCentralMoment12");

		assertEquals(-1.1728837022440002E-7,
				ops.op("imageMoments.normalizedCentralMoment21").input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON, "ImageMoments.NormalizedCentralMoment21");

		assertEquals(9.408242926327751E-8,
				ops.op("imageMoments.normalizedCentralMoment30").input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON, "ImageMoments.NormalizedCentralMoment30");

		assertEquals(-2.22224218245127E-7,
				ops.op("imageMoments.normalizedCentralMoment03").input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON, "ImageMoments.NormalizedCentralMoment03");
	}

	/*
	 * Test the Hu Moment Ops.
	 */
	@Test
	public void testHuMoments() {
		assertEquals(0.001303862018475966, ops.op("imageMoments.huMoment1").input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON,
			"ImageMoments.HuMoment1");
		assertEquals(8.615401633994056e-11, ops.op("imageMoments.huMoment2").input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON,
			"ImageMoments.HuMoment2");
		assertEquals(2.406124306990366e-14, ops.op("imageMoments.huMoment3").input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON,
			"ImageMoments.HuMoment3");
		assertEquals(1.246879188175627e-13, ops.op("imageMoments.huMoment4").input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON,
			"ImageMoments.HuMoment4");
		assertEquals(-6.610443880647384e-27, ops.op("imageMoments.huMoment5").input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON,
			"ImageMoments.HuMoment5");
		assertEquals(1.131019166855569e-18, ops.op("imageMoments.huMoment6").input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON,
			"ImageMoments.HuMoment6");
		assertEquals(1.716256940536518e-27, ops.op("imageMoments.huMoment7").input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON,
			"ImageMoments.HuMoment7");
	}

}
