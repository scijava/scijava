
package org.scijava.ops.opencv;

import org.bytedeco.opencv.opencv_core.GpuMat;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.UMat;
import org.scijava.types.Any;

public final class MatCopier {

	public static final Class<?> TEMP = Any.class;

	public static void copy(Mat source, Mat dest) {
		source.copyTo(dest);
	}

	public static void copyU(UMat source, UMat dest) {
		source.copyTo(dest);
	}

	public static void copyGpu(GpuMat source, GpuMat dest) {
		source.copyTo(dest);
	}
}
