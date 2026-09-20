/*
 * #%L
 * Running things that declare their inputs and outputs.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.execute;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Optional;

import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * An {@link Executable} backed by a Java class whose {@link Parameter} fields
 * declare its parameters, and which implements {@link Runnable}.
 *
 * @author Curtis Rueden
 */
public class JavaExecutable implements Executable {

	private final Class<? extends Runnable> type;
	private final Struct struct;
	private final Lookup lookup;

	public JavaExecutable(final Class<? extends Runnable> type) {
		this(type, null);
	}

	/**
	 * @param type the class to describe
	 * @param lookup a lookup with private access to it, or null to reflect with
	 *          this module's own access. A container supplies one so that the
	 *          class's package need only be opened to the container.
	 */
	public JavaExecutable(final Class<? extends Runnable> type,
		final Lookup lookup)
	{
		this.type = type;
		this.lookup = lookup;
		this.struct = Executables.struct(type, lookup);
	}

	@Override
	public String name() {
		return type.getName();
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public ExecutableInstance create() {
		final Runnable object = instantiate();
		final StructInstance<?> parameters = struct.createInstance(object);
		return new ExecutableInstance() {

			@Override
			public Executable executable() {
				return JavaExecutable.this;
			}

			@Override
			public StructInstance<?> parameters() {
				return parameters;
			}

			@Override
			public void run() {
				object.run();
			}

			@Override
			public Optional<Behavior> behavior(final String name) {
				return methodNamed(name).map(method -> args -> invoke(method, object,
					args));
			}
		};
	}

	/** Gets the class this describes. */
	public Class<? extends Runnable> type() {
		return type;
	}

	/**
	 * Resolves a named behavior on the given object.
	 * <p>
	 * NB: shared with {@link Executables#executableOf}, so that an object handed
	 * in directly supports callbacks and generators exactly as a discovered
	 * class does. Having only one of the two paths resolve behaviors made them
	 * silently do nothing, which is a poor way to find out.
	 * </p>
	 */
	static Optional<Behavior> behaviorOf(final Class<?> type, final Lookup lookup,
		final Object target, final String name)
	{
		return methodNamed(type, name).map(method -> args -> invoke(type, lookup,
			method, target, args));
	}

	/** Finds a declared method by name, in this class or a superclass. */
	private Optional<Method> methodNamed(final String name) {
		return methodNamed(type, name);
	}

	private static Optional<Method> methodNamed(final Class<?> type,
		final String name)
	{
		for (Class<?> c = type; c != null; c = c.getSuperclass()) {
			for (final Method method : c.getDeclaredMethods()) {
				// NB: by name alone. A behavior is identified by its name, and
				// overloading one would be ambiguous rather than useful.
				if (method.getName().equals(name)) return Optional.of(method);
			}
		}
		return Optional.empty();
	}

	private Object invoke(final Method method, final Object target,
		final Object[] args)
	{
		return invoke(type, lookup, method, target, args);
	}

	private static Object invoke(final Class<?> type, final Lookup lookup,
		final Method method, final Object target, final Object[] args)
	{
		try {
			final Lookup access;
			if (lookup != null) access = lookup;
			else {
				// NB: see FieldParameterMember.ownLookupIn -- privateLookupIn needs
				// this module to read the target's, which only its own code may add.
				JavaExecutable.class.getModule().addReads(type.getModule());
				access = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
			}
			final MethodHandle handle = access.unreflect(method);
			final Object[] all = new Object[args.length + 1];
			all[0] = target;
			System.arraycopy(args, 0, all, 1, args.length);
			return handle.invokeWithArguments(all);
		}
		catch (final Throwable exc) {
			throw new BehaviorException("Behavior failed: " + type.getName() + "." + //
				method.getName(), exc);
		}
	}

	private Runnable instantiate() {
		try {
			return type.getDeclaredConstructor().newInstance();
		}
		catch (final ReflectiveOperationException exc) {
			throw new IllegalStateException("Cannot construct " + type.getName() + //
				". Does it have a no-argument constructor, and does its module" + //
				" declare `opens " + type.getPackageName() + //
				" to org.scijava.execute;`?", exc);
		}
	}
}
