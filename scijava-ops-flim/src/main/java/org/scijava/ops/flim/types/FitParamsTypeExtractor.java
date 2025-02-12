/*-
 * #%L
 * Fluorescence lifetime analysis in SciJava Ops.
 * %%
 * Copyright (C) 2024 - 2025 SciJava developers.
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

package org.scijava.ops.flim.types;

import net.imglib2.util.Util;
import org.scijava.ops.flim.FitParams;
import org.scijava.types.extract.SubTypeExtractor;
import org.scijava.types.extract.TypeReifier;

import java.lang.reflect.Type;

/**
 * A {@link org.scijava.types.extract.TypeExtractor} that reifies {@link FitParams}.
 * Useful for directly passing {@code FitParams} objects directly to Op calls.
 *
 * @author Gabriel Selzer
 */
public class FitParamsTypeExtractor extends SubTypeExtractor<FitParams<?>> {

	@Override
	public Class<?> baseClass() {
		return FitParams.class;
	}

	@Override
	protected Type[] getTypeParameters(TypeReifier r, FitParams<?> object) {
        var elementType = r.reify(Util.getTypeFromInterval(object.transMap));
		return new Type[] { elementType };
	}

}
