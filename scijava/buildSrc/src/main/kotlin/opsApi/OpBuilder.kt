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
        val arg = if (it[1] == 'O') it.toLowerCase() else "in" + it.substring(1)
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
 * pattern.
 * <p>
 * TODO: Examples
 * </p>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public class OpBuilder {

	private final OpEnvironment env;
	private final String opName;

	public OpBuilder(final OpEnvironment env, final String opName) {
		this.env = env;
		this.opName = opName;
	}

	/** Specifies the op accepts no inputs&mdash;i.e., a nullary op. */
	public Arity0_OU input() {
		return new Arity0_OU();
	}
"""
        forEachArity {
            +"""
	/** Specifies $arity input by value. */
	public $genericsWithoutOutput Arity${arity}_IV_OU$genericsWithoutOutput input($inputObjectsArgs)
	{
		return new Arity${arity}_IV_OU<>($inputObjects);
	}

	/** Specifies $arity input by raw type. */
	public $genericsWithoutOutput Arity${arity}_IT_OU$genericsWithoutOutput inType($inputClassesArgs)
	{
		return inType($inputClassesToTypes);
	}

	/** Specifies $arity input by generic type. */
	public $genericsWithoutOutput Arity${arity}_IT_OU$genericsWithoutOutput inType($inputTypesArgs)
	{
		return new Arity${arity}_IT_OU<>($inputTypes);
	}
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
	 * Builder with arity 0, output unspecified.
	 *
	 * @author Curtis Rueden
	 */
	public final class Arity0_OU {

		public <O> Arity0_OV<O> output(final O out) {
			return new Arity0_OV<>(out);
		}

		public <O> Arity0_OT<O> outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		public <O> Arity0_OT<O> outType(final Nil<O> outType) {
			return new Arity0_OT<>(outType);
		}

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

		public Producer<?> producer(final Hints hints) {
			final Nil<Producer<Object>> specialType = new Nil<>() {

				@Override
				public Type getType() {
					return Types.parameterize(Producer.class, new Type[] {
						Object.class });
				}
			};
			return env.op(opName, specialType, new Nil<?>[0], Nil.of(
				Object.class, hints));
		}

		public Object create() {
			return producer().create();
		}

		public Object create(final Hints hints) {
			return producer(hints).create();
		}
	}

	/**
	 * Builder with arity 0, output type given.
	 *
	 * @author Curtis Rueden
	 * @param <O> The type of the output.
	 */
	public final class Arity0_OT<O> {

		private final Nil<O> outType;

		public Arity0_OT(final Nil<O> outType) {
			this.outType = outType;
		}

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

		public Producer<O> producer(final Hints hints) {
			final Nil<Producer<O>> specialType = new Nil<>() {

				@Override
				public Type getType() {
					return Types.parameterize(Producer.class, new Type[] { outType
						.getType() });
				}
			};
			return env.op(opName, specialType, new Nil<?>[0], outType, hints);
		}

		public Computers.Arity0<O> computer() {
			return matchComputer(env, opName, outType);
		}

		public O create() {
			return producer().create();
		}

		public Computers.Arity0<O> computer(Hints hints) {
			return matchComputer(env, opName, outType, hints);
		}

		public O create(Hints hints) {
			return producer(hints).create();
		}

	}

	/**
	 * Builder with arity 0, output value given.
	 *
	 * @author Curtis Rueden
	 * @param <O> The type of the output.
	 */
	public final class Arity0_OV<O> {

		private final O out;

		public Arity0_OV(final O out) {
			this.out = out;
		}

		public Computers.Arity0<O> computer() {
			return matchComputer(env, opName, type(out));
		}

		public void compute() {
			computer().compute(out);
		}

		public Computers.Arity0<O> computer(final Hints hints) {
			return matchComputer(env, opName, type(out), hints);
		}

		public void compute(final Hints hints) {
			computer(hints).compute(out);
		}

	}
"""
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
	public final class Arity${arity}_IT_OT$generics {
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

		public $functionArity$generics function() {
			return matchFunction(env, opName, $inputTypesWithOutput);
		}

		public $functionArity$generics function(final Hints hints) {
			return matchFunction(env, opName, $inputTypesWithOutput, hints);
		}

		public Computers.Arity$arity$generics computer() {
			return matchComputer(env, opName, $inputTypesWithOutput);
		}

		public Computers.Arity$arity$generics computer(final Hints hints) {
			return matchComputer(env, opName, $inputTypesWithOutput, hints);
		}
"""
            for (a in 1..arity)
                +"""
		public Inplaces.Arity${inplaceSuffix(a)}$genericsWithoutOutput inplace${inplaceMatchNumber(a)}() {
			return matchInplace${inplaceMatchNumber(a)}(env, opName, $inputTypes);
		}
"""

            +"""
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
	public final class Arity${arity}_IT_OU$genericsWithoutOutput {
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

		public <O> Arity${arity}_IT_OT$generics outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		public <O> Arity${arity}_IT_OT$generics outType(final Nil<O> outType) {
			return new Arity${arity}_IT_OT<>($inputTypesWithOutput);
		}

		public $functionArity$genericsWildcardFunction function() {
			return matchFunction(env, opName, $inputTypes, Nil.of(Object.class));
		}

		public $functionArity$genericsWildcardFunction function(final Hints hints) {
			return matchFunction(env, opName, $inputTypes, Nil.of(Object.class), hints);
		}
"""
            for (a in 1..arity)
                +"""
		public Inplaces.Arity${inplaceSuffix(a)}$genericsWithoutOutput inplace${inplaceMatchNumber(a)}() {
			return matchInplace${inplaceMatchNumber(a)}(env, opName, $inputTypes);
		}
"""

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
	public final class Arity${arity}_IV_OT$generics {
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

		public $functionArity$generics function() {
			return matchFunction(env, opName, $inputTypesFromArgs, outType);
		}
	
		public $functionArity$generics function(final Hints hints) {
			return matchFunction(env, opName, $inputTypesFromArgs, outType, hints);
		}
	
		public Computers.Arity$arity$generics computer() {
			return matchComputer(env, opName, $inputTypesFromArgs, outType);
		}

		public Computers.Arity$arity$generics computer(final Hints hints) {
			return matchComputer(env, opName, $inputTypesFromArgs, outType, hints);
		}
	
		public O apply() {
			return function().apply($inputObjects);
		}

		public O apply(final Hints hints) {
			return function(hints).apply($inputObjects);
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
	public final class Arity${arity}_IV_OU$genericsWithoutOutput {
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

		public <O> Arity${arity}_IV_OV$generics output(final O out) {
			checkComputerRefs($inputObjects, out);
			return new Arity${arity}_IV_OV<>($inputObjects, out);
		}

		public <O> Arity${arity}_IV_OT$generics outType(final Class<O> outType) {
			return outType(Nil.of(outType));
		}

		public <O> Arity${arity}_IV_OT$generics outType(final Nil<O> outType) {
			return new Arity${arity}_IV_OT<>($inputObjects, outType);
		}

		public $functionArity$genericsWildcardFunction function() {
			return matchFunction(env, opName, $inputTypesFromArgs, Nil.of(Object.class));
		}

		public $functionArity$genericsWildcardFunction function(final Hints hints) {
			return matchFunction(env, opName, $inputTypesFromArgs, Nil.of(Object.class), hints);
		}
"""
            for (a in 1..arity)
                +"""
		public Inplaces.Arity${inplaceSuffix(a)}$genericsWithoutOutput inplace${inplaceMatchNumber(a)}() {
			checkInplaceRefs($a, $inputObjects);
			return matchInplace${inplaceMatchNumber(a)}(env, opName, $inputTypesFromArgs);
		}
"""

            for (a in 1..arity)
                +"""
		public Inplaces.Arity${inplaceSuffix(a)}$genericsWithoutOutput inplace${inplaceMatchNumber(a)}(final Hints hints) {
			checkInplaceRefs($a, $inputObjects);
			return matchInplace${inplaceMatchNumber(a)}(env, opName, $inputTypesFromArgs, hints);
		}
"""

            +"""
		public Object apply() {
			return function().apply($inputObjects);
		}

		public Object apply(final Hints hints) {
			return function(hints).apply($inputObjects);
		}
"""
            for (a in 1..arity)
                +"""
		public void mutate${inplaceMatchNumber(a)}() {
			inplace${inplaceMatchNumber(a)}().mutate($inputObjects);
		}
"""

            for (a in 1..arity)
                +"""
		public void mutate${inplaceMatchNumber(a)}(final Hints hints) {
			inplace${inplaceMatchNumber(a)}(hints).mutate($inputObjects);
		}
"""

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
	public final class Arity${arity}_IV_OV$generics {
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

		public Computers.Arity$arity$generics computer() {
			return matchComputer(env, opName, $inputTypesFromArgs, type(out));
		}

		public void compute() {
			computer().compute($inputObjects, out);
		}

		public Computers.Arity$arity$generics computer(final Hints hints) {
			return matchComputer(env, opName, $inputTypesFromArgs, type(out), hints);
		}

		public void compute(final Hints hints) {
			computer(hints).compute($inputObjects, out);
		}

	}"""
        }
        +"""
"""
        forEachArity(0..maxArity) {
            +"""
	@SuppressWarnings({ "unchecked" })
	public static $generics $functionArity$generics matchFunction(final OpEnvironment env, final String opName, $inputTypesArgsWithOutput)
	{
		return matchFunctionHelper(env, opName, $functionArity.class, outType${if (arity == 0) "" else ", "}$inputTypes);
	}
"""
        }
        forEachArity(0..maxArity) {
            +"""
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
            +"""
	@SuppressWarnings("unchecked")
	public static $generics $computerArity$generics matchComputer(final OpEnvironment env, final String opName, $inputTypesArgsWithOutput)
	{
		return matchComputerHelper(env, opName, $computerArity.class, outType${if (arity == 0) "" else ", "}$inputTypes);
	}
"""
        }
        forEachArity(0..maxArity) {
            +"""
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
            for (a in 1..arity)
                +"""
	@SuppressWarnings({ "unchecked" })
	public static ${inplaceGenerics(a)} ${inplaceType(a)}${inplaceGenerics(a)} ${matchName(a)}(final OpEnvironment env, final String opName, ${matchParams(a)})
	{
		return matchInplaceHelper(env, opName, ${inplaceClass(a)}, ioType, new Nil[] {${basicParams(a)}});
	}
"""
        }
        +"""
"""
        forEachArity {
            for (a in 1..arity)
                +"""
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