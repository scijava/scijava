/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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

package org.scijava.ops.engine.describe;

import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.priority.Priority;
import org.scijava.types.Nil;
import org.scijava.common3.Types;

import java.util.List;
import java.util.function.Function;

/**
 * {@code engine.describe} Ops pertaining to built-in Java classes.
 *
 * @param <T>
 * @param <N>
 * @author Gabriel Selzer
 */
public class BaseDescriptors<T, N extends Number> implements OpCollection {

	@OpField(names = "engine.describe")
	public final TypeDescriptor<N> boxedPrimitiveDescriptor = in -> "number";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<byte[]> byteArrayDescriptor = in -> "number[]";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<short[]> shortArrayDescriptor = in -> "number[]";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<int[]> intArrayDescriptor = in -> "number[]";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<long[]> longArrayDescriptor = in -> "number[]";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<float[]> floatArrayDescriptor = in -> "number[]";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<double[]> doubleArrayDescriptor =
		in -> "number[]";

	@OpMethod(names = "engine.describe", type = Function.class)
	public static <T> String arrayDescriptor( //
		@OpDependency(name = "engine.describe") Function<Nil<T>, String> dep, //
		Nil<T[]> in //
	) {
		return dep.apply(new Nil<>() {}) + "[]";
	}

	@OpMethod(names = "engine.describe", type = Function.class)
	public static <T> String listDescriptor( //
		@OpDependency(name = "engine.describe") Function<Nil<T>, String> dep, //
		Nil<List<T>> in //
	) {
		return "list<" + dep.apply(new Nil<>() {}) + ">";
	}

}
