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

package org.scijava.ops.engine.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.common3.Annotations;
import org.scijava.ops.engine.exceptions.impl.FinalOpDependencyFieldException;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.MemberParser;

public class ClassOpDependencyMemberParser implements
	MemberParser<Class<?>, FieldOpDependencyMember<?>>
{

	@Override
	public List<FieldOpDependencyMember<?>> parse(Class<?> source,
		Type structType)
	{
		if (source == null) return null;

		final var items = new ArrayList<FieldOpDependencyMember<?>>();

		// NB: Reject abstract classes.
		org.scijava.struct.Structs.checkModifiers(source.getName() + ": ", source
			.getModifiers(), true, Modifier.ABSTRACT);

		// Parse field level @OpDependency annotations.
		parseFieldOpDependencies(items, source);

		return items;
	}

	private static void parseFieldOpDependencies(
		final List<FieldOpDependencyMember<?>> items, Class<?> annotatedClass)
	{
		final var fields = Annotations.annotatedFields(annotatedClass,
			OpDependency.class);
		for (final var f : fields) {
			f.setAccessible(true);
			if (Modifier.isFinal(f.getModifiers())) {
				// Final fields are bad because they cannot be modified.
				throw new FinalOpDependencyFieldException(f);
			}
			final FieldOpDependencyMember<?> item = new FieldOpDependencyMember<>(f,
				annotatedClass);
			items.add(item);
		}
	}

}
