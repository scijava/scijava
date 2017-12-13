
package org.scijava.ops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Ops {

	private Ops() {
	}

	public static <I, O> ComputerOp<I, O> asComputer(final Function<I, O> op, final BiConsumer<O, O> copy) {
		return (in, out) -> {
			final O temp = op.apply(in);
			copy.accept(temp, out);
		};
	}

	public <IO> ComputerOp<IO, IO> asComputer(final InplaceOp<IO> op, final BiConsumer<IO, IO> copy) {
		return (in, out) -> {
			copy.accept(in, out);
			op.accept(out);
		};
	}

	public <IO, F extends InplaceOp<IO> & OutputAware<IO, IO>> Function<IO, IO> asFunction(final F op,
			final BiConsumer<IO, IO> copy) {
		return in -> {
			IO o = op.createOutput(in);
			copy.accept(in, o);
			op.accept(o);
			return o;
		};
	}

	public static <I, O> Function<I, Collection<O>> asFunction(final FlatMapOp<I, O> op) {
		return in -> {
			final ArrayList<O> out = new ArrayList<>();
			op.accept(in, (o) -> out.add(o));
			return out;
		};
	}

	public static <I, O, OP extends ComputerOp<I, O> & OutputAware<I, O>> Function<I, O> asFunction(final OP op) {
		return in -> {
			final O out = op.createOutput(in);
			op.accept(in, out);
			return out;
		};
	}

	public static <I, O> FlatMapOp<I, O> asFlatMap(final Function<I, O> op) {
		return (in, out) -> out.accept(op.apply(in));
	}

	public static <I> InplaceOp<I> asInplace(Function<I, I> func, final BiConsumer<I, I> copy) {
		return (in) -> {
			final I res = func.apply(in);
			copy.accept(res, in);
		};
	}

	public static <I> InplaceOp<I> asInplace(ComputerOp<I, I> func) {
		return (in) -> func.accept(in, in);
	}

	public static <I, C extends OutputAware<I, I> & ComputerOp<I, I>> InplaceOp<I> asInplace(C func,
			final BiConsumer<I, I> copy) {
		return (in) -> {
			I out = func.createOutput(in);
			func.accept(in, out);
			copy.accept(out, in);
		};
	}

	public static <I, O> Function<Collection<I>, O> asFunction(final AggregateOp<I, O> op) {
		return in -> {
			final O memo = op.createMemo(in.iterator().next());
			return in.parallelStream().reduce(memo, op.accumulator(), op.combiner());
		};
	}

	public static <I, O> Function<Collection<I>, Collection<O>> lift(final Function<I, O> op) {
		// START HERE: need to make this an explicit subclass a la Views
		// maybe LiftedFunctionOp or something. It implements SelfAware,
		// which defines OpInfo info(), allowing this guy to define how it
		// works.
		// That way, the wrapped op's extra parameters can be propagated into
		// the wrapped guy here.
		return in -> in.parallelStream().map(op).collect(Collectors.toList());
	}

	public static <I, O> Function<Collection<I>, Collection<O>> lift(final FlatMapOp<I, O> op) {
		return (in) -> in.parallelStream().flatMap((i) -> asFunction(op).apply(i).parallelStream())
				.collect(Collectors.toList());
	}
}