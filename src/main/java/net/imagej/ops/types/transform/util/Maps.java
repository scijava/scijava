package net.imagej.ops.types.transform.util;

import java.util.function.Function;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.read.ConvertedRandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.scijava.ops.core.computer.BiComputer;
import org.scijava.ops.core.computer.Computer;

public class Maps {

	private Maps() {
		// NB: Prevent instantiation of utility class.
	}
		
	public interface Functions {
		
		public interface RAIs {
			
			public static <I, O extends Type<O>> Function<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> liftBoth(final Function<I, O> function) {
				return in -> {
					O outType = function.apply(Util.getTypeFromInterval(in));
					Img<O> outImg = Util.getSuitableImgFactory(in, outType).create(in);
					Cursor<I> inCursor = Views.flatIterable(in).cursor();
					Cursor<O> outCursor = Views.flatIterable(outImg).cursor();
					while(inCursor.hasNext()) {
						outCursor.next().set(function.apply(inCursor.next()));
					}
					return outImg;
				};
			}
			
			public static <I extends NativeType<I>, O extends NativeType<O>> Function<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> liftBothNativesView(final Function<I, O> function) {
				return in -> {
					O outType = function.apply(Util.getTypeFromInterval(in));
					Converter<I, O> converter = new Converter<I, O>(){
						@Override
						public void convert(I input, O output) {
							output.set(function.apply(input));
						}
					};
					return new ConvertedRandomAccessibleInterval<>(in, converter, outType);
				};
			}
			
			

		}
	}

	public interface Computers {
		
		public interface RAIs {
			
			public static <I, O> Computer<RandomAccessibleInterval<I>, RandomAccessibleInterval<O>> liftBoth(final Computer<I, O> computer) {
				return (raiInput, raiOutput) -> {
					Cursor<I> inCursor = Views.flatIterable(raiInput).cursor();
					Cursor<O> outCursor = Views.flatIterable(raiOutput).cursor();
					while(inCursor.hasNext() && outCursor.hasNext()) {
						computer.compute(inCursor.next(), outCursor.next());
					}
				};
			}

			public static <I1, I2, O> BiComputer<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I2>, RandomAccessibleInterval<O>> liftBoth(final BiComputer<I1, I2, O> computer) {
				return (raiInput1, raiInput2, raiOutput) -> {
					Cursor<I1> inCursor1 = Views.flatIterable(raiInput1).cursor();
					Cursor<I2> inCursor2 = Views.flatIterable(raiInput2).cursor();
					Cursor<O> outCursor = Views.flatIterable(raiOutput).cursor();
					while(inCursor1.hasNext() && inCursor2.hasNext() && outCursor.hasNext()) {
						computer.compute(inCursor1.next(), inCursor2.next(), outCursor.next());
					}
				};
			}
		}
	}
}
