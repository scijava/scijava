package net.imagej.ops2.types;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.jupiter.api.Test;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Computers;
import org.scijava.types.Nil;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class LiftingTest<I extends RealType<I>, O extends RealType<O>> extends AbstractOpTest {

	@OpField(names = "test.liftImg", params = "input, output")
	public final Computers.Arity1<I, O> testOp = (in, out) -> out.setReal(10.);

	@Test
	public void testLiftToImg() {
		Img<DoubleType> input = op("create.img").input(new FinalDimensions(10, 10), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		Img<DoubleType> output = op("create.img").input(new FinalDimensions(10, 10), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		op("test.liftImg").input(input).output(output).compute();

		Cursor<DoubleType> cursor = output.cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 10.);
		}
				
	}

}
