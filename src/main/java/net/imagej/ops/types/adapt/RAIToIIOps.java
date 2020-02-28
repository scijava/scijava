package net.imagej.ops.types.adapt;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class RAIToIIOps<T, U, V> {

	@OpField(names = "adapt", params = "op")
	public final Function<Function<Iterable<T>, U>, Function<RandomAccessibleInterval<T>, U>> func = (in) -> {
		return (in1) -> in.apply(Views.flatIterable(in1));
	};

	@OpField(names = "adapt", params = "op")
	public final Function<BiFunction<Iterable<T>, Iterable<U>, V>, BiFunction<RandomAccessibleInterval<T>, RandomAccessibleInterval<U>, V>> biFunc = (in) -> {
		return (in1, in2) -> in.apply(Views.flatIterable(in1), Views.flatIterable(in2));
	};

}
