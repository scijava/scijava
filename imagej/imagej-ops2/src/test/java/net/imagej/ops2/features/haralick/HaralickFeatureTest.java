/*-
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
package net.imagej.ops2.features.haralick;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.imagej.ops2.AbstractOpTest;
import net.imagej.ops2.features.AbstractFeatureTest;
import net.imagej.ops2.image.cooccurrenceMatrix.MatrixOrientation2D;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Haralick features tested against matlab implementations and all formulas
 * verified.
 * 
 * References:
 * http://murphylab.web.cmu.edu/publications/boland/boland_node26.html
 * http://www.uio.no/studier/emner/matnat/ifi/INF4300/h08/undervisningsmateriale
 * /glcm.pdf
 * 
 * @author Tim-Oliver Buchholz, University of Konstanz
 *
 */
public class HaralickFeatureTest extends AbstractOpTest {

	private final static double EPSILON = 10e-10;

	private Img<UnsignedByteType> img;

	@BeforeEach
	public void loadImage() {
		img = openUnsignedByteType(AbstractFeatureTest.class, "haralick_test_img.tif");
	}

	@Test
	public void asm() {
		DoubleType asm = (DoubleType) ops.op("features.haralick.asm").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(0.002728531855956, asm.get(), EPSILON);
	}

	@Test
	public void clusterPromenence() {
		DoubleType clusterPromenence = (DoubleType) ops.op("features.haralick.clusterPromenence").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(1.913756322645621e+07,
				clusterPromenence.get(), EPSILON);
	}

	@Test
	public void clusterShade() {
		DoubleType clusterShade = (DoubleType) ops.op("features.haralick.clusterShade").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply(); 
		assertEquals(7.909331564367658e+03,
				clusterShade.get(), EPSILON);
	}

	@Test
	public void contrast() {
		DoubleType contrast = (DoubleType) ops.op("features.haralick.contrast").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(2.829684210526314e+03, contrast.get(),
				EPSILON);
	}

	@Test
	public void correlation() {
		DoubleType correlation = (DoubleType) ops.op("features.haralick.correlation").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(-6.957913328178969e-03,
				correlation.get(), EPSILON);
	}

	@Test
	public void differenceEntropy() {
		DoubleType entropy = (DoubleType) ops.op("features.haralick.differenceEntropy").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(4.517886362509830,
				entropy.get(), EPSILON);
	}

	@Test
	public void differenceVariance() {
		DoubleType variance = (DoubleType) ops.op("features.haralick.differenceVariance").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply(); 
		assertEquals(8.885861218836561e+02,
				variance.get(), EPSILON);
	}

	@Test
	public void entropy() {
		DoubleType entropy = (DoubleType) ops.op("features.haralick.entropy").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(5.914634251331289, entropy.get(),
				EPSILON);
	}

	@Test
	public void ICM1() {
		DoubleType icm1 = (DoubleType) ops.op("features.haralick.icm1").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(-1.138457766487823, icm1.get(),
				EPSILON);
	}

	@Test
	public void ICM2() {
		DoubleType icm2 = (DoubleType) ops.op("features.haralick.icm2").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(0.9995136931858095, icm2.get(),
				EPSILON);
	}

	@Test
	public void IFDM() {
		DoubleType ifdm = (DoubleType) ops.op("features.haralick.ifdm").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(2.630092221760193e-02, ifdm.get(),
				EPSILON);
	}

	@Test
	public void maxProbability() {
		DoubleType maxProb = (DoubleType) ops.op("features.haralick.maxProbability").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply(); 
		assertEquals(0.005263157894737,
				maxProb.get(), EPSILON);
	}

	@Test
	public void sumAverage() {
		DoubleType sumAvg = (DoubleType) ops.op("features.haralick.sumAverage").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply(); 
		assertEquals(1.244210526315790e+02,
				sumAvg.get(), EPSILON);
	}

	@Test
	public void sumEntropy() {
		DoubleType sumEntropy = (DoubleType) ops.op("features.haralick.sumEntropy").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(5.007751063919794, sumEntropy.get(),
				EPSILON);
	}

	@Test
	public void sumVariance() {
		DoubleType sumVariance = (DoubleType) ops.op("features.haralick.sumVariance").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply(); 
		assertEquals(1.705010667439121e+04,
				sumVariance.get(), EPSILON);
	}

	@Test
	public void textureHomogeneity() {
		DoubleType homogeneity = (DoubleType) ops.op("features.haralick.textureHomogeneity").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply(); 
		assertEquals(0.061929185106950,
				homogeneity.get(), EPSILON);
	}

	@Test
	public void variance() {
		// no reference found in matlab, formula verified
		// (http://murphylab.web.cmu.edu/publications/boland/boland_node26.html)
		DoubleType variance = (DoubleType) ops.op("features.haralick.variance").input(img, 128, 1, MatrixOrientation2D.HORIZONTAL).apply();
		assertEquals(5176.653047585449, variance.get(),
				EPSILON);
	}
}
