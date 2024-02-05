/*-
 * #%L
 * SciJava Ops OpenCV: OpenCV configuration for ops
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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
package org.scijava.ops.opencv;

import net.imagej.opencv.MatToImgConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.api.OpEnvironment;

import java.io.File;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestOpenCV {

	private static final String TEST_OP = "cv.GaussianBlur";
	private static final String EXPECTED_HELP = //
			"cv.GaussianBlur:\n" //
					+ "\t- (in, @CONTAINER container1, size, StDev) -> None";

	private static final String EXPECTED_HELP_VERBOSE = //
			"cv.GaussianBlur:\n" //
					+ "\t- org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur(org.bytedeco.opencv.opencv_core.Mat,org.bytedeco.opencv.opencv_core.Mat,org.bytedeco.opencv.opencv_core.Size,double)\n" //
					+ "\t\t> in : org.bytedeco.opencv.opencv_core.Mat\n" //
					+ "\t\t\tthe input {@link Mat} to blur\n" + "\t\t> container1 : @CONTAINER org.bytedeco.opencv.opencv_core.Mat\n" //
					+ "\t\t\ta preallocated {@link Mat} to populate\n" //
					+ "\t\t> size : org.bytedeco.opencv.opencv_core.Size\n" //
					+ "\t\t\tdesired kernel size\n" //
					+ "\t\t> StDev : java.lang.Double\n" //
					+ "\t\t\tdesired standard deviation/blur intensity";

	@Test
	public void testDiscovery() {
		final OpEnvironment ops = OpEnvironment.build();
		assertEquals(EXPECTED_HELP, ops.help(TEST_OP));
		assertEquals(EXPECTED_HELP_VERBOSE, ops.helpVerbose(TEST_OP));
	}

	@Test
	public void testUsage() {
		final OpEnvironment ops = OpEnvironment.build();
		Mat src = openFish();
		Mat opsFish = new Mat(src.rows(), src.cols(), src.type());
		Mat opencvFish = new Mat(src.rows(), src.cols(), src.type());

		int stDev = 100;
		Size size = new Size(5, 5);

		OpBuilder.Arity3_IV_OV<Mat, Size, Integer, Mat> builder =
				ops.ternary(TEST_OP).input(src, size, stDev).output(opsFish);

		// Check help strings for the builder
		assertEquals(EXPECTED_HELP, builder.help());
		assertEquals(EXPECTED_HELP_VERBOSE, builder.helpVerbose());

		// Blur with ops
		builder.compute();

		// Blur directly with JavaCV
		GaussianBlur(src, opencvFish, size, stDev);

		// Verify dimensions
		assertEquals(opencvFish.rows(), opsFish.rows());
		assertEquals(opencvFish.cols(), opsFish.cols());

		// Verify data
		byte[] opencvBytes = MatToImgConverter.toByteArray(opencvFish);
		byte[] opsFishBytes = MatToImgConverter.toByteArray(opsFish);

		assertArrayEquals(opencvBytes, opsFishBytes);
	}

	private Mat openFish() {
		return imread(new File(
				getClass().getResource("/HappyFish.jpg").getFile()).getAbsolutePath());
	}

	//TODO try opening with scifio and converting with imagej-opencv
}
