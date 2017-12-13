package org.scijava.ops.imglib2;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class Hello {
	public static Img<UnsignedByteType> fiveTwelve() {
		return ArrayImgs.unsignedBytes(512, 512);
	}
}