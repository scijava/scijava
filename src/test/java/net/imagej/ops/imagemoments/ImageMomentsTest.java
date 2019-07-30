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
		ops.run("imageMoments.moment00", img, moment00);
		DoubleType moment10 = new DoubleType();
		ops.run("imageMoments.moment10", img, moment10);
		DoubleType moment01 = new DoubleType();
		ops.run("imageMoments.moment01", img, moment01);
		DoubleType moment11 = new DoubleType();
		ops.run("imageMoments.moment11", img, moment11);
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
				((DoubleType) ops.run("imageMoments.centralMoment11", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.CentralMoment02", 1.0694469880269902E9,
				((DoubleType) ops.run("imageMoments.centralMoment02", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.CentralMoment20", 1.0585772432642114E9,
				((DoubleType) ops.run("imageMoments.centralMoment20", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.CentralMoment12", 5478324.271281097,
				((DoubleType) ops.run("imageMoments.centralMoment12", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.CentralMoment21", -2.1636455685489437E8,
				((DoubleType) ops.run("imageMoments.centralMoment21", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.CentralMoment30", 1.7355602329912126E8,
				((DoubleType) ops.run("imageMoments.centralMoment30", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.CentralMoment03", -4.099421316116555E8,
				((DoubleType) ops.run("imageMoments.centralMoment03", img)).getRealDouble(), EPSILON);
	}

	/**
	 * Test the Normalized Central Moment Ops.
	 */
	@Test
	public void testNormalizedCentralMoments() {
		assertEquals("ImageMoments.NormalizedCentralMoment11", -3.2325832933879204E-6,
				((DoubleType) ops.run("imageMoments.normalizedCentralMoment11", img)).getRealDouble(), EPSILON);

		assertEquals("ImageMoments.NormalizedCentralMoment02", 6.552610106398286E-4,
				((DoubleType) ops.run("imageMoments.normalizedCentralMoment02", img)).getRealDouble(), EPSILON);

		assertEquals("ImageMoments.NormalizedCentralMoment20", 6.486010078361372E-4,
				((DoubleType) ops.run("imageMoments.normalizedCentralMoment20", img)).getRealDouble(), EPSILON);

		assertEquals("ImageMoments.NormalizedCentralMoment12", 2.969727272701925E-9,
				((DoubleType) ops.run("imageMoments.normalizedCentralMoment12", img)).getRealDouble(), EPSILON);

		assertEquals("ImageMoments.NormalizedCentralMoment21", -1.1728837022440002E-7,
				((DoubleType) ops.run("imageMoments.normalizedCentralMoment21", img)).getRealDouble(), EPSILON);

		assertEquals("ImageMoments.NormalizedCentralMoment30", 9.408242926327751E-8,
				((DoubleType) ops.run("imageMoments.normalizedCentralMoment30", img)).getRealDouble(), EPSILON);

		assertEquals("ImageMoments.NormalizedCentralMoment03", -2.22224218245127E-7,
				((DoubleType) ops.run("imageMoments.normalizedCentralMoment03", img)).getRealDouble(), EPSILON);
	}

	/**
	 * Test the Hu Moment Ops.
	 */
	@Test
	public void testHuMoments() {
		assertEquals("ImageMoments.HuMoment1", 0.001303862018475966,
				((DoubleType)ops.run("imageMoments.huMoment1", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.HuMoment2", 8.615401633994056e-11,
				((DoubleType)ops.run("imageMoments.huMoment2", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.HuMoment3", 2.406124306990366e-14,
				((DoubleType)ops.run("imageMoments.huMoment3", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.HuMoment4", 1.246879188175627e-13,
				((DoubleType)ops.run("imageMoments.huMoment4", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.HuMoment5", -6.610443880647384e-27,
				((DoubleType)ops.run("imageMoments.huMoment5", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.HuMoment6", 1.131019166855569e-18,
				((DoubleType)ops.run("imageMoments.huMoment6", img)).getRealDouble(), EPSILON);
		assertEquals("ImageMoments.HuMoment7", 1.716256940536518e-27,
				((DoubleType)ops.run("imageMoments.huMoment7", img)).getRealDouble(), EPSILON);
	}

}