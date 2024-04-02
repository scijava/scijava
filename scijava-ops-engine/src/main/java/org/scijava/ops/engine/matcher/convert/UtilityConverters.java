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

package org.scijava.ops.engine.matcher.convert;

import org.scijava.function.Computers;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpHints;
import org.scijava.ops.spi.OpMethod;
import org.scijava.priority.Priority;

import java.util.function.Function;

/**
 * Ops useful in Op conversion.
 *
 * @author Gabriel Selzer
 */
public class UtilityConverters implements OpCollection {

	@OpHints(hints = { //
		BaseOpHints.Conversion.FORBIDDEN, //
		BaseOpHints.Adaptation.FORBIDDEN, //
		BaseOpHints.DependencyMatching.FORBIDDEN //
	})
	@OpMethod( //
		names = "engine.convert", //
		type = Function.class, //
		priority = Priority.LAST //
	)
	public static <T, U, V> V combo( //
		@OpDependency(name = "engine.convert") Function<T, U> c1, //
		@OpDependency(name = "engine.convert") Function<U, V> c2, //
		T in //
	) {
		return c2.apply(c1.apply(in));
	}

	@OpHints(hints = { //
		BaseOpHints.Conversion.FORBIDDEN, //
		BaseOpHints.Adaptation.FORBIDDEN, //
		BaseOpHints.DependencyMatching.FORBIDDEN //
	})
	@OpMethod( //
		names = "engine.copy", //
		type = Computers.Arity1.class, //
		priority = Priority.LAST //
	)
	public static <T, U, V> void comboCopier( //
		@OpDependency(name = "engine.convert") Function<T, U> c1, //
		@OpDependency(name = "engine.copy") Computers.Arity1<U, V> c2, //
		T in, //
		V out //
	) {
		c2.compute(c1.apply(in), out);
	}

}
