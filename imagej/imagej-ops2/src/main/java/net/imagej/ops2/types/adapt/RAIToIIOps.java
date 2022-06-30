package net.imagej.ops2.types.adapt;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

public class RAIToIIOps<T, U, V> {

	/**
	 * @implNote op names='adapt'
	 */
	public final Function<Function<Iterable<T>, U>, Function<RandomAccessibleInterval<T>, U>> func = (in) -> {
		return (in1) -> in.apply(Views.flatIterable(in1));
	};

	/**
	 * @implNote op names='adapt'
	 */
	public final Function<BiFunction<Iterable<T>, Iterable<U>, V>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, V>> biFunc = (in) -> {
		return (in1, in2) -> in.apply(Views.flatIterable(in1), Views.flatIterable(in2));
	};

}
