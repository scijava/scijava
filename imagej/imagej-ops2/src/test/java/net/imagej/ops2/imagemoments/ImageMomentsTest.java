/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

import static org.scijava.testutil.AssertClose.assertCloseEnough;

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

	private static final int EPSILON_EXP = -8;
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
		ops.op("imageMoments.moment00").arity1().input(img).output(moment00).compute();
		DoubleType moment10 = new DoubleType();
		ops.op("imageMoments.moment10").arity1().input(img).output(moment10).compute();
		DoubleType moment01 = new DoubleType();
		ops.op("imageMoments.moment01").arity1().input(img).output(moment01).compute();
		DoubleType moment11 = new DoubleType();
		ops.op("imageMoments.moment11").arity1().input(img).output(moment11).compute();
		assertCloseEnough(1277534.0, moment00.getRealDouble(), EPSILON_EXP, "ImageMoments.Moment00");
		assertCloseEnough(6.3018047E7, moment10.getRealDouble(), EPSILON_EXP, "ImageMoments.Moment10");
		assertCloseEnough(6.3535172E7, moment01.getRealDouble(), EPSILON_EXP, "ImageMoments.Moment01");
		assertCloseEnough(3.12877962E9, moment11.getRealDouble(), EPSILON_EXP, "ImageMoments.Moment11");
	}

	/**
	 * Test the Central Moment Ops.
	 */
	@Test
	public void testCentralMoments() {
		assertCloseEnough(-5275876.956702709,
				ops.op("imageMoments.centralMoment11").arity1().input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON_EXP, "ImageMoments.CentralMoment11");
		assertCloseEnough(1.069446988026993E9,
				ops.op("imageMoments.centralMoment02").arity1().input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON_EXP, "ImageMoments.CentralMoment02");
		assertCloseEnough(1.0585772432642086E9,
				ops.op("imageMoments.centralMoment20").arity1().input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON_EXP, "ImageMoments.CentralMoment20");
		assertCloseEnough(5478324.271281064,
				ops.op("imageMoments.centralMoment12").arity1().input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON_EXP, "ImageMoments.CentralMoment12");
		assertCloseEnough(-2.163645568548715E8,
				ops.op("imageMoments.centralMoment21").arity1().input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON_EXP, "ImageMoments.CentralMoment21");
		assertCloseEnough(1.735560232991217E8,
				ops.op("imageMoments.centralMoment30").arity1().input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON_EXP, "ImageMoments.CentralMoment30");
		assertCloseEnough(-4.0994213161155105E8,
				ops.op("imageMoments.centralMoment03").arity1().input(img).outType(DoubleType.class).apply()
						.getRealDouble(),
				EPSILON_EXP, "ImageMoments.CentralMoment03");
	}

	/**
	 * Test the Normalized Central Moment Ops.
	 */
	@Test
	public void testNormalizedCentralMoments() {
		assertCloseEnough(-3.2325832933879204E-6,
				ops.op("imageMoments.normalizedCentralMoment11").arity1().input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON_EXP, "ImageMoments.NormalizedCentralMoment11");

		assertCloseEnough(6.552610106398286E-4,
				ops.op("imageMoments.normalizedCentralMoment02").arity1().input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON_EXP, "ImageMoments.NormalizedCentralMoment02");

		assertCloseEnough(6.486010078361372E-4,
				ops.op("imageMoments.normalizedCentralMoment20").arity1().input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON_EXP, "ImageMoments.NormalizedCentralMoment20");

		assertCloseEnough(2.969727272701925E-9,
				ops.op("imageMoments.normalizedCentralMoment12").arity1().input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON_EXP, "ImageMoments.NormalizedCentralMoment12");

		assertCloseEnough(-1.1728837022440002E-7,
				ops.op("imageMoments.normalizedCentralMoment21").arity1().input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON_EXP, "ImageMoments.NormalizedCentralMoment21");

		assertCloseEnough(9.408242926327751E-8,
				ops.op("imageMoments.normalizedCentralMoment30").arity1().input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON_EXP, "ImageMoments.NormalizedCentralMoment30");

		assertCloseEnough(-2.22224218245127E-7,
				ops.op("imageMoments.normalizedCentralMoment03").arity1().input(img).outType(DoubleType.class)
						.apply().getRealDouble(),
				EPSILON_EXP, "ImageMoments.NormalizedCentralMoment03");
	}

	/*
	 * Test the Hu Moment Ops.
	 */
	@Test
	public void testHuMoments() {
		assertCloseEnough(0.001303862018475966, ops.op("imageMoments.huMoment1").arity1().input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON_EXP,
			"ImageMoments.HuMoment1");
		assertCloseEnough(8.615401633994056e-11, ops.op("imageMoments.huMoment2").arity1().input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON_EXP,
			"ImageMoments.HuMoment2");
		assertCloseEnough(2.406124306990366e-14, ops.op("imageMoments.huMoment3").arity1().input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON_EXP,
			"ImageMoments.HuMoment3");
		assertCloseEnough(1.246879188175627e-13, ops.op("imageMoments.huMoment4").arity1().input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON_EXP,
			"ImageMoments.HuMoment4");
		assertCloseEnough(-6.610443880647384e-27, ops.op("imageMoments.huMoment5").arity1().input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON_EXP,
			"ImageMoments.HuMoment5");
		assertCloseEnough(1.131019166855569e-18, ops.op("imageMoments.huMoment6").arity1().input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON_EXP,
			"ImageMoments.HuMoment6");
		assertCloseEnough(1.716256940536518e-27, ops.op("imageMoments.huMoment7").arity1().input(img)
			.outType(DoubleType.class).apply().getRealDouble(), EPSILON_EXP,
			"ImageMoments.HuMoment7");
	}

}
