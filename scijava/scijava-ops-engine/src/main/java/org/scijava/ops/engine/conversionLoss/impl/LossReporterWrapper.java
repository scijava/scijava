
package org.scijava.ops.engine.conversionLoss.impl;

import org.scijava.ops.api.OpMetadata;
import org.scijava.ops.api.OpWrapper;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.ops.engine.matcher.impl.DefaultRichOp;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;

/**
 * An {@link OpWrapper} for {@link LossReporter}s. TODO: would be nice if this
 * was unnecessary.
 *
 * @author Gabriel Selzer
 * @param <I>
 * @param <O>
 */
@Plugin(type = OpWrapper.class)
public class LossReporterWrapper<I, O> //
	implements //
	OpWrapper<LossReporter<I, O>>
{

	@Override
	public LossReporter<I, O> wrap( //
		final LossReporter<I, O> op, //
		final OpMetadata metadata)
	{
		class GenericTypedLossReporter //
			extends DefaultRichOp //
			implements LossReporter<I, O> //
		{

			public GenericTypedLossReporter() {
				super(op, metadata);
			}

			@Override
			public Double apply(Nil<I> from, Nil<O> to) //
			{
				preprocess(from, to);

				// Call the op
				Double output = op.apply(from, to);

				postprocess(output);

				return output;
			}

		}
		return new GenericTypedLossReporter();
	}
}
