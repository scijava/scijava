/*
 * #%L
 * SciJava Operations API: Outward-facing Interfaces used by the SciJava Operations framework.
 * %%
 * Copyright (C) 2021 - 2023 SciJava developers.
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

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.SortedSet;

import org.scijava.discovery.Discoverer;
import org.scijava.priority.Prioritized;
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

	static OpEnvironment buildEmpty() {
		Optional<OpEnvironment> opsOptional = Discoverer //
			.using(ServiceLoader::load) //
			.discoverMax(OpEnvironment.class);
		return opsOptional.orElseThrow( //
			() -> new RuntimeException("No OpEnvironment Provided!") //
		);
	}

	static OpEnvironment build() {
		OpEnvironment ops = buildEmpty();
		ops.discoverEverything();
		return ops;
	}

	/**
	 * Obtains all Ops in the {@link OpEnvironment}.
	 *
	 * @return a {@link SortedSet} containing all Ops contained in the
	 *         {@link OpEnvironment}.
	 */
	SortedSet<OpInfo> infos();

	/**
	 * Obtains all Ops in the {@link OpEnvironment} that are named {@code name}.
	 *
	 * @param name the {@link String} of all Ops to be returned.
	 * @return a {@link SortedSet} containing all Ops in the {@link OpEnvironment}
	 *         named {@code name}
	 */
	SortedSet<OpInfo> infos(String name);

	/**
	 * Obtains all Ops in the {@link OpEnvironment} that match {@code hints}
	 *
	 * @param hints the {@link Hints} used to filter available Ops.
	 * @return a {@link SortedSet} containing all Ops in the {@link OpEnvironment}
	 *         matching {@code hints}
	 */
	SortedSet<OpInfo> infos(Hints hints);

	/**
	 * Obtains all Ops in the {@link OpEnvironment} that are named {@code name}
	 * and match {@code hints}
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
	<T> T op(final String opName, final Nil<T> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType);

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
	 * Returns an {@link InfoTree} fitting the provided arguments. NB
	 * implementations of this method likely depend on the {@link Hints} set by
	 * {@link OpEnvironment#setDefaultHints(Hints)}, which provides no guarantee
	 * of thread-safety. Users interested in parallel Op matching should consider
	 * using {@link OpEnvironment#op(String, Nil, Nil[], Nil, Hints)} instead.
	 *
	 * @param opName the name of the Op
	 * @param specialType the generic {@link Type} of the Op
	 * @param inTypes the arguments (inputs) to the Op
	 * @param outType the return of the Op (note that it may also be an argument)
	 * @return an instance of an Op aligning with the search parameters
	 */
	InfoTree infoTree(final String opName, final Nil<?> specialType,
		final Nil<?>[] inTypes, final Nil<?> outType);

	/**
	 * Returns an {@link InfoTree} fitting the provided arguments.
	 *
	 * @param opName the name of the Op
	 * @param specialType the generic {@link Type} of the Op
	 * @param inTypes the arguments (inputs) to the Op
	 * @param outType the return of the Op (note that it may also be an argument)
	 * @param hints the {@link Hints} that should guide this matching call
	 * @return an instance of an Op aligning with the search parameters
	 */
	InfoTree infoTree(final String opName, final Nil<?> specialType,
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
	<T> T opFromSignature(final String signature, final Nil<T> specialType);

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
	 * <br/>
	 * The general order of specification:
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
	 * This class also contains a series of convenience methods that combine the
	 * {@code op} and {@code arityN} builder steps into one, e.g.
	 * {@link #nullary}, {@link #unary}, {@link #binary}, etc...
	 * </p>
	 * <br/>
	 * <p>
	 * Examples: {@code OpEnvironment env = new DefaultOpEnvironment();}
	 * <ul>
	 * <li>{@code env.op("create").arity0().outType(DoubleType.class).create();}
	 * &#8212; run an Op creating an instance of the ImgLib2
	 * {@code DoubleType}</li>
	 * <li>{@code env.nullary("create").outType(DoubleType.class).create();}
	 * &#8212; same as above.</li>
	 * <li>{@code env.nullary("create", hints).outType(DoubleType.class).create();}
	 * &#8212; same as above but matching with the provided {@link Hints}.</li>
	 * <li>{@code env.op("create").arity0().outType(Img.class).create();} &#8212;
	 * run an Op creating a raw instance of an ImgLib2 {@code Img}.</li>
	 * <li>{@code env.op("create").arity0().outType(new
	 * Nil<Img<DoubleType>>(){}).create();} &#8212; run an Op creating an instance
	 * of an ImgLib2 {@code Img<DoubleType>}.</li>
	 * <li>{@code env.op("math.add").arity2().inType(Integer.class, Integer.class).function();}
	 * &#8212; get an instance of an Op to add two integers together. Return type
	 * will be {@code Object}.</li>
	 * <li>{@code env.binary("math.add").inType(Integer.class, Integer.class).function();}
	 * &#8212; same as above.</li>
	 * <li>{@code env.op("math.add").arity2().input(1, 1).outType(Double.class).apply();}
	 * &#8212; run an Op combining two integers. Return type will be
	 * {@code Double}.</li>
	 * <li>{@code env.op("math.add").arity2().input(img1, img2).output(result).compute();}
	 * &#8212; run an Op combining two images and storing the result in a
	 * pre-allocated image.</li>
	 * <li>{@code env.op("filter.addPoissonNoise").arity1().input(img1).mutate();}
	 * &#8212; run an Op adding poisson noise to an input image.</li>
	 * </ul>
	 * </p>
	 *
	 * @param opName The name of the Op to run
	 * @return The {@link OpBuilder} instance for builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">op(String, Hints)</a>
	 * @see <a href="#nullary-java.lang.String-"
	 *      title="For a series of convenience builds with arity pre-selected">nullary(String)</a>
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
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">op(String)</a>
	 * @see <a href="#nullary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a series of convenience builds with arity pre-selected">nullary(String,
	 *      Hints)</a>
	 */
	default OpBuilder op(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints);
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 0.
	 *
	 * @param opName The name of the Op to run
	 * @return The {@link org.scijava.ops.api.OpBuilder.Arity0} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#nullary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">nullary(String, Hints)</a>
	 */
	default OpBuilder.Arity0 nullary(final String opName) {
		return new OpBuilder(this, opName).arity0();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 1.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity1} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#unary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">unary(String, Hints)</a>
	 */
	default OpBuilder.Arity1 unary(final String opName) {
		return new OpBuilder(this, opName).arity1();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 2.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity2} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#binary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">binary(String, Hints)</a>
	 */
	default OpBuilder.Arity2 binary(final String opName) {
		return new OpBuilder(this, opName).arity2();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 3.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity3} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#ternary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">ternary(String, Hints)</a>
	 */
	default OpBuilder.Arity3 ternary(final String opName) {
		return new OpBuilder(this, opName).arity3();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 4.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity4} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#quaternary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">quaternary(String,
	 *      Hints)</a>
	 */
	default OpBuilder.Arity4 quaternary(final String opName) {
		return new OpBuilder(this, opName).arity4();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 5.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity5} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#quinary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">quinary(String, Hints)</a>
	 */
	default OpBuilder.Arity5 quinary(final String opName) {
		return new OpBuilder(this, opName).arity5();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 6.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity6} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#senary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">senary(String, Hints)</a>
	 */
	default OpBuilder.Arity6 senary(final String opName) {
		return new OpBuilder(this, opName).arity6();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 7.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity7} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#septenary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">septenary(String,
	 *      Hints)</a>
	 */
	default OpBuilder.Arity7 septenary(final String opName) {
		return new OpBuilder(this, opName).arity7();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 8.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity8} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#octonary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">octonary(String, Hints)</a>
	 */
	default OpBuilder.Arity8 octonary(final String opName) {
		return new OpBuilder(this, opName).arity8();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 9.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity9} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#nonary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">nonary(String, Hints)</a>
	 */
	default OpBuilder.Arity9 nonary(final String opName) {
		return new OpBuilder(this, opName).arity9();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 10.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity10} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#decenary-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">decenary(String, Hints)</a>
	 */
	default OpBuilder.Arity10 decenary(final String opName) {
		return new OpBuilder(this, opName).arity10();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 11.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity11} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#arity11-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">arity11(String, Hints)</a>
	 */
	default OpBuilder.Arity11 arity11(final String opName) {
		return new OpBuilder(this, opName).arity11();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 12.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity12} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#arity12-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">arity12(String, Hints)</a>
	 */
	default OpBuilder.Arity12 arity12(final String opName) {
		return new OpBuilder(this, opName).arity12();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 13.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity13} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#arity13-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">arity13(String, Hints)</a>
	 */
	default OpBuilder.Arity13 arity13(final String opName) {
		return new OpBuilder(this, opName).arity13();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 14.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity14} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#arity14-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">arity14(String, Hints)</a>
	 */
	default OpBuilder.Arity14 arity14(final String opName) {
		return new OpBuilder(this, opName).arity14();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 15.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity15} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#arity15-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">arity15(String, Hints)</a>
	 */
	default OpBuilder.Arity15 arity15(final String opName) {
		return new OpBuilder(this, opName).arity15();
	}

	/**
	 * Convenience version of {@link #op(String)} with arity pre-selected to 16.
	 *
	 * @param opName The name of the Op to run
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity16} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-"
	 *      title="For a Builder instance without the arity choice made">op(String)</a>
	 * @see <a href="#arity16-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="To specify a Hints instance to use">arity16(String, Hints)</a>
	 */
	default OpBuilder.Arity16 arity16(final String opName) {
		return new OpBuilder(this, opName).arity16();
	}

	/**
	 * As {@link #nullary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return The {@link org.scijava.ops.api.OpBuilder.Arity0} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#nullary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">nullary(String)</a>
	 */
	default OpBuilder.Arity0 nullary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity0();
	}

	/**
	 * As {@link #unary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity1} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#unary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">unary(String)</a>
	 */
	default OpBuilder.Arity1 unary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity1();
	}

	/**
	 * As {@link #binary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity2} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#binary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">binary(String)</a>
	 */
	default OpBuilder.Arity2 binary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity2();
	}

	/**
	 * As {@link #ternary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity3} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#ternary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">ternary(String)</a>
	 */
	default OpBuilder.Arity3 ternary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity3();
	}

	/**
	 * As {@link #quaternary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity4} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#quaternary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">quaternary(String)</a>
	 */
	default OpBuilder.Arity4 quaternary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity4();
	}

	/**
	 * As {@link #quinary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity5} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#quinary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">quinary(String)</a>
	 */
	default OpBuilder.Arity5 quinary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity5();
	}

	/**
	 * As {@link #senary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity6} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#senary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">senary(String)</a>
	 */
	default OpBuilder.Arity6 senary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity6();
	}

	/**
	 * As {@link #septenary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity7} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#septenary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">septenary(String)</a>
	 */
	default OpBuilder.Arity7 septenary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity7();
	}

	/**
	 * As {@link #octonary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity8} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#octonary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">octonary(String)</a>
	 */
	default OpBuilder.Arity8 octonary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity8();
	}

	/**
	 * As {@link #nonary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity9} instance for builder
	 *         chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#nonary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">nonary(String)</a>
	 */
	default OpBuilder.Arity9 nonary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity9();
	}

	/**
	 * As {@link #decenary(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity10} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#decenary-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">decenary(String)</a>
	 */
	default OpBuilder.Arity10 decenary(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity10();
	}

	/**
	 * As {@link #arity11(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity11} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#arity11-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">arity11(String)</a>
	 */
	default OpBuilder.Arity11 arity11(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity11();
	}

	/**
	 * As {@link #arity12(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity12} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#arity12-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">arity12(String)</a>
	 */
	default OpBuilder.Arity12 arity12(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity12();
	}

	/**
	 * As {@link #arity13(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity13} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#arity13-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">arity13(String)</a>
	 */
	default OpBuilder.Arity13 arity13(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity13();
	}

	/**
	 * As {@link #arity14(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity14} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#arity14-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">arity14(String)</a>
	 */
	default OpBuilder.Arity14 arity14(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity14();
	}

	/**
	 * As {@link #arity15(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity15} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#arity15-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">arity15(String)</a>
	 */
	default OpBuilder.Arity15 arity15(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity15();
	}

	/**
	 * As {@link #arity16(String)} but using a provided {@link Hints}.
	 *
	 * @param opName The name of the Op to run
	 * @param hints The {@code Hints} instance to use for Op matching
	 * @return A {@link org.scijava.ops.api.OpBuilder.Arity16} instance for
	 *         builder chaining.
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be
	 *           satisfied
	 * @see <a href="#op-java.lang.String-org.scijava.ops.api.Hints-"
	 *      title="For a Builder instance without the arity choice made">op(String,
	 *      Hints)</a>
	 * @see <a href="#arity16-java.lang.String-"
	 *      title="To use the default Hints for this OpEnvironment">arity16(String)</a>
	 */
	default OpBuilder.Arity16 arity16(final String opName, final Hints hints) {
		return new OpBuilder(this, opName, hints).arity16();
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
	OpInfo opify(Class<?> opClass, String... names);

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
	 * Registers all elements within a {@code Object[]} to this
	 * {@link OpEnvironment}
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
