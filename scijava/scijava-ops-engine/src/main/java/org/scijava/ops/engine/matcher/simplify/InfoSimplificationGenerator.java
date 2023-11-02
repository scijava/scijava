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
package org.scijava.ops.engine.matcher.simplify;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.Ops;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.types.Any;
import org.scijava.types.Nil;
import org.scijava.types.Types;

public class InfoSimplificationGenerator {

	private final OpInfo info;
	private final List<RichOp<Function<?, ?>>> inFocusers;
	private final RichOp<Function<?, ?>> outSimplifier;

	private final RichOp<Computers.Arity1<?, ?>> copyOp;

	public InfoSimplificationGenerator(OpInfo info, OpEnvironment env) {
		Hints h = new Hints(BaseOpHints.Adaptation.FORBIDDEN,
				BaseOpHints.Simplification.FORBIDDEN);
		this.info = info;
		this.inFocusers = new ArrayList<>();
		Type[] args = info.inputTypes().toArray(Type[]::new);
		for(Type arg: args) {
			var inNil = Nil.of(arg);
			var focuser = env.unary("focus", h).inType(Any.class).outType(inNil).function();
			inFocusers.add(Ops.rich(focuser));
		}
		var outNil = Nil.of(info.outputType());
		var simplifier = env.unary("simplify", h).inType(outNil).outType(Object.class).function();
		this.outSimplifier = Ops.rich(simplifier);

		int ioIndex = SimplificationUtils.findMutableArgIndex(Types.raw(info.opType()));
		if (ioIndex > -1) {
			var nil = Nil.of(Ops.info(this.outSimplifier).outputType());
			var copier = env.unary("copy").inType(nil).outType(nil).computer();
			this.copyOp = Ops.rich(copier);
		}
		else {
			this.copyOp = null;
		}
	}

	public SimplifiedOpInfo generateSuitableInfo() {
		return new SimplifiedOpInfo(info, inFocusers, outSimplifier, copyOp);
	}

}
