package org.scijava.ops.conversionLoss;

import java.lang.reflect.Type;

import org.scijava.ops.util.OpWrapper;
import org.scijava.plugin.Plugin;
import org.scijava.types.GenericTyped;
import org.scijava.types.Nil;

/**
 * An {@link OpWrapper} for {@link LossReporter}s.
 * TODO: would be nice if this was unnecessary.
 *
 * @author Gabriel Selzer
 *
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
		final Type reifiedType)
	{
		class GenericTypedLossReporter implements //
			LossReporter<I, O>, //
			GenericTyped //
		{

			@Override
			public Double apply(Nil<I> from, Nil<O> to) //
			{
				return op.apply(from, to);
			}

			@Override
			public Type getType() {
				return reifiedType;
			}
		}
		return new GenericTypedLossReporter();
	}
}

