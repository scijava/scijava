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

package org.scijava.ops.engine.copy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.scijava.priority.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class CopyOpCollection<T> implements OpCollection {

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Function<T[], T[]> copyGenericArrayFunction = //
		from -> Arrays.copyOf(from, from.length);

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Function<byte[], byte[]> copyByteArrayFunction = byte[]::clone;

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<byte[], byte[]> copyByteArray = (from, to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<Byte[], Byte[]> copyBoxedByteArray = (from,
		to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Function<short[], short[]> copyShortArrayFunction =
		short[]::clone;

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<short[], short[]> copyShortArray = (from,
		to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<Short[], Short[]> copyBoxedShortArray = (from,
		to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Function<int[], int[]> copyIntArrayFunction = int[]::clone;

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<int[], int[]> copyIntArray = (from, to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<Integer[], Integer[]> copyBoxedIntegerArray = (
		from, to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Function<long[], long[]> copyLongArrayFunction = long[]::clone;

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<long[], long[]> copyLongArray = (from, to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<Long[], Long[]> copyBoxedLongArray = (from,
		to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Function<float[], float[]> copyFloatArrayFunction =
		float[]::clone;

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<float[], float[]> copyFloatArray = (from,
		to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<Float[], Float[]> copyBoxedFloatArray = (from,
		to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Function<double[], double[]> copyDoubleArrayFunction =
		double[]::clone;

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<double[], double[]> copyDoubleArray = (from,
		to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.array, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<Double[], Double[]> copyBoxedDoubleArray = (
		from, to) -> {
        var arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "copy.list, engine.copy", params = "array, arrayCopy")
	public final Computers.Arity1<List<T>, List<T>> copyList = (from, to) -> {
		to.clear();
		to.addAll(from);
	};

	@OpField(names = "copy.list, engine.copy", params = "array, arrayCopy")
	public final Function<List<T>, List<T>> copyListFunction = ArrayList::new;
}
