/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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
import org.scijava.common3.Any;
import org.scijava.types.extract.SubTypeExtractor;
import org.scijava.types.extract.TypeExtractor;
import org.scijava.types.extract.TypeReifier;

import net.imglib2.outofbounds.OutOfBoundsFactory;

/**
 * {@link TypeExtractor} plugin which operates on {@link OutOfBoundsFactory}
 * objects.
 * <p>
 * For performance reasons, we examine only the first element of the iteration,
 * which may be a more specific type than later elements. Hence the generic type
 * given by this extraction may be overly constrained.
 * </p>
 *
 * @author Curtis Rueden
 */
public class OutOfBoundsFactoryTypeExtractor extends
	SubTypeExtractor<OutOfBoundsFactory<?, ?>>
{

	@Override
	public double priority() {
		return Priority.LOW;
	}

	@Override
	public Class<?> baseClass() {
		return OutOfBoundsFactory.class;
	}

	@Override
	protected Type[] getTypeParameters(TypeReifier r,
		OutOfBoundsFactory<?, ?> object)
	{
		return new Type[] { new Any(), new Any() };
	}

}
