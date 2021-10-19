/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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

package net.imagej.ops2.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import net.imglib2.roi.labeling.LabelingMapping;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.types.TypeExtractor;
import org.scijava.types.TypeReifier;

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
@Plugin(type = TypeExtractor.class, priority = Priority.LOW_PRIORITY)
public class LabelingMappingTypeExtractor implements TypeExtractor<LabelingMapping<?>> {

	@Override
	public Type reify(final TypeReifier t, final LabelingMapping<?> o, final int n) {
		if (n != 0)
			throw new IndexOutOfBoundsException();

		// determine the type arg of the mapping through looking at the Set of Labels
		// (o.getLabels() returns a Set<T>, which can be reified by another TypeService
		// plugin).
		Type labelingMappingSet = t.reify(o.getLabels());
		// sanity check, argType will always be a set so argType should always be a
		// ParameterizedType
		if (!(labelingMappingSet instanceof ParameterizedType))
			throw new IllegalArgumentException("Impossible LabelingMapping provided as input");
		// The type arg of argType is the same type arg of o.
		return ((ParameterizedType) labelingMappingSet).getActualTypeArguments()[0];
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Class<LabelingMapping<?>> getRawType() {
		return (Class) LabelingMapping.class;
	}

}
