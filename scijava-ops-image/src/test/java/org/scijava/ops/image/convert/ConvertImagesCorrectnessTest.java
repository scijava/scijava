package org.scijava.ops.image.convert;

import net.imglib2.img.array.ArrayImgs;
import org.junit.jupiter.api.Test;
import org.scijava.common3.MersenneTwisterFast;

public class ConvertImagesCorrectnessTest {

	@Test
	public void realTypeToComplexTypeTest() {
		MersenneTwisterFast mt = new MersenneTwisterFast(0xdeadbeefL);
		int width = 10, height = 10;
		var backing = ArrayImgs.doubles(width, height);
		var cursor = backing.cursor();
		while(cursor.hasNext()) {
			cursor.next().setReal(mt.nextDouble());
		}
	}



}
