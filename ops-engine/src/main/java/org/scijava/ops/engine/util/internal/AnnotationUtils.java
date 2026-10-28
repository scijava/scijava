/*
 * #%L
 * Java implementation of the SciJava Ops matching engine.
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

// File path shortening code adapted from:
// from: http://www.rgagnon.com/javadetails/java-0661.html

package org.scijava.ops.engine.util.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;

/**
 * Useful methods for working with annotation instances.
 *
 * @author David Kolb
 */
public final class AnnotationUtils {

	private AnnotationUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Attempt to retrieve the specified annotation from the i'th parameter of the
	 * specified method. This method will only find annotations with:
	 *
	 * <pre>
	 * &#64;Target(ElementType.TYPE_USE)
	 * </pre>
	 *
	 * If the ElementType is different or no annotation with specified type is
	 * present, null is returned.
	 *
	 * @param method
	 * @param i
	 * @param annotationClass
	 * @return
	 */
	public static <A extends Annotation> A getMethodParameterAnnotation(
		Method method, int i, Class<A> annotationClass)
	{
        var params = method.getAnnotatedParameterTypes();
		if (i >= params.length) {
			return null;
		}
		return params[i].getAnnotation(annotationClass);
	}
}
