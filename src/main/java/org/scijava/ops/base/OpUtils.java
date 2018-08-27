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

package org.scijava.ops.base;

import java.util.List;
import java.util.stream.Collectors;

import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * Utility methods for working with ops. In particular, this class contains
 * handy methods for generating human-readable strings describing ops and match
 * requests against them.
 * 
 * @author Curtis Rueden
 */
public final class OpUtils {

	private OpUtils() {
		// NB: prevent instantiation of utility class.
	}

	// -- Utility methods --

	public static Object[] args(final Object[] latter, final Object... former) {
		final Object[] result = new Object[former.length + latter.length];
		int i = 0;
		for (final Object o : former) {
			result[i++] = o;
		}
		for (final Object o : latter) {
			result[i++] = o;
		}
		return result;
	}

	public static List<MemberInstance<?>> inputs(StructInstance<?> op) {
		return op.members().stream() //
				.filter(memberInstance -> memberInstance.member().isInput()) //
				.collect(Collectors.toList());
	}

	public static List<Member<?>> inputs(OpCandidate candidate) {
		return inputs(candidate.struct());
	}

	public static List<Member<?>> inputs(final Struct struct) {
		return struct.members().stream() //
				.filter(member -> member.isInput()) //
				.collect(Collectors.toList());
	}

	public static List<Member<?>> outputs(OpCandidate candidate) {
		return outputs(candidate.struct());
	}

	public static List<Member<?>> outputs(final Struct struct) {
		return struct.members().stream() //
				.filter(member -> member.isOutput()) //
				.collect(Collectors.toList());
	}

	public static List<MemberInstance<?>> outputs(StructInstance<?> op) {
		return op.members().stream() //
				.filter(memberInstance -> memberInstance.member().isOutput()) //
				.collect(Collectors.toList());
	}
	
	public static double getPriority(final OpCandidate candidate) {
		// TODO: Think about what to do about non @Plugin-based ops...?
		// What if there is no annotation? How to discern a priority?
		return candidate.opInfo().getAnnotation().priority();
	}
}
