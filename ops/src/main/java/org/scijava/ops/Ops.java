
package org.scijava.ops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Ops {

	private Ops() {}

	public static <I, O> ComputerOp<I, O> asComputer(final Function<I, O> op,
		final BiConsumer<O, O> copy)
	{
		return (in, out) -> {
			final O temp = op.apply(in);
			copy.accept(temp, out);
		};
	}
	public <IO> ComputerOp<IO, IO> asComputer(final InplaceOp<IO> op,
		final BiConsumer<IO, IO> copy)
	{
		return (in, out) -> {
			copy.accept(in, out);
			op.accept(out);
		};
	}
	// TODO: add more adapters! Yay!
	
	// TODO: consider whether (some of?) these should be service methods,
	// for extensibility. Or better: plugins of type OpAdapter. Later!
	// E.g.: expressing tree-reduce as a function may depend on type details.
	// net.imagej.ops.map ops are a special case for e.g. IterableInterval.
	
	public static <I, O> Function<Collection<I>, O> asFunction(
		final TreeReduceOp<I, O> op)
	{
		return in -> {
			final BiFunction<O, I, O> acc = op.accumulator();
			final O zero = op.createZero(in.iterator().next());
			// TODO: use spliterator for multi-threading and use combiner
			in.stream().forEach(e -> acc.apply(zero, e));
			return zero;
		};
	}

	public <IO, F extends InplaceOp<IO> & OutputAware<IO, IO>> Function<IO, IO>
		asFunction(final F op, final BiConsumer<IO, IO> copy)
	{
		return in -> {
			IO o = op.createOutput(in);
			copy.accept(in, o);
			op.accept(o);
			return o;
		};
	}

	// FIXME: lame.
	public static <I, O> Function<I, O> asFunction(final MapOp<I, O> op) {
		return new Function<I, O>() {
			private List<O> out = new ArrayList<>(1);
	
			@Override
			public O apply(final I in) {
				op.accept(in, in1 -> out.set(0, in1));
				return out.get(0);
			}};
	}

	public static <I, O, OP extends ComputerOp<I, O> & OutputAware<I, O>>
		Function<I, O> asFunction(final OP op)
	{
		return in -> {
			final O out = op.createOutput(in);
			op.accept(in, out);
			return out;
		};
	}

	public static <I, O> MapOp<I, O> asMap(final Function<I, O> op) {
		return (in, out) -> out.accept(op.apply(in));
	}

	// TODO: add more adapters! Yay!

	public static <I, O> Function<Collection<I>, Collection<O>> lift(
		final Function<I, O> op)
	{
		// START HERE: need to make this an explicit subclass a la Views
		// maybe LiftedFunctionOp or something. It implements SelfAware,
		// which defines OpInfo info(), allowing this guy to define how it works.
		// That way, the wrapped op's extra parameters can be propagated into
		// the wrapped guy here.
		return in -> in.parallelStream().map(op).collect(Collectors.toList());
	}

	// TODO: consider whether (some of?) these should be service methods,
	// for extensibility. Or better: plugins of type OpAdapter. Later!
	// E.g.: expressing tree-reduce as a function may depend on type details.
	// net.imagej.ops.map ops are a special case for e.g. IterableInterval.



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
