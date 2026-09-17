/*
 * #%L
 * Running things that declare their inputs and outputs.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.execute;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.struct.Member;
import org.scijava.struct.MemberParser;

/**
 * Builds {@link Member}s from the {@link Parameter}-annotated fields of a
 * class, including those it inherits.
 *
 * @author Curtis Rueden
 */
public class FieldParameterMemberParser implements
	MemberParser<Class<?>, Member<?>>
{

	private final Lookup lookup;

	public FieldParameterMemberParser() {
		this(null);
	}

	/**
	 * @param lookup a lookup with private access to the classes being parsed,
	 *          or null to reflect with this module's own access
	 */
	public FieldParameterMemberParser(final Lookup lookup) {
		this.lookup = lookup;
	}

	@Override
	public List<Member<?>> parse(final Class<?> source, final Type structType) {
		if (source == null) return List.of();
		final List<Member<?>> members = new ArrayList<>();
		// NB: walk up the hierarchy, so an abstract base class may declare
		// parameters shared by its subclasses.
		for (Class<?> c = source; c != null; c = c.getSuperclass()) {
			for (final var field : c.getDeclaredFields()) {
				if (!field.isAnnotationPresent(Parameter.class)) continue;
				members.add(new FieldParameterMember<>(field, structType, lookup));
			}
		}
		return members;
	}
}
