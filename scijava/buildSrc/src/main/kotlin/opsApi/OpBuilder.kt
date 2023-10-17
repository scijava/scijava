package opsApi

import Generator
import license
import dontEdit
import joinToStringComma

object OpBuilder : Generator() {

    //    .include templates/main/java/org/scijava/ops/api/Globals.list
    override val arities = 1..maxArity

    //    [OpBuilder.java]
    fun inplaceMatchNumber(num: Int) = if (arity == 1) "" else "$num"

    override val generics get() = '<' + (1..arity).joinToStringComma { "I$it" } + "O>"

    val genericsWithoutOutput get() = '<' + (1..arity).joinToString { "I$it" } + '>'

    val genericsWildcardFunction get() = '<' + (1..arity).joinToStringComma { "I$it" } + "?>"

    val inputObjectsArgs get() = (1..arity).joinToString { "final I$it in$it" }

    val inputObjects get() = (1..arity).joinToString { "in$it" }

    val inputClassesArgs get() = (1..arity).joinToString { "final Class<I$it> in${it}Class" }

    val inputClassesToTypes get() = (1..arity).joinToString { "Nil.of(in${it}Class)" }

    val inputTypesArgs get() = (1..arity).joinToString { "final Nil<I$it> in${it}Type" }

    val inputTypesFromArgs get() = (1..arity).joinToString { "type(in$it)" }

    val inputTypesArgsWithOutput get() = (1..arity).joinToStringComma { "final Nil<I$it> in${it}Type" } + "final Nil<O> outType"

    val inputTypes get() = (1..arity).joinToString { "in${it}Type" }

    val inputTypesWithOutput get() = (1..arity).joinToStringComma { "in${it}Type" } + "outType"

    fun simplifiedClass(num: Int) = simplifiedInplace(num) + ".class"

    fun inplaceClass(num: Int) = inplaceType(num) + ".class"

    fun matchName(num: Int) = if (arity == 1) "matchInplace" else "matchInplace${num}"

    fun inplaceTypeVarNums(io: Int) = (1..arity).take(io - 1) + 'O' + (1..arity).drop(io)

    fun inplaceGenericsList(io: Int) = inplaceTypeVarNums(io).map { "I$it" }

    fun inplaceGenerics(io: Int) = '<' + inplaceGenericsList(io).joinToString() + '>'

    fun matchParams(io: Int) = inplaceGenericsList(io).joinToString {
        val arg = if (it[1] == 'O') it.lowercase() else "in" + it.substring(1)
        "final Nil<$it> ${arg}Type"
    }

    fun typeVarNums(io: Int) = (1..arity).take(io - 1) + 'O' + (1..arity).drop(io)

    fun basicParamsList(io: Int) = typeVarNums(io).map { if (it == 'O') "ioType" else "in${it}Type" }

    fun basicParams(io: Int) = basicParamsList(io).joinToString()

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.api;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.function.Producer;
import org.scijava.types.Nil;
import org.scijava.types.Types;

/**
 * Convenience class for looking up and/or executing ops using a builder
 * pattern. Typical entry point is through {@code OpEnvironment.op(String)}
 * - which contains full usage information.
 * <br/>
 * Note that the intermediate builder steps use the following acronyms:
 * <ul>
 *   <li><b>IV/OV:</b> Input/Output Value. Indicates instances will be used for matching; ideal if you want to directly run the matched Op, e.g. via <code>apply</code>, <code>compute</code>, <code>mutate</code> or <code>create</code> methods.</li>
 *   <li><b>IT/OT:</b> Input/Output Types. Indicates {@code Classes} will be used for matching; matching will produce an Op instance that can then be (re)used. There are two "Type" options: raw types or {@code Nil}s. If you are matching using a parameterized type use the {@code Nil} option to preserve the type parameter.</li>
 *   <li><b>OU:</b> Output Unknown. Indicates an output type/value has not been specified to the builder yet. The output, if any, will simply be an {@code Object}</li>
 * </ul>
 * </p>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 * @author Mark Hiner
 */
public class OpBuilder {

	private final OpEnvironment env;
	private final String opName;
	private Hints hints;

	public OpBuilder(final OpEnvironment env, final String opName) {
        this(env, opName, env.getDefaultHints());
	}

	public OpBuilder(final OpEnvironment env, final String opName, final Hints hints) {
		this.env = env;
		this.opName = opName;
		this.hints = hints;
	}

	/** Specifies an op that accepts no inputs&mdash;i.e., a nullary op. */
	public Arity0 arity0() { return new Arity0(); }

	/** Set the Hints instance for this builder */
	public void setHints(Hints hints) { this.hints = hints; }

	/** Get the Hints instance for this builder */
	public Hints hints() { return hints; }
"""
        forEachArity {
            +"""
	/** Specifies an op with $arity input${if (arity > 1) "s" else ""}. */
	public Arity$arity arity$arity() { return new Arity$arity(); }

"""
        }
        +"""
	// -- Helper methods --

	@SuppressWarnings({ "unchecked" })
	private <T> Nil<T> type(Object obj) {
		// FIXME: This vacuous T and unsafe cast is wrong.
		return (Nil<T>) Nil.of(env.genericType(obj));
	}

	private void checkComputerRefs(Object... objects) {
		checkRefs(objects.length - 1, "Output", objects);
	}

	private void checkInplaceRefs(int inplaceNo, Object... objects) {
		checkRefs(inplaceNo - 1, "Mutable input " + inplaceNo, objects);
	}

	private void checkRefs(int mutableIndex, String label, Object... objects) {
		for (int i = 0; i < objects.length; i++) {
			if (i == mutableIndex) continue;
			if (objects[mutableIndex] == objects[i]) {
				throw new IllegalArgumentException(label +
					" cannot be same reference as input #" + (i + 1));
			}
		}
	}

	// @formatter:off
	/*
	 * -- HELPER CLASSES --
	 * 
	 * For each arity, there are multiple conditions:
	 * * Input TYPES are given (IT) 
	 * 	1) The output is unspecified (OU): 
	 * 		a) matchable: Function, Inplace
	 * 		b) runnable: none
	 * 	2) The output type is given (OT): 
	 * 		a) matchable: Function, Computer
	 * 		b) runnable: none
	 *  
	 * * Input VALUES are given (IV) (N.B. this case applies for Arity0):
	 * 	1) The output is unspecified (OU): 
	 * 		a) matchable: Function, Inplace
	 * 		b) runnable: apply, mutate
	 * 	2) The output type is given (OT): 
	 * 		a) matchable: Function, Computer
	 * 		b) runnable: apply
	 * 	3) The output value is given (OV): 
	 * 		a) matchable: Computer
	 *  	b) runnable: compute
	 */
	// @formatter:on


	/**
	 * Abstract superclasses for all Arities.
	 */
	private abstract class Arity {
	    /** Get the Hints instance for this builder */
		public void setHints(Hints hints) { OpBuilder.this.setHints(hints); }

		/** Get the Hints instance for this builder */
		public Hints hints() { return OpBuilder.this.hints(); }
	}

	/**
	 * Builder with arity 0, output unspecified.
	 *
	 * @author Curtis Rueden
	 */
	public final class Arity0 extends Arity {

		/**
		 * Matches with this builder will use the given pre-allocated output instance.
		 *
		 * @see <a href="#[[#create()]]#" title="To match then immediately run a Producer Op without specifying its type, creating an Object.">create</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 * @see <a href="#[[#producer()]]#" title="For a reusable Op to create naively-typed Objects without re-matching.">producer</a>
		 */
		public <O> Arity0_OV<O> output(final O out) {
			return new Arity0_OV<>(out);
		}

		/**
		 * Matches with this builder will use the indicated output class.
		 *
		 * @see <a href="#[[#create()]]#" title="To match then immediately run a Producer Op without specifying its type, creating an Object.">create</a>
		 * @see <a href="#[[#output(O)]]#" title="To specify a concrete output instance. (e.g. pre-allocated for org.scijava.function.Computers)">output</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 * @see <a href="#[[#producer()]]#" title="For a reusable Op to create naively-typed Objects without re-matching.">producer</a>
		 */
		public <O> Arity0_OT<O> outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		/**
		 * Matches with this builder will use the output type of the indicated {@code org.scijava.types.Nil}'s generic parameter.
		 *
		 * @see <a href="#[[#create()]]#" title="To match then immediately run a Producer Op without specifying its type, creating an Object.">create</a>
		 * @see <a href="#[[#output(O)]]#" title="To specify a concrete output instance. (e.g. pre-allocated for org.scijava.function.Computers)">output</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#[[#producer()]]#" title="For a reusable Op to create naively-typed Objects without re-matching.">producer</a>
		 */
		public <O> Arity0_OT<O> outType(final Nil<O> outType) {
			return new Arity0_OT<>(outType);
		}

		/**
		 * Match a {@link org.scijava.function.Producer} op, based on the choices made with this builder, for creating {@code Object} instances.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#create()]]#" title="To match then immediately run a Producer Op without specifying its type, creating an Object.">create</a>
		 * @see <a href="#[[#output(O)]]#" title="To specify a concrete output instance. (e.g. pre-allocated for org.scijava.function.Computers)">output</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 */
		public Producer<?> producer() {
			final Nil<Producer<Object>> specialType = new Nil<>() {

				@Override
				public Type getType() {
					return Types.parameterize(Producer.class, new Type[] {
						Object.class });
				}
			};
			return env.op(opName, specialType, new Nil<?>[0], Nil.of(
				Object.class));
		}

		/**
		 * Match then immediately run a type-unsafe {@link org.scijava.function.Producer} op and get its output.
		 *
		 * @return The {@code Object} created by this op
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#output(O)]]#" title="To specify a concrete output instance. (e.g. pre-allocated for org.scijava.function.Computers)">output</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 * @see <a href="#[[#producer()]]#" title="For a reusable Op to create naively-typed Objects without re-matching.">producer</a>
		 */
		public Object create() {
			return producer().create();
		}
	}

	/**
	 * Builder with arity 0, output type given.
	 *
	 * @author Curtis Rueden
	 * @param <O> The type of the output.
	 */
	public final class Arity0_OT<O> extends Arity {

		private final Nil<O> outType;

		public Arity0_OT(final Nil<O> outType) {
			this.outType = outType;
		}

		/**
		 * Match a {@link org.scijava.function.Producer} op, based on the choices made with this builder, for creating {@code O}-typed instances.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#computer()]]#" title="For a reusable Op to process pre-allocated outputs without re-matching.">computer</a>
		 * @see <a href="#[[#create()]]#" title="To match then immediately run a Producer Op, creating an instance of this builder's output type.">create</a>
		 */
		public Producer<O> producer() {
			final Nil<Producer<O>> specialType = new Nil<>() {

				@Override
				public Type getType() {
					return Types.parameterize(Producer.class, new Type[] { outType
						.getType() });
				}
			};
			return env.op(opName, specialType, new Nil<?>[0], outType);
		}

		/**
		 * Match a {@link org.scijava.function.Computers} op, based on the choices made with this builder, for operating on pre-allocated output.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#create()]]#" title="To match then immediately run a Producer Op, creating an instance of this builder's output type.">create</a>
		 * @see <a href="#[[#producer()]]#" title="For a reusable Op to create objects of this builder's output type without re-matching.">producer</a>
		 */
		public Computers.Arity0<O> computer() {
			return matchComputer(env, opName, outType, OpBuilder.this.hints);
		}

		/**
		 * Match then immediately run a {@link org.scijava.function.Producer} op and get its output.
		 *
		 * @return The {@code O} created by this op
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#computer()]]#" title="For a reusable Op to process pre-allocated outputs without re-matching.">computer</a>
		 * @see <a href="#[[#producer()]]#" title="For a reusable Op to create objects of this builder's output type without re-matching.">producer</a>
		 */
		public O create() {
			return producer().create();
		}
	}

	/**
	 * Builder with arity 0, output value given.
	 *
	 * @author Curtis Rueden
	 * @param <O> The type of the output.
	 */
	public final class Arity0_OV<O> extends Arity {

		private final O out;

		public Arity0_OV(final O out) {
			this.out = out;
		}

		/**
		 * Match a {@link org.scijava.function.Computers} op, based on the choices made with this builder, for operating on pre-allocated output.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#compute()]]#" title="To match then immediately run a Computer Op using this builder's pre-allocated output.">compute</a>
		 */
		public Computers.Arity0<O> computer() {
			return matchComputer(env, opName, type(out), OpBuilder.this.hints);
		}

		/**
		 * Match then immediately run a {@link org.scijava.function.Computers} op on the provided output container.
		 *
		 * @see <a href="#[[#computer()]]#" title="For a reusable Op to process pre-allocated outputs without re-matching.">computer</a>
		 */
		public void compute() {
			computer().compute(out);
		}
	}

"""
        forEachArity {
            +"""
	/**
	 * Builder with arity $arity, no input or output information provided.
	 *
	 * @author Mark Hiner
	 */
	public final class Arity$arity extends Arity {

		/**
		 * Specifies an op with $arity input${if (arity > 1) "s" else ""}, matched by input value.
		 *
		 * @see <a href="#inType-${(1..arity).joinToString("-") { "java.lang.Class" }}-" title="To specify the input type(s) without providing concrete instance(s).">inType(${(1..arity).joinToString { "Class" }})</a>
		 * @see <a href="#inType-${(1..arity).joinToString("-") { "org.scijava.types.Nil" }}-" title="If your input type(s) have generic parameters to preserve.">inType(${(1..arity).joinToString { "Nil" }})</a>
		 */
		public $genericsWithoutOutput Arity${arity}_IV_OU$genericsWithoutOutput input($inputObjectsArgs)
		{
			return new Arity${arity}_IV_OU<>($inputObjects);
		}

		/**
		 * Specifies an op with $arity input${if (arity > 1) "s" else ""}, matched by input type.
		 *
		 * @see <a href="#[[#input]]#(${(1..arity).joinToString { "I$it" }})" title="To specify concrete input parameter(s). (e.g. to directly run the Op via this builder)">input</a>
		 * @see <a href="#inType-${(1..arity).joinToString("-") { "org.scijava.types.Nil" }}-" title="If your input type(s) have generic parameters to preserve.">inType(${(1..arity).joinToString { "Nil" }})</a>
		 */
		public $genericsWithoutOutput Arity${arity}_IT_OU$genericsWithoutOutput inType($inputClassesArgs)
		{
			return inType($inputClassesToTypes);
		}

		/**
		 * Specifies an op with $arity input${if (arity > 1) "s" else ""}, matched using the {@code Nil}'s generic parameter.
		 *
		 * @see <a href="#[[#input]]#(${(1..arity).joinToString { "I$it" }})" title="To specify concrete input parameter(s). (e.g. to directly run the Op via this builder)">input</a>
		 * @see <a href="#inType-${(1..arity).joinToString("-") { "java.lang.Class" }}-" title="To specify the input type(s) without providing concrete instance(s).">inType(${(1..arity).joinToString { "Class" }})</a>
		 */
		public $genericsWithoutOutput Arity${arity}_IT_OU$genericsWithoutOutput inType($inputTypesArgs)
		{
			return new Arity${arity}_IT_OU<>($inputTypes);
		}

	}
"""
        }

        forEachArity {
            +"""
	/**
	 * Builder with arity $arity, input type given, output type given.
	 *
	 * @author Curtis Rueden"""
            for (a in 1..arity)
                +"""
	 * @param <I$a> The type of input $a."""
            +"""
	 * @param <O> The type of the output.
	 */
	public final class Arity${arity}_IT_OT$generics extends Arity {
"""
            for (a in 1..arity)
                +"""
		private final Nil<I$a> in${a}Type;"""
            +"""
		private final Nil<O> outType;

		public Arity${arity}_IT_OT($inputTypesArgsWithOutput)
		{"""
            for (a in 1..arity)
                +"""
			this.in${a}Type = in${a}Type;"""
            +"""
			this.outType = outType;
		}

		/**
		 * Match a {@link org.scijava.function.Functions} op, based on the choices made with this builder.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#computer()]]#" title="For a reusable Op to process pre-allocated outputs without re-matching.">computer</a>
		 */
		public $functionArity$generics function() {
			return matchFunction(env, opName, $inputTypesWithOutput, OpBuilder.this.hints);
		}

		/**
		 * Match a {@link org.scijava.function.Computers} op, based on the choices made with this builder, for operating on pre-allocated output.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 */
		public Computers.Arity$arity$generics computer() {
			return matchComputer(env, opName, $inputTypesWithOutput, OpBuilder.this.hints);
		}
	}

	/**
	 * Builder with arity $arity, input type given, output unspecified.
	 *
	 * @author Curtis Rueden"""
            for (a in 1..arity)
                +"""
	 * @param <I$a> The type of input $a."""
            +"""
	 */
	public final class Arity${arity}_IT_OU$genericsWithoutOutput extends Arity {
"""
            for (a in 1..arity)
                +"""
		private final Nil<I$a> in${a}Type;"""
            +"""

		public Arity${arity}_IT_OU($inputTypesArgs)
		{"""
            for (a in 1..arity)
                +"""
			this.in${a}Type = in${a}Type;"""
            +"""
		}

		/**
		 * Matches with this builder will use the indicated output class.
		 *
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 * @see <a href="#inplace${if (arity > 1) "1" else ""}()" title="For a reusable Op that modifies a provided input parameter in-place.">inplace</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 */
		public <O> Arity${arity}_IT_OT$generics outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		/**
		 * Matches with this builder will use the output type of the indicated {@code org.scijava.types.Nil}'s generic parameter.
		 *
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 * @see <a href="#inplace${if (arity > 1) "1" else ""}()" title="For a reusable Op that modifies a provided input parameter in-place.">inplace</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 */
		public <O> Arity${arity}_IT_OT$generics outType(final Nil<O> outType) {
			return new Arity${arity}_IT_OT<>($inputTypesWithOutput);
		}

		/**
		 * Match a {@link org.scijava.function.Functions} op based on the choices made with this builder.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 * @see <a href="#inplace${if (arity > 1) "1" else ""}()" title="For a reusable Op that modifies a provided input parameter in-place.">inplace</a>
		 */
		public $functionArity$genericsWildcardFunction function() {
			return matchFunction(env, opName, $inputTypes, Nil.of(Object.class), OpBuilder.this.hints);
		}
"""
            for (a in 1..arity) {
                val ordinality = when (a) {
                    1 -> "st"
                    2 -> "nd"
                    3 -> "rd"
                    else -> "th"
                }
                +"""
		/**
		 * Match an {@link org.scijava.function.Inplaces} op, based on the choices made with this builder, to mutate the $a$ordinality parameter.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 */
		public Inplaces.Arity${inplaceSuffix(a)}$genericsWithoutOutput inplace${inplaceMatchNumber(a)}() {
			return ${matchName(a)}(env, opName, $inputTypes);
		}
"""
            }
            +"""
	}

	/**
	 * Builder with arity $arity, input value given, output type given.
	 *
	 * @author Curtis Rueden"""
            for (a in 1..arity)
                +"""
	 * @param <I$a> The type of input $a."""

            +"""
	 * @param <O> The type of the output.
	 */
	public final class Arity${arity}_IV_OT$generics extends Arity {
"""
            for (a in 1..arity)
                +"""
		private final I$a in${a};"""

            +"""
		private final Nil<O> outType;

		public Arity${arity}_IV_OT($inputObjectsArgs, final Nil<O> outType)
		{"""

            for (a in 1..arity)
                +"""
			this.in$a = in$a;"""

            +"""
			this.outType = outType;
		}

		/**
		 * Match a {@link org.scijava.function.Functions} op based on the choices made with this builder.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#apply()]]#" title="To match then immediately run a Function Op using the input values provided to this builder.">apply</a>
		 * @see <a href="#[[#computer()]]#" title="For a reusable Op to process pre-allocated outputs without re-matching.">computer</a>
		 */
		public $functionArity$generics function() {
			return matchFunction(env, opName, $inputTypesFromArgs, outType, OpBuilder.this.hints);
		}
	
		/**
		 * Match a {@link org.scijava.function.Computers} op, based on the choices made with this builder, for operating on pre-allocated output.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#apply()]]#" title="To match then immediately run a Function Op using the input values provided to this builder.">apply</a>
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 */
		public Computers.Arity$arity$generics computer() {
			return matchComputer(env, opName, $inputTypesFromArgs, outType, OpBuilder.this.hints);
		}

		/**
		 * Match then immediately run a {@link org.scijava.function.Functions} op and get its output.
		 *
		 * @return The output of this function
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#computer()]]#" title="For a reusable Op to process pre-allocated outputs without re-matching.">computer</a>
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 */
		public O apply() {
			return function().apply($inputObjects);
		}
	}

	/**
	 * Builder with arity $arity, input value given, output unspecified.
	 *
	 * @author Curtis Rueden"""
            for (a in 1..arity)
                +"""
	 * @param <I$a> The type of input $a."""

            +"""
	 */
	public final class Arity${arity}_IV_OU$genericsWithoutOutput extends Arity {
"""
            for (a in 1..arity)
                +"""
		private final I$a in$a;"""

            +"""

		public Arity${arity}_IV_OU($inputObjectsArgs)
		{"""
            for (a in 1..arity)
                +"""
			this.in$a = in$a;"""

            +"""
		}

		/**
		 * Matches with this builder will use the given pre-allocated output instance.
		 *
		 * @see <a href="#[[#apply()]]#" title="To match then immediately run a Function Op using the input values provided to this builder.">apply</a>
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 * @see <a href="#inplace${if (arity > 1) "1" else ""}()" title="For a reusable Op that modifies a provided input parameter in-place.">inplace</a>
		 * @see <a href="#mutate${if (arity > 1) "1" else ""}()" title="To match then immediately run an Inplace Op modifying a provided input parameter.">mutate</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 */
		public <O> Arity${arity}_IV_OV$generics output(final O out) {
			checkComputerRefs($inputObjects, out);
			return new Arity${arity}_IV_OV<>($inputObjects, out);
		}

		/**
		 * Matches with this builder will use the indicated output class.
		 *
		 * @see <a href="#[[#apply()]]#" title="To match then immediately run a Function Op using the input values provided to this builder.">apply</a>
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 * @see <a href="#inplace${if (arity > 1) "1" else ""}()" title="For a reusable Op that modifies a provided input parameter in-place.">inplace</a>
		 * @see <a href="#mutate${if (arity > 1) "1" else ""}()" title="To match then immediately run an Inplace Op modifying a provided input parameter.">mutate</a>
		 * @see <a href="#[[#output(O)]]#" title="To specify a concrete output instance. (e.g. pre-allocated for org.scijava.function.Computers)">output</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 */
		public <O> Arity${arity}_IV_OT$generics outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		/**
		 * Matches with this builder will use the output type of the indicated {@code org.scijava.types.Nil}'s generic parameter.
		 *
		 * @see <a href="#[[#apply()]]#" title="To match then immediately run a Function Op using the input values provided to this builder.">apply</a>
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 * @see <a href="#inplace${if (arity > 1) "1" else ""}()" title="For a reusable Op that modifies a provided input parameter in-place.">inplace</a>
		 * @see <a href="#mutate${if (arity > 1) "1" else ""}()" title="To match then immediately run an Inplace Op modifying a provided input parameter.">mutate</a>
		 * @see <a href="#[[#output(O)]]#" title="To specify a concrete output instance. (e.g. pre-allocated for org.scijava.function.Computers)">output</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 */
		public <O> Arity${arity}_IV_OT$generics outType(final Nil<O> outType) {
			return new Arity${arity}_IV_OT<>($inputObjects, outType);
		}

		/**
		 * Match a {@link org.scijava.function.Functions} op based on the choices made with this builder.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#apply()]]#" title="To match then immediately run a Function Op using the input values provided to this builder.">apply</a>
		 * @see <a href="#inplace${if (arity > 1) "1" else ""}()" title="For a reusable Op that modifies a provided input parameter in-place.">inplace</a>
		 * @see <a href="#mutate${if (arity > 1) "1" else ""}()" title="To match then immediately run an Inplace Op modifying a provided input parameter.">mutate</a>
		 * @see <a href="#[[#output(O)]]#" title="To specify a concrete output instance. (e.g. pre-allocated for org.scijava.function.Computers)">output</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 */
		public $functionArity$genericsWildcardFunction function() {
			return matchFunction(env, opName, $inputTypesFromArgs, Nil.of(Object.class), OpBuilder.this.hints);
		}
"""

            for (a in 1..arity) {
                val ordinality = when (a) {
                    1 -> "st"
                    2 -> "nd"
                    3 -> "rd"
                    else -> "th"
                }
                +"""
		/**
		 * Match an {@link org.scijava.function.Inplaces} op, based on the choices made with this builder, to mutate the $a$ordinality parameter.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#apply()]]#" title="To match then immediately run a Function Op using the input values provided to this builder.">apply</a>
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 * @see <a href="#mutate${if (arity > 1) "1" else ""}()" title="To match then immediately run an Inplace Op modifying a provided input parameter.">mutate</a>
		 * @see <a href="#[[#output(O)]]#" title="To specify a concrete output instance. (e.g. pre-allocated for org.scijava.function.Computers)">output</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 */
		public Inplaces.Arity${inplaceSuffix(a)}$genericsWithoutOutput inplace${inplaceMatchNumber(a)}() {
			checkInplaceRefs($a, $inputObjects);
			return ${matchName(a)}(env, opName, $inputTypesFromArgs);
		}
"""
            }

            +"""
		/**
		 * Match then immediately run a type-unsafe {@link org.scijava.function.Functions} op and get its output.
		 *
		 * @return The output of this function
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 * @see <a href="#inplace${if (arity > 1) "1" else ""}()" title="For a reusable Op that modifies a provided input parameter in-place.">inplace</a>
		 * @see <a href="#mutate${if (arity > 1) "1" else ""}()" title="To match then immediately run an Inplace Op modifying a provided input parameter.">mutate</a>
		 * @see <a href="#[[#output(O)]]#" title="To specify a concrete output instance. (e.g. pre-allocated for org.scijava.function.Computers)">output</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 */
		public Object apply() {
			return function().apply($inputObjects);
		}
"""
            for (a in 1..arity) {
                val ordinality = when (a) {
                    1 -> "st"
                    2 -> "nd"
                    3 -> "rd"
                    else -> "th"
                }
                +"""
		/**
		 * Match then immediately run an {@link org.scijava.function.Inplaces} op to mutate the $a$ordinality parameter.
		 *
		 * @see <a href="#[[#apply()]]#" title="To match then immediately run a Function Op using the input values provided to this builder.">apply</a>
		 * @see <a href="#[[#function()]]#" title="For a reusable Op that generates output instances based on its inputs.">function</a>
		 * @see <a href="#inplace${if (arity > 1) "1" else ""}()" title="For a reusable Op that modifies a provided input parameter in-place.">inplace</a>
		 * @see <a href="#[[#output(O)]]#" title="To specify a concrete output instance. (e.g. pre-allocated for org.scijava.function.Computers)">output</a>
		 * @see <a href="#outType-java.lang.Class-" title="To specify the output type without providing a concrete instance.">outType(Class)</a>
		 * @see <a href="#outType-org.scijava.types.Nil-" title="To specify the output type, preserving its generic parameters.">outType(Nil)</a>
		 */
		public void mutate${inplaceMatchNumber(a)}() {
			inplace${inplaceMatchNumber(a)}().mutate($inputObjects);
		}
"""
            }

            +"""
	}

	/**
	 * Builder with arity $arity, input value given, output value given.
	 *
	 * @author Curtis Rueden"""

            for (a in 1..arity)
                +"""
	 * @param <I$a> The type of input $a."""

            +"""
	 */
	public final class Arity${arity}_IV_OV$generics extends Arity {
"""

            for (a in 1..arity)
                +"""
		private final I$a in$a;"""

            +"""
		private final O out;

		public Arity${arity}_IV_OV($inputObjectsArgs, final O out)
		{"""

            for (a in 1..arity)
                +"""
			this.in$a = in$a;"""

            +"""
			this.out = out;
		}

		/**
		 * Match a {@link org.scijava.function.Computers} op, based on the choices made with this builder, for operating on pre-allocated output.
		 *
		 * @return An instance of the matched op, e.g. for reuse.
		 *
		 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
		 *
		 * @see <a href="#[[#compute()]]#" title="To match then immediately run a Computer Op using this builder's pre-allocated output.">compute</a>
		 */
		public Computers.Arity$arity$generics computer() {
			return matchComputer(env, opName, $inputTypesFromArgs, type(out), OpBuilder.this.hints);
		}

		/**
		 * Match then immediately run a {@link org.scijava.function.Computers} op using the provided pre-allocated output.
		 *
		 * @see <a href="#[[#computer()]]#" title="For a reusable Op to process pre-allocated outputs without re-matching.">computer</a>
		 */
		public void compute() {
			computer().compute($inputObjects, out);
		}
	}"""
        }
        +"""
"""

        forEachArity(0..maxArity) {
            val maybeSee = when {
                arity > 0 -> """* @see <a href="#[[#]]#${matchName(1)}(org.scijava.ops.api.OpEnvironment, java.lang.String, ${(1..arity).joinToString { "org.scijava.types.Nil" }})" title="For a reusable Op that modifies a provided input parameter in-place.">OpBuilder.${matchName(1)}(OpEnvironment, String, ${(1..arity).joinToString { "Nil" }})</a>""" + "\n"
                else -> ""
            } + "*/"
            +"""
	/**
	 * Static utility method to match a {@link org.scijava.function.Functions} op with $arity input${if (arity != 1) "s" else ""}.
	 *
	 * @return An instance of the matched op, e.g. for reuse.
	 *
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
	 *
	 * @see <a href="#[[#matchComputer]]#(org.scijava.ops.api.OpEnvironment, java.lang.String, ${(1..arity).joinToString { "org.scijava.types.Nil" }}, org.scijava.types.Nil)" title="For a reusable Op to process pre-allocated outputs without re-matching.">OpBuilder.matchComputer(OpEnvironment, String, ${(1..arity).joinToString { "Nil" }}, Nil)</a>
     $maybeSee
	@SuppressWarnings({ "unchecked" })
	public static $generics $functionArity$generics matchFunction(final OpEnvironment env, final String opName, $inputTypesArgsWithOutput)
	{
		return matchFunctionHelper(env, opName, $functionArity.class, outType${if (arity == 0) "" else ", "}$inputTypes);
	}
"""
        }

        forEachArity(0..maxArity) {
            +"""
	/**
	 * As {@link OpBuilder#matchFunction}, but match using the provided {@code Hints}.
	 */
	@SuppressWarnings({ "unchecked" })
	public static $generics $functionArity$generics matchFunction(final OpEnvironment env, final String opName, $inputTypesArgsWithOutput, final Hints hints)
	{
		return matchFunctionHelper(env, opName, hints, $functionArity.class, outType${if (arity == 0) "" else ", "}$inputTypes);
	}
"""
        }
        +"""
	@SuppressWarnings({ "unchecked" })
	private static <T> T matchFunctionHelper(final OpEnvironment env, final String opName,
		final Class<T> opClass, final Nil<?> outType, final Nil<?>... inTypes)
	{
		final Type[] types = new Type[inTypes.length + 1];
		for (int i = 0; i < inTypes.length; i++)
			types[i] = inTypes[i].getType();
		types[types.length - 1] = outType.getType();
		final Type specialType = Types.parameterize(opClass, types);
		return (T) env.op(opName, Nil.of(specialType), inTypes, outType);
	}

	@SuppressWarnings({ "unchecked" })
	private static <T> T matchFunctionHelper(final OpEnvironment env, final String opName, final Hints hints,
		final Class<T> opClass, final Nil<?> outType, final Nil<?>... inTypes)
	{
		final Type[] types = new Type[inTypes.length + 1];
		for (int i = 0; i < inTypes.length; i++)
			types[i] = inTypes[i].getType();
		types[types.length - 1] = outType.getType();
		final Type specialType = Types.parameterize(opClass, types);
		return (T) env.op(opName, Nil.of(specialType), inTypes, outType, hints);
	}
"""

        forEachArity(0..maxArity) {
            val maybeSee = when {
                arity > 0 -> """* @see <a href="#[[#]]#${matchName(1)}(org.scijava.ops.api.OpEnvironment, java.lang.String, ${(1..arity).joinToString { "org.scijava.types.Nil" }})" title="For a reusable Op that modifies a provided input parameter in-place.">OpBuilder.${matchName(1)}(OpEnvironment, String, ${(1..arity).joinToString { "Nil" }})</a>"""
                else -> ""
            } + "*/"
            +"""
	/**
	 * Static utility method to match a {@link org.scijava.function.Computers} op with $arity input${if (arity != 1) "s" else ""} for operating on pre-allocated output.
	 *
	 * @return An instance of the matched op, e.g. for reuse.
	 *
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
	 *
	 * @see <a href="#[[#matchFunction]]#(org.scijava.ops.api.OpEnvironment, java.lang.String, ${(1..arity).joinToString { "org.scijava.types.Nil" }}, org.scijava.types.Nil)" title="For a reusable Op that generates output instances based on its inputs.">OpBuilder.matchFunction(OpEnvironment, String, ${(1..arity).joinToString { "Nil" }}, Nil)</a>
     $maybeSee
	@SuppressWarnings("unchecked")
	public static $generics $computerArity$generics matchComputer(final OpEnvironment env, final String opName, $inputTypesArgsWithOutput)
	{
		return matchComputerHelper(env, opName, $computerArity.class, outType${if (arity == 0) "" else ", "}$inputTypes);
	}
"""
        }

        forEachArity(0..maxArity) {
            +"""
	/**
	 * As {@link OpBuilder#matchComputer}, but match using the provided {@code Hints}.
	 */
	@SuppressWarnings("unchecked")
	public static $generics $computerArity$generics matchComputer(final OpEnvironment env, final String opName, $inputTypesArgsWithOutput, final Hints hints)
	{
		return matchComputerHelper(env, opName, hints, $computerArity.class, outType${if (arity == 0) "" else ", "}$inputTypes);
	}
"""
        }

        +"""
	@SuppressWarnings({ "unchecked" })
	private static <T> T matchComputerHelper(final OpEnvironment env, final String opName, final Class<T> opClass, final Nil<?> outType, final Nil<?>... inTypes)
	{
		final Type[] types = new Type[inTypes.length + 1];
		for (int i = 0; i < inTypes.length; i++)
			types[i] = inTypes[i].getType();
		types[types.length - 1] = outType.getType();
		final Type specialType = Types.parameterize(opClass, types);
		final Nil<?>[] nils = new Nil[inTypes.length + 1];
		System.arraycopy(inTypes, 0, nils, 0, inTypes.length);
		nils[nils.length - 1] = outType;
		return (T) env.op(opName, Nil.of(specialType), nils, outType);
	}

	@SuppressWarnings({ "unchecked" })
	private static <T> T matchComputerHelper(final OpEnvironment env, final String opName, final Hints hints, final Class<T> opClass, final Nil<?> outType, final Nil<?>... inTypes)
	{
		final Type[] types = new Type[inTypes.length + 1];
		for (int i = 0; i < inTypes.length; i++)
			types[i] = inTypes[i].getType();
		types[types.length - 1] = outType.getType();
		final Type specialType = Types.parameterize(opClass, types);
		final Nil<?>[] nils = new Nil[inTypes.length + 1];
		System.arraycopy(inTypes, 0, nils, 0, inTypes.length);
		nils[nils.length - 1] = outType;
		return (T) env.op(opName, Nil.of(specialType), nils, outType, hints);
	}
"""

        forEachArity {

            for (a in 1..arity) {

                val ordinality = when (a) {
                    1 -> "st"
                    2 -> "nd"
                    3 -> "rd"
                    else -> "th"
                }
                +"""
	/**
	 * Static utility method to match an {@link org.scijava.function.Inplaces} op with $arity input${if (arity != 1) "s" else ""}, modifying the $a$ordinality input in-place.
	 *
	 * @return An instance of the matched op, e.g. for reuse.
	 *
	 * @throws org.scijava.ops.api.OpMatchingException if the Op request cannot be satisfied.
	 *
	 * @see <a href="#[[#matchComputer]]#(org.scijava.ops.api.OpEnvironment, java.lang.String, ${(1..arity).joinToString { "org.scijava.types.Nil" }}, org.scijava.types.Nil)" title="For a reusable Op to process pre-allocated outputs without re-matching.">OpBuilder.matchComputer(OpEnvironment, String, ${(1..arity).joinToString { "Nil" }}, Nil)</a>
	 * @see <a href="#[[#matchFunction]]#(org.scijava.ops.api.OpEnvironment, java.lang.String, ${(1..arity).joinToString { "org.scijava.types.Nil" }}, org.scijava.types.Nil)" title="For a reusable Op that generates output instances based on its inputs.">OpBuilder.matchFunction(OpEnvironment, String, ${(1..arity).joinToString { "Nil" }}, Nil)</a>
	 */
	@SuppressWarnings({ "unchecked" })
	public static ${inplaceGenerics(a)} ${inplaceType(a)}${inplaceGenerics(a)} ${matchName(a)}(final OpEnvironment env, final String opName, ${matchParams(a)})
	{
		return matchInplaceHelper(env, opName, ${inplaceClass(a)}, ioType, new Nil[] {${basicParams(a)}});
	}
"""
            }
        }

        forEachArity {
            for (a in 1..arity)
                +"""
	/**
	 * As {@link OpBuilder#${matchName(a)}}, but match using the provided {@code Hints}.
	 */
	@SuppressWarnings({ "unchecked" })
	public static ${inplaceGenerics(a)} ${inplaceType(a)}${inplaceGenerics(a)} ${matchName(a)}(final OpEnvironment env, final String opName, ${matchParams(a)}, final Hints hints)
	{
		return matchInplaceHelper(env, opName, hints, ${inplaceType(a)}.class, ioType, new Nil[] {${basicParams(a)}});
	}
"""
        }
        +"""
	@SuppressWarnings({ "unchecked" })
	private static <T> T matchInplaceHelper(final OpEnvironment env, final String opName,
		final Class<T> opClass, final Nil<?> outType, final Nil<?>... inTypes)
	{
		final Type[] types = new Type[inTypes.length];
		for (int i = 0; i < inTypes.length; i++)
			types[i] = inTypes[i].getType();
		final Type specialType = Types.parameterize(opClass, types);
		return (T) env.op(opName, Nil.of(specialType), inTypes, outType);
	}

	@SuppressWarnings({ "unchecked" })
	private static <T> T matchInplaceHelper(final OpEnvironment env, final String opName, final Hints hints,
		final Class<T> opClass, final Nil<?> outType, final Nil<?>... inTypes)
	{
		final Type[] types = new Type[inTypes.length];
		for (int i = 0; i < inTypes.length; i++)
			types[i] = inTypes[i].getType();
		final Type specialType = Types.parameterize(opClass, types);
		return (T) env.op(opName, Nil.of(specialType), inTypes, outType, hints);
	}
}
"""
    }
}