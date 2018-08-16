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

package org.scijava.ops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.scijava.Contextual;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;

/**
 * An op environment is the top-level entry point into op execution. It provides
 * all the built-in functionality of ops in a single place, including:
 * <ul>
 * <li>The pool of available ops, from which candidates are chosen.</li>
 * <li>Type-safe, built-in method signatures for all op implementations.</li>
 * <li>Selection (a.k.a. "matching") of op implementations from {@link OpRef}
 * descriptors.</li>
 * </ul>
 * <p>
 * Customizing the {@link OpEnvironment} allows customization of any or all of
 * the above. Potential use cases include:
 * <ul>
 * <li>Limiting or extending the pool of available op implementations.</li>
 * <li>Caching op outputs to improve subsequent time performance.</li>
 * <li>Configuration of environment "hints" to improve performance in time or
 * space.</li>
 * </ul>
 * <p>
 * The default&mdash;but not necessarily <em>only</em>&mdash;op environment is
 * the {@link OpService} of the application. The environment can be modified by
 * using a {@link CustomOpEnvironment}, or by implementing this interface
 * directly.
 * </p>
 * 
 * @author Curtis Rueden
 * @see OpService
 * @see CustomOpEnvironment
 */
public interface OpEnvironment extends Contextual {

	// -- OpEnvironment methods --

	OpMatchingService matcher();

	/**
	 * Executes the given operation with the specified arguments. The best
	 * {@link Op} implementation to use will be selected automatically from the
	 * operation name and arguments.
	 *
	 * @param name The operation to execute. If multiple {@link Op}s share this
	 *          name, then the best {@link Op} implementation to use will be
	 *          selected automatically from the name and arguments. If a name
	 *          without namespace is given, then ops from all namespaces with that
	 *          name will be included in the match; e.g., {@code "and"} will match
	 *          both {@code "logic.and"} and {@code "math.and"} ops.
	 * @param args The operation's arguments.
	 * @return The result of the execution. If the {@link Op} has no outputs, this
	 *         will return {@code null}. If exactly one output, it will be
	 *         returned verbatim. If more than one, a {@code List<Object>} of the
	 *         outputs will be given.
	 */
	default Object run(final String name, final Object... args) {
		return run(module(name, args));
	}

	/**
	 * Executes the operation of the given type with the specified arguments. The
	 * best {@link Op} implementation to use will be selected automatically from
	 * the operation type and arguments.
	 *
	 * @param type The {@link Class} of the operation to execute. If multiple
	 *          {@link Op}s share this type (e.g., the type is an interface which
	 *          multiple {@link Op}s implement), then the best {@link Op}
	 *          implementation to use will be selected automatically from the type
	 *          and arguments.
	 * @param args The operation's arguments.
	 * @return The result of the execution. If the {@link Op} has no outputs, this
	 *         will return {@code null}. If exactly one output, it will be
	 *         returned verbatim. If more than one, a {@code List<Object>} of the
	 *         outputs will be given.
	 */
	default Object run(final Class<? extends Op> type, final Object... args) {
		return run(module(type, args));
	}

	/**
	 * Executes the given {@link Op} with the specified arguments.
	 *
	 * @param op The {@link Op} to execute.
	 * @param args The operation's arguments.
	 * @return The result of the execution. If the {@link Op} has no outputs, this
	 *         will return {@code null}. If exactly one output, it will be
	 *         returned verbatim. If more than one, a {@code List<Object>} of the
	 *         outputs will be given.
	 */
	default Object run(final Op op, final Object... args) {
		return run(module(op, args));
	}

	/**
	 * Gets the best {@link Op} to use for the given operation and arguments,
	 * populating its inputs.
	 *
	 * @param name The name of the operation. If multiple {@link Op}s share this
	 *          name, then the best {@link Op} implementation to use will be
	 *          selected automatically from the name and arguments.
	 * @param args The operation's arguments.
	 * @return An {@link Op} with populated inputs, ready to run.
	 */
	default Op op(final String name, final Object... args) {
		return OpUtils.unwrap(module(name, args), OpRef.types(Op.class));
	}

	/**
	 * Gets the best {@link Op} to use for the given operation type and arguments,
	 * populating its inputs.
	 *
	 * @param type The {@link Class} of the operation. If multiple {@link Op}s
	 *          share this type (e.g., the type is an interface which multiple
	 *          {@link Op}s implement), then the best {@link Op} implementation to
	 *          use will be selected automatically from the type and arguments.
	 * @param args The operation's arguments.
	 * @return An {@link Op} with populated inputs, ready to run.
	 */
	default <OP extends Op> OP op(final Class<OP> type, final Object... args) {
		return (OP) OpUtils.unwrap(module(type, args), OpRef.types(type));
	}

	/**
	 * Looks up an op whose constraints are specified by the given {@link OpRef}
	 * descriptor.
	 * <p>
	 * NB: While it is typically the case that the returned {@link Op} instance is
	 * of the requested type(s), it may differ in certain circumstances. For
	 * example, the {@link CachedOpEnvironment} wraps the matching {@link Op}
	 * instance in some cases so that the values it computes can be cached for
	 * performance reasons.
	 * </p>
	 * 
	 * @param ref The {@link OpRef} describing the op to match.
	 * @return The matched op.
	 */
	default Op op(final OpRef ref) {
		return op(Collections.singletonList(ref));
	}

	/**
	 * Looks up an op whose constraints are specified by the given list of
	 * {@link OpRef} descriptor.
	 * <p>
	 * NB: While it is typically the case that the returned {@link Op} instance is
	 * of the requested type(s), it may differ in certain circumstances. For
	 * example, the {@link CachedOpEnvironment} wraps the matching {@link Op}
	 * instance in some cases so that the values it computes can be cached for
	 * performance reasons.
	 * </p>
	 * 
	 * @param refs The list of {@link OpRef}s describing the op to match.
	 * @return The matched op.
	 */
	default Op op(final List<OpRef> refs) {
		final OpCandidate match = matcher().findMatch(this, refs);
		return OpUtils.unwrap(match.getModule(), match.getRef());
	}

	/**
	 * Gets the best {@link Op} to use for the given operation and arguments,
	 * wrapping it as a {@link Module} with populated inputs.
	 *
	 * @param name The name of the operation.
	 * @param args The operation's arguments.
	 * @return A {@link Module} wrapping the best {@link Op}, with populated
	 *         inputs, ready to run.
	 */
	default Module module(final String name, final Object... args) {
		return matcher().findMatch(this, OpRef.create(name, args)).getModule();
	}

	/**
	 * Gets the best {@link Op} to use for the given operation type and arguments,
	 * wrapping it as a {@link Module} with populated inputs.
	 *
	 * @param type The required type of the operation. If multiple {@link Op}s
	 *          share this type (e.g., the type is an interface which multiple
	 *          {@link Op}s implement), then the best {@link Op} implementation to
	 *          use will be selected automatically from the type and arguments.
	 * @param args The operation's arguments.
	 * @return A {@link Module} wrapping the best {@link Op}, with populated
	 *         inputs, ready to run.
	 */
	default Module module(final Class<? extends Op> type, final Object... args) {
		return matcher().findMatch(this, OpRef.create(type, args)).getModule();
	}

	/**
	 * Wraps the given {@link Op} as a {@link Module}, populating its inputs.
	 *
	 * @param op The {@link Op} to wrap and populate.
	 * @param args The operation's arguments.
	 * @return A {@link Module} wrapping the {@link Op}, with populated inputs,
	 *         ready to run.
	 */
	default Module module(final Op op, final Object... args) {
		final Module module = info(op).struct().createModule(op);
		return matcher().assignInputs(module, args);
	}

	/** Gets the metadata for a given {@link Op} class. */
	OpInfo info(Class<? extends Op> type);

	/** Gets the metadata for a given {@link Op}. */
	default OpInfo info(final Op op) {
		return info(op.getClass());
	}

	/**
	 * The available ops for the context, <em>including</em> those of the parent.
	 *
	 * @see #parent()
	 */
	Collection<OpInfo> infos();

	/** Gets the fully qualified names of all available operations. */
	default Collection<String> ops() {
		// collect list of unique operation names
		final HashSet<String> operations = new HashSet<>();
		for (final OpInfo info : infos()) {
			if (info.isNamed()) operations.add(info.getName());
		}

		// convert the set into a sorted list
		final ArrayList<String> sorted = new ArrayList<>(operations);
		Collections.sort(sorted);
		return sorted;
	}

	/** The parent context, if any. */
	default OpEnvironment parent() {
		return null;
	}

	/** Gets the namespace of the given class. */
	<NS extends Namespace> NS namespace(final Class<NS> nsClass);

	// -- Helper methods --

	static Object run(final Module module) {
		module.run();

		final List<Object> outputs = new ArrayList<>();
		for (final ModuleItem<?> output : module.getInfo().outputs()) {
			final Object value = output.getValue(module);
			outputs.add(value);
		}
		return outputs.size() == 1 ? outputs.get(0) : outputs;
	}
}
