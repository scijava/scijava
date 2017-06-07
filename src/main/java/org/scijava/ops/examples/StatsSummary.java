package org.scijava.ops.examples;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.scijava.ops.ReduceOp;
import org.scijava.plugin.Plugin;
import org.scijava.util.DoubleArray;

/**
 * Takes a list of doubles as inputs; produces some statistics as output.
 */
@Plugin(type = ReduceOp.class)
public class StatsSummary implements ReduceOp<Double, DoubleArray> {
	// reduce      N->M -- BiConsumer<Iterable<I>, Consumer<O>> void accept(Iterable<Data> in, Consumer<O> out)

	@Override
	public void accept(final Iterable<Double> in, Consumer<DoubleArray> out) {
		DoubleArray results = new DoubleArray();
		results.add(median(in));
		results.addAll(modes(in));
		out.accept(results);
	}

	private DoubleArray modes(final Iterable<Double> in) {
		return new DoubleArray(new double[] {2, 3, 4}); // fake for now
	}

	private Double median(final Iterable<Double> in) {
		final DoubleArray doubles = new DoubleArray();
		StreamSupport.stream(in.spliterator(), false).forEachOrdered(d -> doubles.add(d));
		Collections.sort(doubles);
		if (doubles.size() % 2 == 0) {
			return (doubles.get(doubles.size() / 2) + doubles.get(doubles.size() / 2 + 1)) / 2;
		}
		return doubles.get(doubles.size() / 2);
	}

}