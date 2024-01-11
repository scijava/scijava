/*
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

package org.scijava.ops.engine.eval;

import java.util.Map;

import org.scijava.function.Functions;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;

/**
 * Evaluates an expression.
 * <p>
 * The expression is parsed using
 * <a href="https://github.com/scijava/parsington">Parsington</a>, the SciJava
 * expression parsing library, then evaluated by invoking available ops.
 * </p>
 * 
 * @author Curtis Rueden
 * @see OpEvaluator
 */
@OpClass(names = "eval")
public class DefaultEval implements
	Functions.Arity3<String, OpEnvironment, Map<String, ?>, Object>, Op
{

	/**
	 * TODO
	 *
	 * @param input
	 * @param vars
	 * @param ops
	 * @return the output
	 */
	@Override
	public Object apply( //
		final String input, //
		final OpEnvironment ops, //
		@Nullable final Map<String, ?> vars //
	) {
		OpEvaluator e = new OpEvaluator(ops);
		if (vars != null) e.setAll(vars);
		return e.evaluate(input);
	}

}
