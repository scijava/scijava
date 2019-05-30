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

package org.scijava.ops.matcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.scijava.core.Priority;
import org.scijava.ops.MethodParameterOpDependencyMember;
import org.scijava.ops.OpDependency;
import org.scijava.ops.OpDependencyMember;
import org.scijava.ops.OpUtils;
import org.scijava.ops.core.Op;
import org.scijava.param.ParameterStructs;
import org.scijava.param.ValidityException;
import org.scijava.plugin.Plugin;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.util.Types;

/**
 * Metadata about an op implementation defined as a class.
 * 
 * @author Curtis Rueden
 * @author David Kolb
 */
public class OpClassInfo implements OpInfo {

	private final Class<? extends Op> opClass;
	private Struct struct;
	private ValidityException validityException;

	public OpClassInfo(final Class<? extends Op> opClass) {
		this.opClass = opClass;
		try {
			struct = ParameterStructs.structOf(opClass);
			OpUtils.checkHasSingleOutput(struct);
		} catch (ValidityException e) {
			validityException = e;
		} 
	}

	// -- OpInfo methods --

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
	public double priority() {
		final Plugin opAnnotation = opClass.getAnnotation(Plugin.class);
		return opAnnotation == null ? Priority.NORMAL : opAnnotation.priority();
	}

	@Override
	public String implementationName() {
		return opClass.getName();
	}

	@Override
	public StructInstance<?> createOpInstance(
		List<? extends Object> dependencies)
	{
		Object op = null;
		try {
			// TODO: Consider whether this is really the best way to
			// instantiate the op class here. No framework usage?
			// E.g., what about pluginService.createInstance?
			Constructor<? extends Op> ctor = getConstructor();
			try {
				op = ctor.newInstance(dependencies.toArray());
			}
			catch (Exception ex) {
				// TODO: This is just needed as long as not all Ops are ported to
				// Op-constructor (also see the next lines).
			}
			if (op == null) op = opClass.getDeclaredConstructor().newInstance();
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
				// TODO: "if" can be removed once all Ops use Op-constructor
				if (!(dependencyMember instanceof MethodParameterOpDependencyMember))
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

	// TODO: We should store the (annotated) constructor to be used somewhere.
	// Should be part of parsing. Maybe have a ConstructorMember that is a
	// collection of OpDependency members or something like this.
	private Constructor<? extends Op> getConstructor()
		throws NoSuchMethodException, SecurityException
	{
		Optional<Constructor<?>> ctorOptional = Arrays.stream(opClass
			.getDeclaredConstructors()).findAny().filter(c -> c.getAnnotationsByType(
				OpDependency.class).length != 0);
		final Constructor<? extends Op> ctor = ctorOptional.isPresent()
			? (Constructor<? extends Op>) ctorOptional.get() : opClass
				.getDeclaredConstructor();
		ctor.setAccessible(true);
		return ctor;
	}

	@Override
	public ValidityException getValidityException() {
		return validityException;
	}

	@Override
	public boolean isValid() {
		return validityException == null;
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
}
