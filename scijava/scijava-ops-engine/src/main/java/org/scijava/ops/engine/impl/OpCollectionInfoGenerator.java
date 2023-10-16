/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.scijava.common3.Annotations;
import org.scijava.meta.Versions;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpInfoGenerator;
import org.scijava.ops.engine.matcher.impl.OpFieldInfo;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.ops.engine.util.Ops;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpHints;
import org.scijava.ops.spi.OpMethod;

public class OpCollectionInfoGenerator implements OpInfoGenerator {

	private Hints formHints(OpHints h) {
		if (h == null) return new Hints();
		return new Hints(h.hints());
	}

	protected List<OpInfo> processClass(Class<?> cls) {
		String version = Versions.getVersion(cls);
		List<OpInfo> collectionInfos = new ArrayList<>();

		// add OpFieldInfos
		final List<Field> fields = Annotations.getAnnotatedFields(cls,
			OpField.class);
		final Optional<Object> instance = getInstance(cls);
		if (instance.isPresent()) {
			final List<OpFieldInfo> fieldInfos = //
				fields.parallelStream() //
					.map(f -> generateFieldInfo(f, instance.get(), version)) //
					.collect(Collectors.toList());
			collectionInfos.addAll(fieldInfos);
		}
		// add OpMethodInfos
		//
		final List<OpMethodInfo> methodInfos = //
			Annotations.getAnnotatedMethods(cls, OpMethod.class).parallelStream() //
				.map(m -> generateMethodInfo(m, version)) //
				.collect(Collectors.toList());
		collectionInfos.addAll(methodInfos);
		return collectionInfos;
	}

	private Optional<Object> getInstance(Class<?> c) {
		try {
			return Optional.of(c.getDeclaredConstructor().newInstance());
		}
		catch (Exception exc) {
			return Optional.empty();
		}
	}

	private OpFieldInfo generateFieldInfo(Field field, Object instance,
		String version)
	{
		final boolean isStatic = Modifier.isStatic(field.getModifiers());
		OpField annotation = field.getAnnotation(OpField.class);
		String unparsedOpNames = annotation.names();
		String[] parsedOpNames = Ops.parseOpNames(unparsedOpNames);
		double priority = annotation.priority();
		Hints hints = formHints(field.getAnnotation(OpHints.class));
		return new OpFieldInfo(isStatic ? null : instance, field, version, hints,
			priority, parsedOpNames);
	}

	private OpMethodInfo generateMethodInfo(Method method, String version) {
		OpMethod annotation = method.getAnnotation(OpMethod.class);
		Class<?> opType = annotation.type();
		String unparsedOpNames = annotation.names();
		String[] parsedOpNames = Ops.parseOpNames(unparsedOpNames);
		Hints hints = formHints(method.getAnnotation(OpHints.class));
		double priority = annotation.priority();
		return new OpMethodInfo(method, opType, version, hints, priority,
			parsedOpNames);
	}

	@Override public boolean canGenerateFrom(Object o) {
		return o instanceof OpCollection;
	}

	@Override public List<OpInfo> generateInfosFrom(Object o) {
		return processClass(o.getClass());
	}
}
