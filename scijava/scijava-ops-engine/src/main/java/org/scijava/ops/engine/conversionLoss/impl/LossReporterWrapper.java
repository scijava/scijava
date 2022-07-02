
package org.scijava.ops.engine.conversionLoss.impl;

import org.scijava.ops.api.OpInstance;
import org.scijava.ops.api.OpMetadata;
import org.scijava.ops.api.OpWrapper;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.ops.engine.matcher.impl.AbstractRichOp;
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
		final OpInstance<LossReporter<I, O>> instance, //
		final OpMetadata metadata)
	{
		class GenericTypedLossReporter //
			extends AbstractRichOp<LossReporter<I, O>> //
			implements LossReporter<I, O> //
		{

			public GenericTypedLossReporter() {
				super(instance, metadata);
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
