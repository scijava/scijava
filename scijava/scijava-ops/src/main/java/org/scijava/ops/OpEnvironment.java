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

import java.lang.reflect.Type;

import org.scijava.ops.hints.Hints;
import org.scijava.ops.matcher.OpRef;
import org.scijava.types.Nil;

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
 * 
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public interface OpEnvironment {

	/** The available ops for this environment. */
	Iterable<OpInfo> infos();
	
	Iterable<OpInfo> infos(String name);

	Iterable<OpInfo> infos(Hints hints);

	Iterable<OpInfo> infos(String name, Hints hints);

	// TODO: Add interface method: OpInfo info(final String opName, final Nil<T> specialType, final Nil<?>[] inTypes, final Nil<?> outType);

	/**
	 * Returns an Op fitting the provided arguments. NB implementations of this
	 * method likely depend on the {@link Hints} set by
	 * {@link OpEnvironment#setHints(Hints)}, which provides no guarantee of
	 * thread-safety. Users interested in parallel Op matching should consider
	 * using {@link OpEnvironment#op(String, Nil, Nil[], Nil, Hints)} instead.
	 *
	 * @param <T> the {@link Type} of the Op
	 * @param opName the name of the Op
	 * @param specialType the generic {@link Type} of the Op
	 * @param inTypes the arguments (inputs) to the Op
	 * @param outType the return of the Op (note that it may also be an argument)
	 * @return an instance of an Op aligning with the search parameters
	 */
	<T> T op(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType);

	/**
	 * Returns an Op fitting the provided arguments. NB implementations of this
	 * method likely depend on the {@link Hints} set by
	 * {@link OpEnvironment#setHints(Hints)}, which provides no guarantee of
	 * thread-safety. Users interested in parallel Op matching should consider
	 * using {@link OpEnvironment#op(String, Nil, Nil[], Nil, Hints)} instead.
	 *
	 * @param <T> the {@link Type} of the Op
	 * @param info the {@link OpInfo}
	 * @param specialType the generic {@link Type} of the Op
	 * @param inTypes the arguments (inputs) to the Op
	 * @param outType the return of the Op (note that it may also be an argument)
	 * @return an instance of an Op aligning with the search parameters
	 */
	<T> T op(OpInfo info, Nil<T> specialType, Nil<?>[] inTypes, Nil<?> outType);

	/**
	 * Returns an Op fitting the provided arguments.
	 *
	 * @param <T> the {@link Type} of the Op
	 * @param opName the name of the Op
	 * @param specialType the generic {@link Type} of the Op
	 * @param inTypes the arguments (inputs) to the Op
	 * @param outType the return of the Op (note that it may also be an argument)
	 * @param hints the {@link Hints} that should guide this matching call
	 * @return an instance of an Op aligning with the search parameters
	 */
	<T> T op(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType, Hints hints);

	/**
	 * Returns an Op fitting the provided arguments.
	 *
	 * @param <T> the {@link Type} of the Op
	 * @param info the {@link OpInfo}
	 * @param specialType the generic {@link Type} of the Op
	 * @param inTypes the arguments (inputs) to the Op
	 * @param outType the return of the Op (note that it may also be an argument)
	 * @param hints the {@link Hints} that should guide this matching call
	 * @return an instance of an Op aligning with the search parameters
	 */
	<T> T op(OpInfo info, Nil<T> specialType, Nil<?>[] inTypes, Nil<?> outType,
		Hints hints);

	default OpBuilder op(final String opName) {
		return new OpBuilder(this, opName);
	}

	/** Discerns the generic type of an object instance. */
	Type genericType(Object obj);

	/**
	 * Used to enrich a lambda expression with its generic type. Its usage is
	 * necessary in order to find Ops that could take this lamdba expression as an
	 * argument.
	 * <p>
	 * Suppose, for example, that a user has written a lambda
	 * {@code Computers.Arity1<Double, Double> computer}, but a user wishes to
	 * have a {@code Computers.Arity1<Iterable<Double>, Iterable<Double>>}. They
	 * know of an Op in the {@code OpEnvironment} that is able to adapt
	 * {@code computer} so that it is run in a parallel fashion. They cannot
	 * simply call
	 * <p>
	 *
	 * <pre>
	 * <code>
	 * op("adapt")
	 *   .input(computer)
	 *   .outType(new Nil&lt;Computers.Arity1&lt;Iterable&lt;Double&gt;, Iterable&lt;Double&gt;&gt;&gt;() {})
	 *   .apply()
	 * </code>
	 * </pre>
	 *
	 * since the type parameters of {@code computer} are not retained at runtime.
	 * <p>
	 * {@code bakeLambdaType} should be used as a method of retaining that fully
	 * reified lambda type so that the lambda can be used
	 * <p>
	 * Note: {@code bakeLambdaType} <b>does not</b> need to be used with anonymous
	 * subclasses; these retain their type parameters at runtime. It is only
	 * lambda expressions that need to be passed to this method.
	 * 
	 * @param <T> The type of the op instance to enrich.
	 * @param op The op instance to enrich.
	 * @param type The intended generic type of the object to be known at runtime.
	 * @return An enriched version of the object with full knowledge of its
	 *         generic type.
	 */
	<T> T bakeLambdaType(T op, Type type);

	/**
	 * Creates an {@link OpInfo} from an {@link Class}.
	 *
	 * @param opClass
	 * @return an {@link OpInfo} which can make instances of {@code opClass}
	 */
	OpInfo opify(Class<?> opClass);

	/**
	 * Creates an {@link OpInfo} from an {@link Class} with the given priority.
	 *
	 * @param opClass
	 * @param priority - the assigned priority of the Op.
	 * @return an {@link OpInfo} which can make instances of {@code opClass}
	 */
	OpInfo opify(Class<?> opClass, double priority);

	/**
	 * Makes the {@link OpInfo} {@code info} known to this {@link OpEnvironment} under the name {@code name}
	 * @param info
	 * @param name
	 */
	void register(OpInfo info, String name);

	/**
	 * Sets the {@link Hints} for the {@link OpEnvironment}. Every Call to
	 * {@link #op} that <b>does not</b> pass a {@link Hints} will <b>copy</b> the
	 * Hints passed here (to prevent multiple Op calls from accessing/changing the
	 * same {@link Hints}). In the case that <b>no {@link Hints} have been
	 * given</b> to the Environment before {@code op} is called, the
	 * implementation will create a suitable stand-in.
	 * <p>
	 * Note that this method does not inherently provide <b>any</b> guarantees
	 * pertaining to thread-safety; this API is provided purely for convenience.
	 * Any calls to {@link OpEnvironment#op(String, Nil, Nil[], Nil)} that require
	 * a specific {@Hints} should <b>not</b> use this method, instead opting for
	 * {@link OpEnvironment#op(String, Nil, Nil[], Nil, Hints)}.
	 * 
	 * @param hints
	 */
	void setHints(Hints hints);
}
