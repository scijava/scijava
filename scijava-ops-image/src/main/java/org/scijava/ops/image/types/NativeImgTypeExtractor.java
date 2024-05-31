/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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
import org.scijava.types.extract.SubTypeExtractor;
import org.scijava.types.extract.TypeExtractor;
import org.scijava.types.extract.TypeReifier;

import net.imglib2.img.NativeImg;

/**
 * {@link TypeExtractor} plugin which operates on {@link NativeImg} objects.
 * <p>
 * This TypeExtractor is high priority to allow precedence over the RAI
 * TypeExtractor, IterableTypeExtractor, etc. We cannot remove this
 * TypeExtractor in favor of those type extractors since this image type has a
 * second type variable (the data array) that should be reified (otherwise we
 * might have wildcards in the fully reified type).
 * </p>
 *
 * @author Gabriel Selzer
 */
public class NativeImgTypeExtractor extends SubTypeExtractor<NativeImg<?, ?>> {

	@Override
	public double priority() {
		return Priority.VERY_HIGH;
	}

	@Override
	public Class<?> baseClass() {
		return NativeImg.class;
	}

	@Override
	protected Type[] getTypeParameters(TypeReifier r, NativeImg<?, ?> object) {
		Type componentType = r.reify(object.firstElement());
		Type backingType = r.reify(object.update(object.cursor()));
		return new Type[] { componentType, backingType };
	}

}
