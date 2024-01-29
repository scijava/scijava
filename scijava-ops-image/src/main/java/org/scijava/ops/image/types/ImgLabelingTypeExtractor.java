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

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.view.Views;

/**
 * {@link TypeExtractor} plugin which operates on {@link ImgLabeling} objects.
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public class ImgLabelingTypeExtractor extends
	SubTypeExtractor<ImgLabeling<?, ?>>
{

	@Override
	protected Class<?> getRawType() {
		return ImgLabeling.class;
	}

	@Override
	protected Type[] getTypeParameters(TypeReifier r, ImgLabeling<?, ?> object) {
		// o.firstElement will return a LabelingType
		Type labelingType = r.reify(object.firstElement());
		// sanity check
		if (!(labelingType instanceof ParameterizedType))
			throw new IllegalArgumentException(
				"ImgLabeling is not of a LabelingType");
		// get type arg of labelingType
		ParameterizedType pType = (ParameterizedType) labelingType;
		Type mappingType = pType.getActualTypeArguments()[0];
		// otherwise n == 1
		Type elementType = r.reify(Views.iterable(object.getSource())
			.firstElement());
		return new Type[] { mappingType, elementType };
	}

}
