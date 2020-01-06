package org.scijava.ops.adapt.lift;

import java.util.Iterator;
import java.util.function.Function;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Computers;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Converts {@link Computers} operating on single types to {@link Computers}
 * that operate on {@link Iterable}s of types. N.B. it is the user's
 * responsibility to pass {@link Iterable}s of the same length (otherwise the Op
 * will stop when one of the {@link Iterable}s runs out of {@link Object}s).
 * 
 * @author Gabriel Selzer
 *
 * @param <I1>
 *            type of the first input to the Computer
 * @param <O>
 *            type of the output of the Computer
 */
@Plugin(type = OpCollection.class)
public class ComputerToIterables<I1, O> {

	// TODO: put Type Variables on lambda
	@OpField(names = "adapt")
	@Parameter(key = "fromOp")
	@Parameter(key = "toOp")
	public final Function<Computers.Arity1<I1, O>, Computers.Arity1<Iterable<I1>, Iterable<O>>> liftComputer1 = (
			computer) -> {
		return (in, out) -> {
			Iterator<I1> itrIn = in.iterator();
			Iterator<O> itrOut = out.iterator();
			while (itrIn.hasNext() && itrOut.hasNext()) {
				computer.compute(itrIn.next(), itrOut.next());
			}
		};
	};

}
