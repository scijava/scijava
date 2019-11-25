package net.imagej.ops.types;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.Test;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

@Plugin(type = OpCollection.class)
public class LiftingTest <I extends RealType<I>, O extends RealType<O>> extends AbstractOpTest{

	@OpField(names = "test.liftImg", params = "input, output")
	public final Computers.Arity1<I, O> testOp = (in, out) -> out.setReal(10.);
	
	@Test
	public void testLiftToImg() {
		Img<DoubleType> input = (Img<DoubleType>) new OpBuilder(ops, "create.img").input(new FinalDimensions(10, 10), new DoubleType()).apply();
		Img<DoubleType> output = (Img<DoubleType>) new OpBuilder(ops, "create.img").input(new FinalDimensions(10, 10), new DoubleType()).apply();
		
		new OpBuilder(ops, "test.liftImg").input(input, output).apply();

		Cursor<DoubleType> cursor = output.cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 10.);
		}
				
	}


}
