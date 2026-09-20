/*-
 * #%L
 * SciJava library for generic type reasoning.
 * %%
 * Copyright (C) 2016 - 2025 SciJava developers.
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
package org.scijava.types.infer;

import org.scijava.common3.Any;
import org.scijava.common3.Types;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author Gabriel Selzer
 */
class AnyTypeMapping extends TypeMapping {

    private final Any boundingAny;

    AnyTypeMapping(TypeVariable<?> typeVar, Type any,
                        boolean malleable)
    {
        super(typeVar, any, malleable);
        this.boundingAny = any instanceof Any ? (Any) any : new Any(typeVar.getBounds());
    }

    @Override
    public void refine(Type newType, boolean malleable) {
        super.refine(newType, malleable);
        for(Type t: boundingAny.getLowerBounds()) {
            if (!Types.isAssignable(t, newType)) {
                throw new TypeInferenceException("The new type " + newType + " is not a parent of the Any bound " + t);
            }
        }
        for(Type t: boundingAny.getUpperBounds()) {
            if (!Types.isAssignable(newType, t)) {
                throw new TypeInferenceException("The new type " + newType + " is not a child of the Any bound " + t);
            }
        }
    }
}
