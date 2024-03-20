
package org.scijava.ops.opencv;

import org.bytedeco.opencv.opencv_core.GpuMat;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.UMat;
import org.scijava.types.Nil;

/**
 * A set of static methods intended to describe OpenCV data structures.
 *
 * @author Gabriel Selzer
 */
public class OpenCVDescriptors {

	public static String matDescriptor(Nil<Mat> in) {
		return "image";
	}

	public static String gpuMatDescriptor(Nil<GpuMat> in) {
		return "image";
	}

	public static String uMatDescriptor(Nil<UMat> in) {
		return "image";
	}
}
