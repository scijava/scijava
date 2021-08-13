/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.Priority;
import org.scijava.ValidityProblem;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpDependencyMember;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.api.OpHints;
import org.scijava.ops.engine.hint.ImmutableHints;
import org.scijava.ops.engine.struct.ClassOpDependencyMemberParser;
import org.scijava.ops.engine.struct.ClassParameterMemberParser;
import org.scijava.plugin.Plugin;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;
import org.scijava.struct.ValidityException;
import org.scijava.types.Types;
import org.scijava.util.VersionUtils;

/**
 * Metadata about an op implementation defined as a class.
 * 
 * @author Curtis Rueden
 * @author David Kolb
 */
public class OpClassInfo implements OpInfo {

	private final List<String> names;
	private final Class<?> opClass;
	private final String version;
	private Struct struct;
	private ValidityException validityException;
	private final double priority;
	private final Hints hints;

	public OpClassInfo(final Class<?> opClass, final String version, final String... names) {
		this(opClass, version, priorityFromAnnotation(opClass), names);
	}

	public OpClassInfo(final Class<?> opClass, final String version, final double priority, final String... names) {
		this.opClass = opClass;
		this.version = version;
		this.names = Arrays.asList(names);
		List<ValidityProblem> problems = new ArrayList<>();
		try {
			struct = Structs.from(opClass, problems, new ClassParameterMemberParser(), new ClassOpDependencyMemberParser());
			OpUtils.checkHasSingleOutput(struct);
		} catch (ValidityException e) {
			validityException = e;
		} 
		this.priority = priority;

		hints = formHints(opClass.getAnnotation(OpHints.class));
	}

	// -- OpInfo methods --

	@Override
	public List<String> names() {
		return names;
	}

	@Override
	public Type opType() {
		// TODO: Check whether this is correct!
		return Types.parameterizeRaw(opClass);
		//return opClass;
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public Hints declaredHints() {
		return hints;
	}

	@Override
	public double priority() {
		return priority;
	}

	@Override
	public String implementationName() {
		return opClass.getName();
	}

	@Override
	public StructInstance<?> createOpInstance(List<?> dependencies) {
		final Object op;
		try {
			// TODO: Consider whether this is really the best way to
			// instantiate the op class here. No framework usage?
			// E.g., what about pluginService.createInstance?
			Constructor<?> ctor = opClass.getDeclaredConstructor();
			ctor.setAccessible(true);
			op = ctor.newInstance();
		}
		catch (final InstantiationException | IllegalAccessException
				| NoSuchMethodException | SecurityException | IllegalArgumentException
				| InvocationTargetException e)
		{
			// TODO: Think about whether exception handling here should be
			// different.
			throw new IllegalStateException("Unable to instantiate op: '" + opClass
				.getName() + "' Ensure that the Op has a no-args constructor.", e);
		}
		final List<OpDependencyMember<?>> dependencyMembers = dependencies();
		for (int i = 0; i < dependencyMembers.size(); i++) {
			final OpDependencyMember<?> dependencyMember = dependencyMembers.get(i);
			try {
				dependencyMember.createInstance(op).set(dependencies.get(i));
			}
			catch (final Exception ex) {
				// TODO: Improve error message. Used to include exact OpRef of Op
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

	@Override
	public ValidityException getValidityException() {
		return validityException;
	}
	
	@Override
	public boolean isValid() {
		return validityException == null;
	}
	
	@Override
	public AnnotatedElement getAnnotationBearer() {
		return opClass;
	}
	
	// -- Object methods --

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof OpClassInfo))
			return false;
		final OpInfo that = (OpInfo) o;
		return struct().equals(that.struct());
	}

	@Override
	public int hashCode() {
		return struct().hashCode();
	}

	@Override
	public String toString() {
		return OpUtils.opString(this);
	}

	@Override
	public String version() {
		return version;
	}

	/**
	 * For a {@link Class}, we define the implementation as the concatenation
	 * of:
	 * <ol>
	 * <li>The fully qualified name of the class
	 * <li>The version of the class containing the field, with a preceding
	 * {@code @}
	 * </ol>
	 * <p>
	 * For example, for a field class {@code com.example.foo.Bar}, you might have
	 * <p>
	 * {@code com.example.foo.Bar@1.0.0}
	 * <p>
	 */
	@Override
	public String id() {
		return  implementationName() + "@" + version();
	}

	// -- Helper methods

	private static double priorityFromAnnotation(Class<?> annotationBearer) {
		final Plugin opAnnotation = annotationBearer.getAnnotation(Plugin.class);
		return opAnnotation == null ? Priority.NORMAL : opAnnotation.priority();
	}

	private Hints formHints(OpHints h) {
		if (h == null) return new ImmutableHints(new String[0]);
		return new ImmutableHints(h.hints());
	}

}
