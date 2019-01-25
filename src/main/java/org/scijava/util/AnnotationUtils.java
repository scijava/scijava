/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.scijava.param.Parameter;
import org.scijava.param.Parameters;

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
	 * Gets a list of {@link Parameter} from the specified annotated element.
	 * If the element is not annotated, an empty array is returned.
	 * 
	 * @param element the annotated element
	 * @return array of {@link Parameter} annotations
	 */
	public static Parameter[] parameters(final AnnotatedElement element) {
		final Parameters params = element.getAnnotation(Parameters.class);
		if (params != null) {
			return params.value();
		}
		final Parameter p = element.getAnnotation(Parameter.class);
		return p == null ? new Parameter[0] : new Parameter[] { p };
	}

	/**
	 * Changes the annotation value for the given key of the given annotation to
	 * newValue and returns the previous value.
	 * 
	 * Taken and modified from: https://stackoverflow.com/a/28118436
	 * 
	 * @param instance the annotation to mutate
	 * @param key the key of the field to mutate
	 * @param newValue the new value to set
	 * @return the old value of the field with the specified key
	 */
	@SuppressWarnings("unchecked")
	public static Object mutateAnnotationInstance(Annotation instance, String key, Object newValue) {
		Object handler = Proxy.getInvocationHandler(instance);
		Field f;
		try {
			f = handler.getClass().getDeclaredField("memberValues");
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException(e);
		}
		f.setAccessible(true);
		Map<String, Object> memberValues;
		try {
			memberValues = (Map<String, Object>) f.get(handler);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		Object oldValue = memberValues.get(key);
		if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
			throw new IllegalArgumentException();
		}
		memberValues.put(key, newValue);
		return oldValue;
	}
}
