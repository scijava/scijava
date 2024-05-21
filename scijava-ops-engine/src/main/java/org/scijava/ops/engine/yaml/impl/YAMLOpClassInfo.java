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

import org.scijava.common3.Annotations;
import org.scijava.common3.Classes;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.OpDependencyMember;
import org.scijava.ops.engine.exceptions.impl.FinalOpDependencyFieldException;
import org.scijava.ops.engine.struct.FieldOpDependencyMember;
import org.scijava.ops.engine.struct.FunctionalParameters;
import org.scijava.ops.engine.struct.SynthesizedParameterMember;
import org.scijava.ops.engine.util.Infos;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.types.Types;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An {@link OpInfo}, backed by some {@code public} {@link Class}, described via
 * YAML that is stored within a {@link Map}.
 *
 * @author Gabriel Selzer
 */
public class YAMLOpClassInfo extends AbstractYAMLOpInfo {

	private final Class<?> cls;
	private final Struct struct;

	public YAMLOpClassInfo( //
		final Map<String, Object> yaml, //
		final String identifier)
	{
		super(yaml, identifier);
		// parse class
		this.cls = Classes.load(identifier);
		this.struct = createStruct(yaml);

		// Validate general OpInfo features
		Infos.validate(this);
	}

	// -- OpInfo methods --

	@Override
	public Type opType() {
		return Types.parameterizeRaw(cls);
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public String implementationName() {
		return cls.getName();
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return cls;
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
		var fmts = FunctionalParameters.findFunctionalMethodTypes(cls);
		for (int i = 0; i < params.size(); i++) {
			var pMap = params.get(i);
			var fmt = fmts.get(i);
			String name = (String) pMap.get("name");
			String description = (String) pMap.get("description");
			boolean nullable = (boolean) pMap.getOrDefault("nullable", false);
			members.add(new SynthesizedParameterMember<>(fmt, name, !nullable,
				description));
		}

		// Add Op Dependencies
		final List<Field> fields = Annotations.annotatedFields(cls,
			OpDependency.class);
		for (final Field f : fields) {
			f.setAccessible(true);
			if (Modifier.isFinal(f.getModifiers())) {
				// Final fields are bad because they cannot be modified.
				throw new FinalOpDependencyFieldException(f);
			}
			final FieldOpDependencyMember<?> item = new FieldOpDependencyMember<>(f,
				cls);
			members.add(item);
		}

		return () -> members;
	}

	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies) {
		final Object op;
		try {
			Constructor<?> ctor = cls.getDeclaredConstructor();
			ctor.setAccessible(true);
			op = ctor.newInstance();
		}
		catch (final InstantiationException | IllegalAccessException
				| NoSuchMethodException | SecurityException | IllegalArgumentException
				| InvocationTargetException e)
		{
			throw new IllegalStateException("Unable to instantiate op: '" + cls
				.getName() + "' Ensure that the Op has a no-args constructor.", e);
		}
		final var dependencyMembers = Infos.dependencies(this);
		for (int i = 0; i < dependencyMembers.size(); i++) {
			final OpDependencyMember<?> dependencyMember = dependencyMembers.get(i);
			try {
				dependencyMember.createInstance(op).set(dependencies.get(i));
			}
			catch (final Exception ex) {
				// TODO: Improve error message. Used to include exact OpRequest of Op
				// dependency.
				throw new IllegalStateException(
					"Exception trying to inject Op dependency field.\n" +
						"\tOp dependency field to resolve: " + dependencyMember.getKey() +
						"\n" + "\tFound Op to inject: " + dependencies.get(i).getClass()
							.getName() + //
						"\n" + "\tField signature: " + dependencyMember.getType(), ex);
			}
		}
		return struct().createInstance(op);
	}

}
