
package org.scijava.ops.engine.matcher.impl;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInstance;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.OpWrapper;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.types.Nil;

/**
 * An {@link OpWrapper} for {@link LossReporter}s. TODO: would be nice if this
 * was unnecessary.
 *
 * @author Gabriel Selzer
 * @param <I>
 * @param <O>
 */
public class LossReporterWrapper<I, O> //
	implements //
	OpWrapper<LossReporter<I, O>>
{

	@Override
	public RichOp<LossReporter<I, O>> wrap( //
		final OpInstance<LossReporter<I, O>> instance, OpEnvironment env, Hints hints)
	{
		class GenericTypedLossReporter //
			extends AbstractRichOp<LossReporter<I, O>> //
			implements LossReporter<I, O> //
		{

			public GenericTypedLossReporter() {
				super(instance, env, hints);
			}

			@Override
			public Double apply(Nil<I> from, Nil<O> to) //
			{
				preprocess(from, to);

				// Call the op
				Double output = instance.op().apply(from, to);

				postprocess(output);

				return output;
			}

			@Override
			public LossReporter<I, O> asOpType() {
				return this;
			}

		}
		return new GenericTypedLossReporter();
	}
}
