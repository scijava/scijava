package org.scijava.ops.image.adapt;

import java.util.function.Function;

import org.scijava.ops.image.AbstractOpTest;
import org.scijava.ops.image.util.TestImgGeneration;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.spi.OpCollection;
import org.scijava.types.Nil;

public class LiftFunctionsToImgTest extends AbstractOpTest implements OpCollection {
	
	/**
	 * @implNote op names="test.liftFunctionToImg"
	 */
	public final Function<UnsignedByteType, UnsignedByteType> inc = //
		(in) -> new UnsignedByteType(in.get() + 1);	

	@Test
	public void testLiftingArity1() {
		Img<UnsignedByteType> foo = TestImgGeneration.unsignedByteArray(true, 10, 10, 10);
		Img<UnsignedByteType> result = ops.op("test.liftFunctionToImg") //
			.arity1() //
			.input(foo) //
			.outType(new Nil<Img<UnsignedByteType>>() {}) //
			.apply();
		Cursor<UnsignedByteType> cursor = result.localizingCursor();
		RandomAccess<UnsignedByteType> fooRA = foo.randomAccess();
		while(cursor.hasNext()) {
			cursor.next();
			cursor.localize(fooRA);
			Assertions.assertEquals(cursor.get().get(), (fooRA.get().get() + 1) % 256);
		}
	}

}
