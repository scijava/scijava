
package net.imagej.ops2.types.adapt;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imagej.ops2.AbstractOpTest;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.Test;
import org.scijava.function.Functions;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;

@Plugin(type = OpCollection.class)
public class LiftFunctionsToRAITest<I extends RealType<I>> extends
	AbstractOpTest
{

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final Function<I, DoubleType> testOp = (in) -> new DoubleType(10d);

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final BiFunction<I, I, DoubleType> testOp2 = (in1,
		in2) -> new DoubleType(20d);

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final Functions.Arity3<I, I, I, DoubleType> testOp3 = (in1, in2,
		in3) -> new DoubleType(30d);

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final Functions.Arity4<I, I, I, I, DoubleType> testOp4 = (in1, in2,
		in3, in4) -> new DoubleType(40d);

	/**
	 * @implNote op names='test.liftImg'
	 */
	public final Functions.Arity5<I, I, I, I, I, DoubleType> testOp5 = (in1, in2,
		in3, in4, in5) -> new DoubleType(50d);

	@Test
	public void testLiftFunction1ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(
			new FinalDimensions(3, 3), new DoubleType()).outType(
				new Nil<Img<DoubleType>>()
				{}).apply();

		RandomAccessibleInterval<DoubleType> output = ops.op("test.liftImg").input(
			input).outType(new Nil<RandomAccessibleInterval<DoubleType>>()
		{}).apply();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while (cursor.hasNext()) {
			assert (cursor.next().get() == 10.);
		}

	}

	@Test
	public void testLiftFunction2ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(
			new FinalDimensions(3, 3), new DoubleType()).outType(
				new Nil<Img<DoubleType>>()
				{}).apply();

		RandomAccessibleInterval<DoubleType> output = ops.op("test.liftImg").input(
			input, input).outType(new Nil<RandomAccessibleInterval<DoubleType>>()
		{}).apply();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while (cursor.hasNext()) {
			assert (cursor.next().get() == 20.);
		}

	}

	@Test
	public void testLiftFunction3ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(
			new FinalDimensions(3, 3), new DoubleType()).outType(
				new Nil<Img<DoubleType>>()
				{}).apply();

		RandomAccessibleInterval<DoubleType> output = ops.op("test.liftImg").input(
			input, input, input).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while (cursor.hasNext()) {
			assert (cursor.next().get() == 30.);
		}

	}

	@Test
	public void testLiftFunction4ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(
			new FinalDimensions(3, 3), new DoubleType()).outType(
				new Nil<Img<DoubleType>>()
				{}).apply();

		RandomAccessibleInterval<DoubleType> output = ops.op("test.liftImg").input(
			input, input, input, input).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while (cursor.hasNext()) {
			assert (cursor.next().get() == 40.);
		}

	}

	@Test
	public void testLiftFunction5ToRAI() {
		RandomAccessibleInterval<DoubleType> input = ops.op("create.img").input(
			new FinalDimensions(3, 3), new DoubleType()).outType(
				new Nil<Img<DoubleType>>()
				{}).apply();

		RandomAccessibleInterval<DoubleType> output = ops.op("test.liftImg").input(
			input, input, input, input, input).outType(
				new Nil<RandomAccessibleInterval<DoubleType>>()
				{}).apply();

		Cursor<DoubleType> cursor = Views.flatIterable(output).cursor();
		while (cursor.hasNext()) {
			assert (cursor.next().get() == 50.);
		}

	}

}
