/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
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

package org.scijava.ops.transform;

import java.util.Collection;
import java.util.Collections;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;

/**
 * Specialization of {@link OpTransformer} that transform Ops of a fixed source
 * type {@code S} into Ops of a fixed target type {@code T}. Represents a
 * one-to-one mapping between those Op types.
 *
 * @param <S> The type of the source Ops.
 * @param <T> The type of the target Ops.
 * @author Marcel Wiedenmann
 */
public interface OpMapper<S, T> extends OpTransformer {

	/**
	 * @return The(possibly raw) source type which this mapper transforms.
	 */
	Class<S> srcClass();

	/**
	 * @return The (possibly raw) target type into which this mapper transforms.
	 */
	Class<T> targetClass();

	/**
	 * Type safe specialization of {@link #transform(OpService, Object, OpRef)}.
	 */
	T transformTypesafe(final OpService opService, final S src, final OpRef targetRef) throws OpTransformationException;

	/**
	 * The default implementation of {@link OpMapper} delegates to
	 * {@link #transformTypesafe(OpService, Object, OpRef)} after performing type
	 * checks.
	 * <P>
	 * {@inheritDoc}
	 *
	 * @throws OpTransformationException {@inheritDoc} This includes cases where
	 *           the type of {@code src} is not compatible to {@link #srcClass()}.
	 */
	@Override
	default Object transform(final OpService opService, final Object src, final OpRef targetRef)
		throws OpTransformationException
	{
		if (srcClass().isAssignableFrom(src.getClass())) {
			@SuppressWarnings("unchecked")
			final S typedSrc = (S) src;
			return transformTypesafe(opService, typedSrc, targetRef);
		}
		throw new OpTransformationException("Object to transform: " + src.getClass().getName() +
			" is not of required class: " + srcClass().getName());
	}

	/**
	 * One-to-one specialization of {@link #getRefsTransformingTo(OpRef)}. Returns
	 * a non-{@code null} source if one could be found and {@code null} otherwise.
	 */
	OpRef getRefTransformingTo(OpRef targetRef);

	/**
	 * The default implementation of {@link OpMapper} delegates to
	 * {@link #getRefTransformingTo(OpRef)}. The returned collection is singleton
	 * if a source could be found and empty otherwise.
	 * <P>
	 * {@inheritDoc}
	 */
	@Override
	default Collection<OpRef> getRefsTransformingTo(final OpRef targetRef) {
		final OpRef sourceRef = getRefTransformingTo(targetRef);
		return sourceRef != null ? Collections.singletonList(sourceRef) : Collections.emptyList();
	}
}
