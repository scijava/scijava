/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.eval;

import java.util.Map;

import org.scijava.ops.OpService;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Functions;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

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
@Plugin(type = Op.class, name = "eval")
@Parameter(key = "input")
@Parameter(key = "vars")
@Parameter(key = "opService")
@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
public class DefaultEval implements Functions.Arity3<String, Map<String, Object>, OpService, Object>
{

	@Override
	public Object apply(final String input, final Map<String, Object> vars, final OpService ops) {
		OpEvaluator e = new OpEvaluator(ops);
		if (vars != null) e.setAll(vars);
		return e.evaluate(input);
	}

}
