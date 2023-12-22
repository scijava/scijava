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

import java.lang.reflect.Type;

import org.scijava.priority.Priority;
import org.scijava.types.SubTypeExtractor;
import org.scijava.types.TypeExtractor;
import org.scijava.types.TypeReifier;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.util.Util;

/**
 * {@link TypeExtractor} plugin which operates on
 * {@link RandomAccessibleInterval} objects.
 * <p>
 * Note that this {@link TypeExtractor} is low priority since we prefer subclass
 * TypeExtractors to take pre
 * </p>
 *
 * @author Gabriel Selzer
 */
public class RAITypeExtractor extends
	SubTypeExtractor<RandomAccessibleInterval<?>>
{

	@Override
	public double getPriority() {
		return Priority.LOW;
	}

	@Override
	protected Class<?> getRawType() {
		return RandomAccessibleInterval.class;
	}

	@Override
	protected Type[] getTypeParameters(TypeReifier r,
		RandomAccessibleInterval<?> object)
	{
		return new Type[] { r.reify(Util.getTypeFromInterval(object)) };
	}

}
