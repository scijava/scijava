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

package net.imagej.ops.imagemoments;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests {@link net.imagej.ops.Ops.ImageMoments}.
 * 
 * @author Daniel Seebacher
 */
public class ImageMomentsTest extends AbstractOpTest {

	private static final double EPSILON = 1e-8;
	private static Img<UnsignedByteType> img;

	@BeforeClass
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
		new OpBuilder(ops, "imageMoments.moment00").input(img, moment00).apply();
		DoubleType moment10 = new DoubleType();
		new OpBuilder(ops, "imageMoments.moment10").input(img, moment10).apply();
		DoubleType moment01 = new DoubleType();
		new OpBuilder(ops, "imageMoments.moment01").input(img, moment01).apply();
		DoubleType moment11 = new DoubleType();
		new OpBuilder(ops, "imageMoments.moment11").input(img, moment11).apply();
		assertEquals("ImageMoments.Moment00", 1277534.0, moment00.getRealDouble(), EPSILON);
		assertEquals("ImageMoments.Moment10", 6.3018047E7, moment10.getRealDouble(), EPSILON);
		assertEquals("ImageMoments.Moment01", 6.3535172E7, moment01.getRealDouble(), EPSILON);
		assertEquals("ImageMoments.Moment11", 3.12877962E9, moment11.getRealDouble(), EPSILON);
	}

	/**
	 * Test the Central Moment Ops.
	 */
	@Test
	public void testCentralMoments() {
		assertEquals("ImageMoments.CentralMoment11", -5275876.956702709,
				((DoubleType) new OpBuilder(ops, "imageMoments.centralMoment11").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.CentralMoment02", 1.0694469880269902E9,
				((DoubleType) new OpBuilder(ops, "imageMoments.centralMoment02").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.CentralMoment20", 1.0585772432642114E9,
				((DoubleType) new OpBuilder(ops, "imageMoments.centralMoment20").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.CentralMoment12", 5478324.271281097,
				((DoubleType) new OpBuilder(ops, "imageMoments.centralMoment12").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.CentralMoment21", -2.1636455685489437E8,
				((DoubleType) new OpBuilder(ops, "imageMoments.centralMoment21").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.CentralMoment30", 1.7355602329912126E8,
				((DoubleType) new OpBuilder(ops, "imageMoments.centralMoment30").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.CentralMoment03", -4.099421316116555E8,
				((DoubleType) new OpBuilder(ops, "imageMoments.centralMoment03").input(img)).getRealDouble(), EPSILON).apply();
	}

	/**
	 * Test the Normalized Central Moment Ops.
	 */
	@Test
	public void testNormalizedCentralMoments() {
		assertEquals("ImageMoments.NormalizedCentralMoment11", -3.2325832933879204E-6,
				((DoubleType) new OpBuilder(ops, "imageMoments.normalizedCentralMoment11").input(img)).getRealDouble(), EPSILON).apply();

		assertEquals("ImageMoments.NormalizedCentralMoment02", 6.552610106398286E-4,
				((DoubleType) new OpBuilder(ops, "imageMoments.normalizedCentralMoment02").input(img)).getRealDouble(), EPSILON).apply();

		assertEquals("ImageMoments.NormalizedCentralMoment20", 6.486010078361372E-4,
				((DoubleType) new OpBuilder(ops, "imageMoments.normalizedCentralMoment20").input(img)).getRealDouble(), EPSILON).apply();

		assertEquals("ImageMoments.NormalizedCentralMoment12", 2.969727272701925E-9,
				((DoubleType) new OpBuilder(ops, "imageMoments.normalizedCentralMoment12").input(img)).getRealDouble(), EPSILON).apply();

		assertEquals("ImageMoments.NormalizedCentralMoment21", -1.1728837022440002E-7,
				((DoubleType) new OpBuilder(ops, "imageMoments.normalizedCentralMoment21").input(img)).getRealDouble(), EPSILON).apply();

		assertEquals("ImageMoments.NormalizedCentralMoment30", 9.408242926327751E-8,
				((DoubleType) new OpBuilder(ops, "imageMoments.normalizedCentralMoment30").input(img)).getRealDouble(), EPSILON).apply();

		assertEquals("ImageMoments.NormalizedCentralMoment03", -2.22224218245127E-7,
				((DoubleType) new OpBuilder(ops, "imageMoments.normalizedCentralMoment03").input(img)).getRealDouble(), EPSILON).apply();
	}

	/**
	 * Test the Hu Moment Ops.
	 */
	@Test
	public void testHuMoments() {
		assertEquals("ImageMoments.HuMoment1", 0.001303862018475966,
				((DoubleType)new OpBuilder(ops, "imageMoments.huMoment1").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.HuMoment2", 8.615401633994056e-11,
				((DoubleType)new OpBuilder(ops, "imageMoments.huMoment2").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.HuMoment3", 2.406124306990366e-14,
				((DoubleType)new OpBuilder(ops, "imageMoments.huMoment3").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.HuMoment4", 1.246879188175627e-13,
				((DoubleType)new OpBuilder(ops, "imageMoments.huMoment4").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.HuMoment5", -6.610443880647384e-27,
				((DoubleType)new OpBuilder(ops, "imageMoments.huMoment5").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.HuMoment6", 1.131019166855569e-18,
				((DoubleType)new OpBuilder(ops, "imageMoments.huMoment6").input(img)).getRealDouble(), EPSILON).apply();
		assertEquals("ImageMoments.HuMoment7", 1.716256940536518e-27,
				((DoubleType)new OpBuilder(ops, "imageMoments.huMoment7").input(img)).getRealDouble(), EPSILON).apply();
	}

}
