/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.conversionLoss.impl;

import org.scijava.ops.spi.OpHints;
import org.scijava.ops.engine.BaseOpHints.Conversion;
import org.scijava.ops.engine.conversionLoss.LossReporter;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.priority.Priority;
import org.scijava.types.Nil;

/**
 * A {@link LossReporter} used when a type is not converted.
 *
 * @author Gabriel Selzer
 * @param <T> - the type that is not being converted.
 * @see org.scijava.ops.engine.matcher.convert.IdentityCollection
 */
@OpHints(hints = { Conversion.FORBIDDEN })
@OpClass(names = "engine.lossReporter", priority = Priority.VERY_HIGH)
public class IdentityLossReporter<U, T extends U> implements LossReporter<T, U>,
	Op
{

	/**
	 * @param t the Nil describing the type that is being converted from
	 * @param u the Nil describing the type that is being converted to
	 * @return the worst-case loss converting from type T to type T (i.e. 0)
	 */
	@Override
	public Double apply(Nil<T> t, Nil<U> u) {
		return 0.;
	}

}
