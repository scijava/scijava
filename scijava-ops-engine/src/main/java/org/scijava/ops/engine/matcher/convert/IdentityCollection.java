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

package org.scijava.ops.engine.matcher.convert;

import org.scijava.function.Inplaces;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.BaseOpHints.Conversion;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpHints;
import org.scijava.priority.Priority;

import java.util.function.Function;

/**
 * An {@link OpCollection} containing {@code identity} Ops.
 *
 * @author Gabriel Selzer
 * @param <T>
 */
public class IdentityCollection<T> implements OpCollection {

	/**
	 * @input t the object to be converted
	 * @output the converted object (since we are doing an identity conversion,
	 *         this is just a reference to the input object).
	 */
	@OpHints(hints = { Conversion.FORBIDDEN,
		BaseOpHints.DependencyMatching.FORBIDDEN })
	@OpField(names = "engine.convert, engine.identity", priority = Priority.FIRST)
	public final Function<T, T> identity = (t) -> t;

	/**
	 * @mutable t the object to be "mutated"
	 */
	@OpHints(hints = { Conversion.FORBIDDEN })
	@OpField(names = "engine.identity", priority = Priority.FIRST)
	public final Inplaces.Arity1<T> inplace = (t) -> {};

}
