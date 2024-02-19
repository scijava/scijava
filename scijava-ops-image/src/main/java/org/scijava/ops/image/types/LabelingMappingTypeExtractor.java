/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.image.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.scijava.types.SubTypeExtractor;
import org.scijava.types.TypeExtractor;
import org.scijava.types.TypeReifier;

import net.imglib2.roi.labeling.LabelingMapping;

/**
 * {@link TypeExtractor} plugin which operates on {@link Iterable} objects.
 * <p>
 * For performance reasons, we examine only the first element of the iteration,
 * which may be a more specific type than later elements. Hence the generic type
 * given by this extraction may be overly constrained.
 * </p>
 *
 * @author Curtis Rueden
 */
public class LabelingMappingTypeExtractor extends
	SubTypeExtractor<LabelingMapping<?>>
{

	@Override
	public Class<?> baseClass() {
		return LabelingMapping.class;
	}

	@Override
	protected Type[] getTypeParameters(TypeReifier r, LabelingMapping<?> object) {
		// determine the type arg of the mapping through looking at the Set of
		// Labels (object.getLabels() returns a Set<T>, which can be reified by
		// another TypeService plugin).
		Type labelingMappingSet = r.reify(object.getLabels());
		// sanity check, argType will always be a set so argType should always be a
		// ParameterizedType
		if (!(labelingMappingSet instanceof ParameterizedType))
			throw new IllegalArgumentException(
				"Impossible LabelingMapping provided as input");
		// The type arg of argType is the same type arg of o.
		Type elementType = ((ParameterizedType) labelingMappingSet)
			.getActualTypeArguments()[0];
		return new Type[] { elementType };
	}

}
