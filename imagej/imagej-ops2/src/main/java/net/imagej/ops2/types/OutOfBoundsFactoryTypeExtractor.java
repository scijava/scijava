/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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

import java.lang.reflect.Type;

import net.imglib2.img.ImgFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;

import org.scijava.priority.Priority;
import org.scijava.types.Any;
import org.scijava.types.TypeExtractor;
import org.scijava.types.TypeReifier;
import org.scijava.types.TypeTools;

/**
 * {@link TypeExtractor} plugin which operates on {@link OutOfBoundsFactory} objects.
 * <p>
 * For performance reasons, we examine only the first element of the iteration,
 * which may be a more specific type than later elements. Hence the generic type
 * given by this extraction may be overly constrained.
 * </p>
 *
 * @author Curtis Rueden
 */
public class OutOfBoundsFactoryTypeExtractor implements TypeExtractor {

	@Override public double getPriority() {
		return Priority.LOW;
	}

	@Override public boolean canReify(TypeReifier r, Class<?> object) {
		return OutOfBoundsFactory.class.isAssignableFrom(object);
	}

	@Override public Type reify(TypeReifier r, Object object) {
		if (!(object instanceof OutOfBoundsFactory))
			throw new IllegalArgumentException(this + " cannot reify " + object);
		return TypeTools.raiseParametersToClass(object.getClass(), OutOfBoundsFactory.class, new Type[] {new Any(), new Any()});
	}

}
