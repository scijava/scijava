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

package org.scijava.ops.engine.matcher.impl;

import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.exceptions.impl.InstanceOpMethodException;
import org.scijava.ops.engine.exceptions.impl.PrivateOpException;
import org.scijava.ops.engine.exceptions.impl.UnreadableOpException;
import org.scijava.ops.engine.struct.MethodOpDependencyMemberParser;
import org.scijava.ops.engine.struct.MethodParameterMemberParser;
import org.scijava.ops.engine.util.Infos;
import org.scijava.ops.engine.util.internal.OpMethodUtils;
import org.scijava.ops.spi.OpMethod;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.Structs;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * @author Marcel Wiedenmann
 */
public class DefaultOpMethodInfo implements OpInfo {

	private final Method method;
	private final String description;
	private final String version;
	private final List<String> names;
	private final Type opType;
	private final Struct struct;
	private final double priority;

	private final Hints hints;

	public DefaultOpMethodInfo( //
		final Method method, //
		final Class<?> opType, //
		final String version, //
		final String description, //
		final Hints hints, //
		final double priority, //
		final String... names //
	) {
		this.method = method;
		this.version = version;
		this.description = description;
		this.names = Arrays.asList(names);
		this.hints = hints;
		this.priority = priority;

		checkModifiers();

		this.opType = OpMethodUtils.getOpMethodType(opType, method);
		this.struct = Structs.from( //
			method, //
			opType, //
			new MethodParameterMemberParser(), //
			new MethodOpDependencyMemberParser() //
		);

		Infos.validate(this);
	}

	private void checkModifiers() {
		// Reject all non public methods
		if (!Modifier.isPublic(method.getModifiers())) {
			throw new PrivateOpException(method);
		}
		if (!Modifier.isStatic(method.getModifiers())) {
			// TODO: We can't properly infer the generic types of static methods at
			// the moment. This might be a Java limitation.
			throw new InstanceOpMethodException(method);
		}

		// If the Op is not in the SciJava Ops Engine module, check visibility
		Module methodModule = method.getDeclaringClass().getModule();
		if (methodModule != this.getClass().getModule()) {
			String packageName = method.getDeclaringClass().getPackageName();
			if (!methodModule.isOpen(packageName, methodModule)) {
				throw new UnreadableOpException(packageName);
			}
		}
	}

	// -- OpInfo methods --

	@Override
	public List<String> names() {
		return names;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public Type opType() {
		return opType;
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
		// Get generic string without modifiers and return type
		String fullyQualifiedMethod = method.toGenericString();
		String packageName = method.getDeclaringClass().getPackageName();
		int classNameIndex = fullyQualifiedMethod.indexOf(packageName);
		return fullyQualifiedMethod.substring(classNameIndex);
	}

	@Override
	public StructInstance<?> createOpInstance(final List<?> dependencies) {
		return OpMethodUtils.createOpInstance(this, method, dependencies);
	}

	@Override
	public String version() {
		return version;
	}

	/**
	 * For an {@link OpMethod}, we define the implementation as the concatenation
	 * of:
	 * <ol>
	 * <li>The fully qualified name of the class containing the method</li>
	 * <li>The method name</li>
	 * <li>The method parameters</li>
	 * <li>The version of the class containing the method, with a preceding
	 * {@code @}</li>
	 * </ol>
	 * <p>
	 * For example, for a method {@code baz(Double in1, String in2)} in class
	 * {@code com.example.foo.Bar}, you might have
	 * <p>
	 * {@code com.example.foo.Bar.baz(Double in1,String in2)@1.0.0}
	 */
	@Override
	public String id() {
		return OpInfo.IMPL_DECLARATION + implementationName() + "@" + version();
	}

	// -- Object methods --

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof DefaultOpMethodInfo)) return false;
		final OpInfo that = (OpInfo) o;
		return struct().equals(that.struct());
	}

	@Override
	public int hashCode() {
		return struct().hashCode();
	}

	@Override
	public String toString() {
		return Infos.describe(this);
	}

	@Override
	public AnnotatedElement getAnnotationBearer() {
		return method;
	}

}
