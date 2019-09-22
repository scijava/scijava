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

package net.imagej.ops.types;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsRandomValueFactory;

import org.scijava.ops.types.Any;
import org.scijava.ops.types.TypeExtractor;
import org.scijava.ops.types.TypeService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * {@link TypeExtractor} plugin which operates on
 * {@link OutOfBoundsConstantValueFactory} objects.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = TypeExtractor.class)
public class OutOfBoundsRandomValueFactoryTypeExtractor implements TypeExtractor<OutOfBoundsRandomValueFactory<?, ?>> {

	@Parameter
	private TypeService typeService;

	@Override
	public Type reify(final OutOfBoundsRandomValueFactory<?, ?> o, final int n) {
		if (n < 0 || n > 1)
			throw new IndexOutOfBoundsException();

		Object elementObject = new Any();
		try {
			Field elementField = o.getClass().getDeclaredField("value");
			elementField.setAccessible(true);
			elementObject = elementField.get(o);
		} catch (Exception e) {

		}
		Type elementType = typeService.reify(elementObject);
		if (n == 0)
			return elementType;
		// if we need the second type parameter, it can just be a
		// randomAccessibleInterval of elementType.
		Type elementRAI = Types.parameterize(RandomAccessibleInterval.class, new Type[] { elementType });
		return elementRAI;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Class<OutOfBoundsRandomValueFactory<?, ?>> getRawType() {
		return (Class) OutOfBoundsRandomValueFactory.class;
	}

}
