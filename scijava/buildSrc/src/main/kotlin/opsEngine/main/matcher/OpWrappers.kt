package opsEngine.main.matcher

import Generator
import dontEdit

object OpWrappers : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    //    arities = (0..maxArity).collect()

    //[OpWrappers.java]
    fun inplaceTypeVarNums(io: Int) = (1..arity).take(io - 1) + 'O' + (1..arity).drop(io)

    fun inplaceGenericsList(io: Int) = inplaceTypeVarNums(io).map { "I$it" }

    fun inplaceTypeParams(io: Int) = '<' + inplaceGenericsList(io).joinToString() + '>'

    val nilArgs get() = nilNames.joinToString()

    val genericFunctionGenerics
        get() = when (arity) {
            0 -> "GenericTypedProducer<O>"
            else -> "GenericTypedFunction$arity$generics"
        }

    val genericComputerGenerics get() = "GenericTypedComputer${arity}<" + genericParamTypes.joinToString() + '>'

    fun genericInplaceGenerics(io: Int) = "GenericTypedInplace${arity}<" + inplaceGenericsList(io).joinToString() + '>'

    val functionGenerics get() = functionArity + '<' + genericParamTypes.joinToString() + '>'

    val computerGenerics get() = computerArity + '<' + genericParamTypes.joinToString() + '>'

    fun inplaceGenerics(io: Int) = inplaceType(io) + '<' + inplaceGenericsList(io).joinToString() + '>'

    val matchParams: String
        get() {
            // contains "I1, I2, ..., IN, O"
            val gpt = genericParamTypes
            // contains "in1, in2, ..., inN, out"
            val names = nilNames
            // constructing strings of the term "final Nil<I1> in1"
            return (0..arity).joinToString { "final Nil<${gpt[it]}> ${names[it]}" }
        }

    fun mutateArgsList(io: Int) = inplaceTypeVarNums(io).map { if (it == 'O') "ioType" else "in${it}Type" }

    fun mutateParams(io: Int): String {
        val args = mutateArgsList(io)
        val types = inplaceGenericsList(io)
        return (0 until arity).joinToString { types[it] + " " + args[it] }
    }

    fun mutateArgs(io: Int) = mutateArgsList(io).joinToString()

    override fun generate() {
        +"""
$dontEdit

package org.scijava.ops.engine.matcher.impl;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.function.Producer;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInstance;
import org.scijava.ops.engine.OpWrapper;
import org.scijava.ops.api.RichOp;

public class OpWrappers {

	// -- producer --

	public static class ProducerOpWrapper<T> implements OpWrapper<Producer<T>> {

		@Override
		public RichOp<Producer<T>> wrap( //
            final OpInstance<Producer<T>> instance, //
            final OpEnvironment env, //
            final Hints hints)
		{
			class GenericTypedProducer //
				extends AbstractRichOp<Producer<T>> //
				implements Producer<T>
			{

				public GenericTypedProducer()
				{
					super(instance, env, hints);
				}

				@Override
				public T create() {
					preprocess();

					// Call the op
					T out = instance.op().create();

					postprocess(out);

					return out;
				}

				@Override
				public Producer<T> asOpType() {
					return this;
				}

			}
			return new GenericTypedProducer();
		}
	}

	// -- functions --
"""
        forEachArity(1..maxArity) {
            val fg = functionGenerics
            +"""
	public static class Function${arity}OpWrapper$generics //
		implements //
		OpWrapper<$fg>
	{

		@Override
		public RichOp<$fg> wrap( //
			final OpInstance<$fg> instance, //
			final OpEnvironment env, //
			final Hints hints)
		{
			class GenericTypedFunction$arity //
				extends AbstractRichOp<$fg> //
				implements $fg 
			{

				public GenericTypedFunction$arity()
				{
					super(instance, env, hints);
				}

				@Override
				public O apply($applyParams) //
				{
					preprocess($applyArgs);

					// Call the op
					O out = instance.op().apply($applyArgs);

					postprocess(out);
					return out;
				}

				@Override
				public $fg asOpType() {
					return this;
				}

			}
			return new GenericTypedFunction$arity();
		}
	}
"""
        }
        +"""
	// -- computers --
"""
        forEachArity(0..maxArity) {
            val cg = computerGenerics
            +"""
	public static class Computer${arity}OpWrapper$generics //
		implements //
		OpWrapper<$cg>
	{

		@Override
		public RichOp<$cg> wrap( //
			final OpInstance<$cg> instance, //
			final OpEnvironment env, //
			final Hints hints)
		{
			class GenericTypedComputer$arity //
				extends AbstractRichOp<$cg> //
				implements $cg 
			{
				public GenericTypedComputer$arity()
				{
					super(instance, env, hints);
				}

				@Override
				public void compute($computeParams) //
				{
					preprocess($computeArgs);

					// Call the op
					instance.op().compute($computeArgs);

					postprocess(out);
				}

				@Override
				public $cg asOpType() {
					return this;
				}

			}
			return new GenericTypedComputer$arity();
		}
	}
"""
        }
        +"""
	// -- inplaces --
"""
        forEachArity(1..maxArity) {
            for(a in 1..arity) {
                val ig = inplaceGenerics(a)
                +"""
	public static class Inplace${inplaceSuffix(a)}OpWrapper${inplaceTypeParams(a)} //
		implements //
		OpWrapper<$ig> //
	{

		@Override
		public RichOp<$ig> wrap( //
			final OpInstance<$ig> instance, //
			final OpEnvironment env, //
			final Hints hints)
		{
			class GenericTypedInplace${inplaceSuffix(a)} //
				extends AbstractRichOp<$ig> //
				implements $ig 
			{
				public GenericTypedInplace${inplaceSuffix(a)}()
				{
					super(instance, env, hints);
				}

				@Override
				public void mutate(${mutateParams(a)}) //
				{
					preprocess(${mutateArgs(a)});

					// Call the op
					instance.op().mutate(${mutateArgs(a)});

					// Log a new execution
					postprocess(ioType);
				}

				@Override
				public $ig asOpType() {
					return this;
				}

			}
			return new GenericTypedInplace${inplaceSuffix(a)}();
		}
	}
"""
            }
        }
        +"""        


}
"""
    }
}