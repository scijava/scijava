/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.engine.matcher.impl;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInstance;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.MatchingConditions;
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
		final OpInstance<LossReporter<I, O>> instance, OpEnvironment env,
		MatchingConditions conditions)
	{
		class GenericTypedLossReporter //
			extends AbstractRichOp<LossReporter<I, O>> //
			implements LossReporter<I, O> //
		{

			public GenericTypedLossReporter() {
				super(instance, env, conditions);
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
