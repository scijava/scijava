package org.scijava.ops.image.convert;

import net.imglib2.img.array.ArrayImgs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.common3.MersenneTwisterFast;

public class ConvertImagesCorrectnessTest {

	@Test
	public void realTypeToComplexDoubleTypeTest() {
		MersenneTwisterFast mt = new MersenneTwisterFast(0xdeadbeefL);
		int width = 10, height = 10;
		var backing = ArrayImgs.bytes(width, height);
		var converted = RAIWrappers.toComplexDoubleType(backing);

		var cursor = backing.cursor();
		var ra = converted.randomAccess();
		double inc = 3.2;
		byte max = (byte) (Byte.MAX_VALUE - (byte) Math.ceil(inc));
		while(cursor.hasNext()) {
			cursor.next();
			ra.setPosition(cursor);
			byte b = mt.nextByte();
			if (b > max) b = max;
			cursor.get().set(b);
			Assertions.assertEquals(b, ra.get().getRealDouble());
			ra.get().set(b + inc, inc);
			Assertions.assertEquals(b + (byte) inc, cursor.get().get());
			Assertions.assertEquals(0, cursor.get().getImaginaryDouble());
		}
	}

	@Test
	public void complexTypeToComplexDoubleTypeTest() {
		MersenneTwisterFast mt = new MersenneTwisterFast(0xdeadbeefL);
		int width = 10, height = 10;
		var backing = ArrayImgs.complexFloats(width, height);
		var converted = RAIWrappers.toComplexDoubleType(backing);

		var cursor = backing.cursor();
		var ra = converted.randomAccess();
		while(cursor.hasNext()) {
			cursor.next();
			ra.setPosition(cursor);
			var r = mt.nextFloat();
			var i = mt.nextFloat();
			cursor.get().set(r, i);
			Assertions.assertEquals(r, ra.get().getRealDouble(), 1e-6);
			Assertions.assertEquals(i, ra.get().getImaginaryDouble(), 1e-6);
			ra.get().set(r + r, i + i);
			Assertions.assertEquals(r + r, cursor.get().getRealFloat(), 1e-6);
			Assertions.assertEquals(i + i, cursor.get().getImaginaryFloat(), 1e-6);
		}
	}



}
