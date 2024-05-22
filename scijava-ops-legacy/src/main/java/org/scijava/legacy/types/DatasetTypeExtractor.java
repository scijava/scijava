/*-
 * #%L
 * Interoperability between SciJava Ops and ImageJ/ImageJ2.
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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

package org.scijava.legacy.types;

import net.imagej.Dataset;
import org.scijava.priority.Priority;
import org.scijava.types.extract.TypeExtractor;
import org.scijava.types.extract.TypeReifier;

import java.lang.reflect.Type;

public class DatasetTypeExtractor implements TypeExtractor {

	@Override
	public double getPriority() {
		return Priority.HIGH;
	}

	@Override
	public Class<?> baseClass() {
		return Dataset.class;
	}

	@Override
	public Type reify(TypeReifier r, Object object) {
		if (!(object instanceof Dataset)) throw new IllegalArgumentException(
			object + " cannot be reified because it is not a Dataset!");
		return r.reify(((Dataset) object).getImgPlus());
	}

}
