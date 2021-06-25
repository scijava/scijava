package net.imagej.ops2.types.adapt;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.types.Nil;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class LiftComputersToRAITest<I extends RealType<I>, O extends RealType<O>> extends AbstractOpTest {

	@OpField(names = "test.liftImg", params = "input, output")
	public final Computers.Arity1<I, O> testOp = (in, out) -> out.setReal(10.);

	@OpField(names = "test.liftImg", params = "input1, input2, output")
	public final Computers.Arity2<I, I, O> testOp2 = (in1, in2, out) -> out.setReal(20.);

	@OpField(names = "test.liftImg", params = "input1, input2, input3, output")
	public final Computers.Arity3<I, I, I, O> testOp3 = (in1, in2, in3, out) -> out.setReal(30.);

	@OpField(names = "test.liftImg", params = "input1, input2, input3, input4, output")
	public final Computers.Arity4<I, I, I, I, O> testOp4 = (in1, in2, in3, in4, out) -> out.setReal(40.);

	@OpField(names = "test.liftImg", params = "input1, input2, input3, input4, input5, output")
	public final Computers.Arity5<I, I, I, I, I, O> testOp5 = (in1, in2, in3, in4, in5, out) -> out.setReal(50.);

	@Test
	public void testLiftComputer1ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		RandomAccessibleInterval<DoubleType> output = ops.op("create.img").input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		ops.op("test.liftImg").input(input).output(output).compute();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 10.);
		}
				
	}

	@Test
	public void testLiftComputer2ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		RandomAccessibleInterval<DoubleType> output = ops.op("create.img").input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		ops.op("test.liftImg").input(input, input).output(output).compute();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 20.);
		}
				
	}

	@Test
	public void testLiftComputer3ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		RandomAccessibleInterval<DoubleType> output = ops.op("create.img").input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		ops.op("test.liftImg").input(input, input, input).output(output).compute();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 30.);
		}
				
	}

	@Test
	public void testLiftComputer4ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		RandomAccessibleInterval<DoubleType> output = ops.op("create.img").input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		ops.op("test.liftImg").input(input, input, input, input).output(output).compute();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 40.);
		}
				
	}

	@Test
	public void testLiftComputer5ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		RandomAccessibleInterval<DoubleType> output = ops.op("create.img").input(new FinalDimensions(3, 3), new DoubleType()).outType(new Nil<Img<DoubleType>>() {}).apply();
		
		ops.op("test.liftImg").input(input, input, input, input, input).output(output).compute();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while(cursor.hasNext()) {
			assert(cursor.next().get() == 50.);
		}
				
	}

}
