/*
 * #%L
 * The public API of SciJava Ops.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
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

package org.scijava.ops.api;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.SortedSet;

import org.scijava.discovery.Discoverer;
import org.scijava.priority.Prioritized;
import org.scijava.priority.Priority;
import org.scijava.types.Nil;

/**
 * An op environment is the top-level entry point into op execution. It provides
 * all the built-in functionality of ops in a single place, including:
 * <ul>
 * <li>The pool of available ops, from which candidates are chosen.</li>
 * <li>Type-safe, built-in method signatures for all op implementations.</li>
 * <li>Selection (a.k.a. "matching") of op implementations from
 * {@link OpRequest} descriptors.</li>
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
public interface OpEnvironment extends Prioritized<OpEnvironment> {

	/**
	 * Generates an <b>empty</b> {@link OpEnvironment}, which can be populated
	 * with the Ops of the caller's choice.
	 *
	 * @return an empty {@link OpEnvironment}
	 * @see #build() for an {@link OpEnvironment} that is fully populated
	 */
	static OpEnvironment buildEmpty() {
		Optional<OpEnvironment> opsOptional = Discoverer //
			.using(ServiceLoader::load) //
			.discoverMax(OpEnvironment.class);
		return opsOptional.orElseThrow( //
			() -> new RuntimeException("No OpEnvironment Provided!") //
		);
	}

	/**
	 * Generates an {@link OpEnvironment} with all available Ops.
	 *
	 * @return an {@link OpEnvironment} with all available Ops.
	 * @see #buildEmpty() for an {@link OpEnvironment} that is empty
	 */
	static OpEnvironment build() {
		OpEnvironment ops = buildEmpty();
		ops.discoverEverything();
		return ops;
	}

	/**
	 * Obtains all Ops in the {@link OpEnvironment}, sorted by priority.
	 *
	 * @return a {@link SortedSet} containing all Ops contained in the
	 *         {@link OpEnvironment}.
	 */
	default SortedSet<OpInfo> infos() {
		return infos(null, getDefaultHints());
	}

	/**
	 * Obtains all Ops in the {@link OpEnvironment} that are named {@code name},
	 * sorted by priority.
	 *
	 * @param name the {@link String} of all Ops to be returned.
	 * @return a {@link SortedSet} containing all Ops in the {@link OpEnvironment}
	 *         named {@code name}
	 */
	SortedSet<OpInfo> infos(String name);

	/**
	 * Obtains all Ops in the {@link OpEnvironment} that match {@code hints},
	 * sorted by priority
	 *
	 * @param hints the {@link Hints} used to filter available Ops.
	 * @return a {@link SortedSet} containing all Ops in the {@link OpEnvironment}
	 *         matching {@code hints}
	 */
	SortedSet<OpInfo> infos(Hints hints);

	/**
	 * Obtains all Ops in the {@link OpEnvironment} that are named {@code name}
	 * and match {@code hints}, sorted by priority
	 *
	 * @param name the {@link String} of all Ops to be returned.
	 * @param hints the {@link Hints} used to filter available Ops.
	 * @return a {@link SortedSet} containing all Ops in the {@link OpEnvironment}
	 *         named {@code name} and matching {@code hints}
	 */
	SortedSet<OpInfo> infos(String name, Hints hints);

	void discoverUsing(Discoverer... d);

	void discoverEverything();

	OpHistory history();

	// TODO: Add interface method: OpInfo info(final String opName, final Nil<T>
	// specialType, final Nil<?>[] inTypes, final Nil<?> outType);

	/**
	 * Returns an Op fitting the provided arguments. NB implementations of this
	 * method likely depend on the {@link Hints} set by
	 * {@link OpEnvironment#setDefaultHints(Hints)}, which provides no guarantee
	 * of thread-safety. Users interested in parallel Op matching should consider
	 * using {@link OpEnvironment#op(String, Nil, Nil[], Nil, Hints)} instead.
	 *
	 * @param <T> the {@link Type} of the Op
	 * @param opName the name of the Op
	 * @param specialType the generic {@link Type} of the Op
	 * @param inTypes the arguments (inputs) to the Op
	 * @param outType the return of the Op (note that it may also be an argument)
	 * @return an instance of an Op aligning with the search parameters
	 */
	default <T> T op( //
		final String opName, //
		final Nil<T> specialType, //
		final Nil<?>[] inTypes, //
		final Nil<?> outType //
	) {
		return op(opName, specialType, inTypes, outType, getDefaultHints());
	}

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

	default <T> T opFromInfoChain(InfoTree tree, Nil<T> specialType) {
		return opFromInfoChain(tree, specialType, getDefaultHints());
	}

	<T> T opFromInfoChain(InfoTree tree, Nil<T> specialType, Hints hints);

	/**
	 * Returns an Op fitting the provided arguments.
	 *
	 * @param <T> the {@link Type} of the Op
	 * @param signature the signature of the Op
	 * @param specialType the generic {@link Type} of the Op
	 * @return an instance of an Op aligning with the search parameters
	 */
	default <T> T opFromSignature(final String signature,
		final Nil<T> specialType)
	{
		InfoTree info = treeFromID(signature);
		return opFromInfoChain(info, specialType);
	}

	InfoTree treeFromID(final String signature);

	default InfoTree treeFromInfo(final OpInfo info, final Nil<?> specialType) {
		return treeFromInfo(info, specialType, getDefaultHints());
	}

	InfoTree treeFromInfo(final OpInfo info, final Nil<?> specialType,
		final Hints hints);

	/**
	 * <p>
	 * Entry point for convenient Op calls, providing a builder-style interface to
	 * walk through the process step-by-step.
	 * </p>
	 * <p>
	 * The general order of specification:
	 * </p>
	 * <ol>
	 * <li>The op name (and {@link Hints}, if desired)</li>
	 * <li>The number of input(s)</li>
	 * <li>The type or value(s) of input(s)</li>
	 * <li>One of:
	 * <ul>
	 * <li>The type or value of the output</li>
	 * <li>Which input should be modified in-place</li>
	 * </ul>
	 * </li>
	 * </ol>
	 * The first two steps are required, at a minimum. The choices you make will
	 * determine the <i>type</i> of Op that is matched:
	 * <ul>
	 * <li>No inputs &rarr; <code>Producer</code> or <code>Computer</code></li>
	 * <li>Inputs with an output <i>value</i> &rarr; <code>Computer</code></li>
	 * <li>Inputs with an output <i>type</i> &rarr; <code>Computer</code> or
	 * <code>Function</code></li>
	 * <li>Inputs with no output &rarr; <code>Inplace</code> or
	 * <code>Function</code> with unknown (<code>Object</code>) return</li>
	 * </ul>
	 * <p>
	 * Examples: {@code OpEnvironment env = new DefaultOpEnvironment();}
	 * </p>
	 * <ul>
	 * <li>{@code env.op("create").outType(DoubleType.class).create();} &#8212;
	 * run an Op creating an instance of the ImgLib2 {@code DoubleType}</li>
	 * <li>{@code env.op("create").outType(DoubleType.class).create();} &#8212;
	 * same as above.</li>
	 * <li>{@code env.op("create", hints).outType(DoubleType.class).create();}
	 * &#8212; same as above but matching with the provided {@link Hints}.</li>
	 * <li>{@code env.op("create").outType(Img.class).create();} &#8212; run an Op
	 * creating a raw instance of an ImgLib2 {@code Img}.</li>
	 * <li>{@code env.op("create").outType(new
	 * Nil<Img<DoubleType>>(){}).create();} &#8212; run an Op creating an instance
	 * of an ImgLib2 {@code Img<DoubleType>}.</li>
	 * <li>{@code env.op("math.add").inType(Integer.class, Integer.class).function();}
	 * &#8212; get an instance of an Op to add two integers together. Return type
	 * will be {@code Object}.</li>
	 * <li>{@code env.op("math.add").inType(Integer.class, Integer.class).function();}
	 * &#8212; same as above.</li>
	 * <li>{@code env.op("math.add").input(1, 1).outType(Double.class).apply();}
	 * &#8212; run an Op combining two integers. Return type will be
	 * {@code Double}.</li>
	 * <li>{@code env.op("math.add").input(img1, img2).output(result).compute();}
	 * &#8212; run an Op combining two images and storing the result in a
	 * pre-allocated image.</li>
	 * <li>{@code env.op("filter.addPoissonNoise").input(img1).mutate();} &#8212;
	 * run an Op adding poisson noise to an input image.</li>
	 * </ul>
	 *
	 * @param opName The name of the Op to run
	 * @return The {@link OpBuilder} instance for builder chaining.
	 * @throws OpMatchingException if the Op request cannot be satisfied
	 * @see #op(String, Hints) To specify a Hints instance to use
	 * @see OpBuilder
	 */
	default OpBuilder op(final String opName) {
		return new OpBuilder(this, opName);
	}

	/**
	 * As {@link #op(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return The {@link OpBuilder} instance for builder chaining.
	 * @throws OpMatchingException if the Op request cannot be satisfied
	 * @see #op(String) To use the default Hints for this OpEnvironment
	 */
	default OpBuilder op(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints);
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
	 * <pre>{@code
	 * op("engine.adapt")
	 *  input(computer)
	 *   .outType(new Nil<Computers.Arity1<Iterable<Double>, Iterable<Double>>>() {})
	 *   .apply()
	 * }</pre>
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
	 * @param opClass the {@link Class} to derive the Op from
	 * @param names - the name(s) of the Op
	 * @return an {@link OpInfo} which can make instances of {@code opClass}
	 */
	default OpInfo opify(Class<?> opClass, String... names) {
		return opify(opClass, Priority.NORMAL, names);
	}

	/**
	 * Creates an {@link OpInfo} from an {@link Class} with the given priority.
	 *
	 * @param opClass the {@link Class} to derive the Op from
	 * @param priority - the assigned priority of the Op.
	 * @param names - the name(s) of the Op
	 * @return an {@link OpInfo} which can make instances of {@code opClass}
	 */
	OpInfo opify(Class<?> opClass, double priority, String... names);

	/**
	 * Registers all {@link OpInfo} derived from {@link Object}s within the passed
	 * array. Elements within this {@link Object[]} might be:
	 * <ul>
	 * <li>{@link OpInfo}s</li>
	 * <li>{@link java.util.Collection}s or arrays of the above</li>
	 * <li>{@link Object}s (such as, but not limited to, {@link Class}es,
	 * {@link Method}s, etc.) from which {@link OpInfo}s could be derived
	 * </ul>
	 *
	 * @param objects the {@link Object}s that should be made discoverable to this
	 *          {@link OpEnvironment}
	 */
	void register(Object... objects);

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
	 * a specific {@link Hints} should <b>not</b> use this method, instead opting
	 * for {@link OpEnvironment#op(String, Nil, Nil[], Nil, Hints)}.
	 *
	 * @param hints the {@link Hints} to be set as default
	 */
	void setDefaultHints(Hints hints);

	/**
	 * Returns <b>a copy</b> of the default {@link Hints} object.
	 *
	 * @return the default {@link Hints}
	 */
	Hints getDefaultHints();

	/**
	 * Returns the descriptions for all Ops contained within this
	 * {@link OpEnvironment}
	 *
	 * @return a String describing all Ops in the environment matching
	 *         {@code name}
	 */
	default String help() {
		return help(new PartialOpRequest());
	}

	/**
	 * Returns the descriptions for all Ops contained within this
	 * {@link OpEnvironment} matching {@code name}
	 *
	 * @param name the {@link String} name to filter on
	 * @return a {@link String} describing all Ops in the environment matching
	 *         {@code name}
	 */
	default String help(final String name) {
		return help(new PartialOpRequest(name));
	}

	/**
	 * Returns simple descriptions for all Ops identifiable by a given name within
	 * this {@link OpEnvironment}
	 *
	 * @param request the {@link OpRequest} to filter on
	 * @return a {@link String} containing a (set of) lines for each Op matching
	 *         {@code request}
	 */
	String help(final OpRequest request);

	/**
	 * Returns verbose descriptions for all Ops contained within this
	 * {@link OpEnvironment}
	 *
	 * @return a {@link String} containing a (set of) lines for each Op
	 */
	default String helpVerbose() {
		return helpVerbose(new PartialOpRequest());
	}

	/**
	 * Returns verbose descriptions for all Ops contained within this
	 * {@link OpEnvironment}
	 *
	 * @param name the {@link String} name to filter on
	 * @return a {@link String} describing all Ops in the environment matching
	 *         {@code name}
	 */
	default String helpVerbose(final String name) {
		return helpVerbose(new PartialOpRequest(name));
	}

	/**
	 * Returns verbose descriptions for all Ops contained within this
	 * {@link OpEnvironment}
	 *
	 * @param request the {@link OpRequest} to filter on
	 * @return a {@link String} containing a (set of) lines for each Op matching
	 *         {@code request}
	 */
	String helpVerbose(final OpRequest request);
}
