/*-
 * #%L
 * A library for building and introspecting structs.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

package org.scijava.struct;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A structure consisting of typed fields called {@link Member}s.
 *
 * @author Curtis Rueden
 * @author Christian Dietz
 */
public interface Struct extends Iterable<Member<?>> {

	List<Member<?>> members();

	@Override
	default Iterator<Member<?>> iterator() {
		return members().iterator();
	}

	default <C> StructInstance<C> createInstance(final C object) {
		final LinkedHashMap<String, MemberInstance<?>> memberMap;
		memberMap = new LinkedHashMap<>();
		for (final var member : members()) {
			memberMap.put(member.key(), member.createInstance(object));
		}

		return new StructInstance<>() {
			@Override
			public List<MemberInstance<?>> members() {
				return memberMap.values().stream().collect(Collectors.toList());
			}

			@Override
			public Struct struct() {
				return Struct.this;
			}

			@Override
			public C object() {
				return object;
			}

			@Override
			public MemberInstance<?> member(final String key) {
				return memberMap.get(key);
			}
		};
	}
}
