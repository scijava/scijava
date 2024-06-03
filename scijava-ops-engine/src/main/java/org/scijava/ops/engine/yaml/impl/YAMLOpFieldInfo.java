/*
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.yaml.impl;

import org.scijava.common3.Classes;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.engine.struct.SynthesizedParameterMember;
import org.scijava.ops.engine.util.Infos;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An {@link OpInfo}, backed by some {@code public final} {@link Field},
 * described via YAML that is stored within a {@link Map}.
 *
 * @author Gabriel Selzer
 */
public class YAMLOpFieldInfo extends AbstractYAMLOpInfo {

	private final Object instance;
	private final Field field;
	private final Struct struct;

	public YAMLOpFieldInfo( //
		final Map<String, Object> yaml, //
		final String identifier //
	) throws Exception {
		super(yaml, identifier);

		// parse class
		int clsIndex = identifier.indexOf('$');
		String clsString = identifier.substring(0, clsIndex);
		Class<?> cls = Classes.load(clsString);
		this.instance = cls.getConstructor().newInstance();
		// parse Field
		String fieldString = identifier.substring(clsIndex + 1);
		this.field = cls.getDeclaredField(fieldString);
		// parse Struct
		this.struct = createStruct(yaml);

		// Validate general OpInfo features
		Infos.validate(this);
	}

	// -- OpInfo methods --

	@Override
	public Type opType() {
		return field.getGenericType();
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public String implementationName() {
		// Get generic string without modifiers and return type
		String fullyQualifiedField = field.toGenericString();
		int lastDotPos = fullyQualifiedField.lastIndexOf('.');
		fullyQualifiedField = fullyQualifiedField.substring(0, lastDotPos) + "$" +
			fullyQualifiedField.substring(lastDotPos + 1);
		String packageName = field.getDeclaringClass().getPackageName();
		int classNameIndex = fullyQualifiedField.lastIndexOf(packageName);
		return fullyQualifiedField.substring(classNameIndex);
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return field;
	}

	/**
	 * For a {@link Class}, we define the implementation as the concatenation of:
	 * <ol>
	 * <li>The fully qualified name of the class</li>
	 * <li>The version of the class containing the field, with a preceding
	 * {@code @}</li>
	 * </ol>
	 * <p>
	 * For example, for a field class {@code com.example.foo.Bar}, you might have
	 * <p>
	 * {@code com.example.foo.Bar@1.0.0}
	 * <p>
	 */
	@Override
	public String id() {
		return OpInfo.IMPL_DECLARATION + implementationName() + "@" + version();
	}

	private Struct createStruct(Map<String, Object> yaml) {
		List<Member<?>> members = new ArrayList<>();
		List<Map<String, Object>> params = (List<Map<String, Object>>) yaml.get(
			"parameters");
		var fmts = FunctionalParameters.findFunctionalMethodTypes(opType());
		for (int i = 0; i < params.size(); i++) {
			var pMap = params.get(i);
			var fmt = fmts.get(i);
			String name = (String) pMap.get("name");
			String description = (String) pMap.get("description");
			boolean nullable = (boolean) pMap.getOrDefault("nullable", false);
			members.add(new SynthesizedParameterMember<>(fmt, name, !nullable,
				description));
		}

		return () -> members;
	}

	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies) {
		// NB: dependencies are not allowed on field Ops, since field Ops can only
		// point to a single instance, which allows successive matching calls to
		// overwrite the dependencies used on earlier matching calls.
		// This can happen if (a) a single Field is matched multiple times, using
		// different dependencies, or if (b) multiple fields point to the same
		// object.
		if (dependencies != null && !dependencies.isEmpty())
			throw new IllegalArgumentException(
				"Op fields are not allowed to have any Op dependencies.");
		// NB: In general, there is no way to create a new instance of the field
		// value.
		// Calling clone() may or may not work; it does not work with e.g. lambdas.
		// Better to just use the same value directly, rather than trying to copy.
		try {
			final Object object = field.get(instance);
			// TODO: Wrap object in a generic holder with the same interface.
			return struct().createInstance(object);
		}
		catch (final IllegalAccessException exc) {
			throw new RuntimeException(exc);
		}
	}

}
