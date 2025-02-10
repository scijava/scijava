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

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.scijava.common3.Any;
import org.scijava.common3.Types;
import org.scijava.types.extract.SubTypeExtractor;
import org.scijava.types.extract.TypeExtractor;
import org.scijava.types.extract.TypeReifier;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsRandomValueFactory;

/**
 * {@link TypeExtractor} plugin which operates on
 * {@link OutOfBoundsRandomValueFactory} objects.
 *
 * @author Gabriel Selzer
 */
public class OutOfBoundsRandomValueFactoryTypeExtractor extends
	SubTypeExtractor<OutOfBoundsRandomValueFactory<?, ?>>
{

	@Override
	public Class<?> baseClass() {
		return OutOfBoundsRandomValueFactory.class;
	}

	@Override
	protected Type[] getTypeParameters(TypeReifier r,
		OutOfBoundsRandomValueFactory<?, ?> object)
	{
		Object elementObject;
		try {
            var elementField = object.getClass().getDeclaredField("value");
			elementField.setAccessible(true);
			elementObject = elementField.get(object);
		}
		catch (Exception e) {
			elementObject = new Any();
		}
        var elementType = r.reify(elementObject);
		// if we need the second type parameter, it can just be a
		// randomAccessibleInterval of elementType.
		Type raiType = Types.parameterize(RandomAccessibleInterval.class,
			new Type[] { elementType });
		return new Type[] { elementType, raiType };
	}

}
