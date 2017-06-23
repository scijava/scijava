
package org.scijava.ops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Ops {

	private Ops() {}

	public static <I, O> MapOp<I, O> asMap(final Function<I, O> op) {
		return new MapOp<I, O>() {

			@Override
			public void accept(final I in, final Consumer<O> out) {
				out.accept(op.apply(in));
			}
		};
	}

	public static <I, O> FilterOp<I> asFilter(final Function<I, Boolean> op) {
		return new FilterOp<I>() {

			@Override
			public boolean test(final I t) {
				return op.apply(t);
			}
		};
	}

	public static <I, O, OP extends ComputerOp<I, O> & OutputAware<I, O>>
		FunctionOp<I, O> asFunction(final OP op)
	{
		return new FunctionOp<I, O>() {

			@Override
			public O apply(I in) {
				final O out = op.createOutput(in);
				op.accept(in, out);
				return out;
			}

		};
	}

	public static <I, O> ComputerOp<I, O> asComputer(final Function<I, O> op,
		final BiConsumer<O, O> copy)
	{
		return new ComputerOp<I, O>() {

			@Override
			public void accept(final I in, final O out) {
				final O temp = op.apply(in);
				copy.accept(temp, out);
			}

		};
	}
	// TODO: add more adapters! Yay!

	// FIXME: lame.
	public static <I, O> FunctionOp<I, O> asFunction(final MapOp<I, O> op) {
		return new FunctionOp<I, O>() {
			private ArrayList<O> out = new ArrayList<>(1);

			@Override
			public O apply(final I in) {
				op.accept(in, new Consumer<O>() {

					@Override
					public void accept(final O in) {
						out.set(0, in);
					}
				});
				return out.get(0);
			}};
	}

	public static <I, O> FunctionOp<Collection<I>, Collection<O>> lift(
		final Function<I, O> op)
	{
		// START HERE: need to make this an explicit subclass a la Views
		// maybe LiftedFunctionOp or something. It implements SelfAware,
		// which defines OpInfo info(), allowing this guy to define how it works.
		// That way, the wrapped op's extra parameters can be propagated into
		// the wrapped guy here.
		return new FunctionOp<Collection<I>, Collection<O>>() {

			@Override
			public Collection<O> apply(final Collection<I> in) {
				return in.stream().map(op).collect(Collectors.toList());
			}
		};
	}

	// TODO: consider whether (some of?) these should be service methods,
	// for extensibility. Or better: plugins of type OpAdapter. Later!
	// E.g.: expressing tree-reduce as a function may depend on type details.
	// net.imagej.ops.map ops are a special case for e.g. IterableInterval.

	public static <I, O> FunctionOp<Collection<I>, O> asFunction(
		final TreeReduceOp<I, O> op)
	{
		return new FunctionOp<Collection<I>, O>() {

			@Override
			public O apply(final Collection<I> in) {
				final BiFunction<O, I, O> acc = op.accumulator();
				final O zero = op.createZero(in.iterator().next());
				// TODO: use spliterator for multi-threading and use combiner
				in.stream().forEach(e -> acc.apply(zero, e));
				return zero;
			}
		};
	}
	// asFunction(TreeReduceOp)

	//

// use function as map
// accept(I in, Consumer<O> out):
//    out.accept(f(in))

// use function as computer  ---------
// compute(in, out):
//    o = f(in)
//    copy(o -> out)

// use computer as function  --------- needs OutputAware
// out = f(in):
//    o = create(in)
//    compute(in, o)
//    return o

// use inplace as function  ---------
// out = f(in):
//    o = create(in)
//    copy(in -> o)
//    mutate(o)

// use inplace as computer  ---------
// compute(in, out):
//    copy(in -> out)
//    mutate(out)

////////////////////
// Possibly problematic
// Consider whether to have these, can throw exception if incompatible

// use function as inplace  --------- only allowed if Function<T, T> and structures match
// mutate(arg):
//    o = f(arg)
//    copy(o -> arg)

// use computer as inplace  ---------
// mutate(arg):
//    o = create(arg)
//    compute(arg, o)
//    copy(o -> arg)
}
