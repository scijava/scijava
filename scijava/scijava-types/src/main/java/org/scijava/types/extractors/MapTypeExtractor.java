/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2019 SciJava Ops developers.
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

package org.scijava.types.extractors;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.scijava.priority.Priority;
import org.scijava.types.TypeExtractor;
import org.scijava.types.TypeReifier;
import org.scijava.types.TypeTools;
import org.scijava.types.Types;

/**
 * {@link TypeExtractor} plugin which operates on {@link Map} objects.
 * <p>
 * For performance reasons, we examine only one entry of the map, which may be
 * more specifically typed than later entries. Hence the generic types given by
 * this extraction may be overly constrained.
 * </p>
 *
 * @author Curtis Rueden
 */
public class MapTypeExtractor extends ParameterizedTypeExtractor {

	@Override
	public Type reify(final TypeReifier t, final Object object) {
		if (!(object instanceof Map)) throw new IllegalArgumentException(this + " is only capable of reifying Iterables!");

		// Obtain the element type using the TypeService.
		int typesToCheck = 100;
		// can we make this more efficient (possibly a parallel stream)?
		List<Entry<?, ?>> entries = new ArrayList<>(((Map<?, ?>) object).entrySet());
		entries = entries.subList(0, Math.min(typesToCheck, entries.size()));

		Type[] keyTypes = new Type[entries.size()];
		Type[] valueTypes = new Type[entries.size()];
		for (int i = 0; i < entries.size(); i++) {
			keyTypes[i] = t.reify(entries.get(i).getKey());
			valueTypes[i] = t.reify(entries.get(i).getValue());
		}

		Type keyType = Types.greatestCommonSuperType(keyTypes, true);
		Type valueType = Types.greatestCommonSuperType(valueTypes, true);
		return TypeTools.raiseParametersToClass(object.getClass(), Map.class, new Type[] {keyType, valueType});
	}

	@Override public double getPriority() {
		return Priority.VERY_LOW;
	}

	@Override public boolean canReify(TypeReifier r, Class<?> cls) {
		return Map.class.isAssignableFrom(cls);
	}
}
