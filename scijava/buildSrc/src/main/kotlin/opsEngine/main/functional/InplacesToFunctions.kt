package opsEngine.main.functional

import Generator
import license
import dontEdit

object InplacesToFunctions : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities = 1..maxArity

    //[InplacesToFunctions.java]

    fun simplifiedClass(num: Int) = simplifiedInplace(num) + ".class"

    fun inplaceClass(num: Int) = inplaceType(num) + ".class"

    fun typeVarNums(io: Int) = (1..arity).take(io - 1) + 'O' + (1..arity).drop(io)

    fun genericsList(io: Int) = typeVarNums(io).map { "I$it" }

    fun generics(io: Int) = '<' + genericsList(io).joinToString() + '>'

    fun functionGenerics(io: Int) = '<' + genericsList(io).joinToString() + ", IO>"

    val allMutableGenericsList get() = (1..arity).map { "IO$it" }

    val allMutableGenerics get() = '<' + allMutableGenericsList.joinToString() + '>'

    fun basicParamsList(io: Int) = typeVarNums(io).map { if (it == 'O') "ioType" else "in${it}Type" }

    fun basicParams(io: Int) = basicParamsList(io).joinToString()

    fun matchName(num: Int) = if (arity == 1) "match" else "match$num"

    fun matchParams(io: Int) = genericsList(io).joinToString {
        val arg = if (it[1] == 'O') it.lowercase() else "in" + it.substring(1)
        "final Nil<$it> ${arg}Type"
    }

    fun typeArgs(io: Int) = basicParamsList(io).joinToString { "$it.getType()" }

    val allMutableMutateParams get() = allMutableGenericsList.joinToString { "@Mutable $it ${it.lowercase()}" }

    val allMutableMutateArgs get() = allMutableGenericsList.joinToString { it.lowercase() }

    fun mutateArgs(io: Int) = genericsList(io).joinToString { if (it.substring(1) == "O") "IO io" else "$it in${it.substring(1)}" }

    fun mutateParams(io: Int) = genericsList(io).joinToString { if (it.substring(1) == "O") "io" else "in${it.substring(1)}" }

    fun mutateTempParams(io: Int) = genericsList(io).joinToString { if (it.substring(1) == "O") "temp" else "in${it.substring(1)}" }

    override fun generate() {
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.functional;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
"""
        val classArity = maxArity + 1
        arity = classArity // no need to set it back, it will be overwritten by the very next `forEachArity`
        +"""
/**
 * Collection of adaptation Ops to convert {@link Inplaces} into
 * {@link Functions}.
 * 
 * @author Gabriel Selzer
 */
public class InplacesToFunctions${generics(classArity)} implements OpCollection {
"""
        forEachArity {
            for (a in 1..arity)
                +"""
	@OpClass(names = "adapt")
	public static class Inplace${inplaceSuffix(a)}ToFunction$arity${generics(a)} implements Function<${inplaceType(a)}${generics(a)}, $functionArity${functionGenerics(a)}>, Op {
		
		@OpDependency(name = "create", adaptable = false)
		private Function<IO, IO> createOp;
		@OpDependency(name = "copy", adaptable = false)
		private Computers.Arity1<IO, IO> copyOp;

		/**
		 * @param t the Inplace to adapt
		 * @return an adaptation of inplace
		 */
		@Override
		public $functionArity${functionGenerics(a)} apply(${inplaceType(a)}${generics(a)} t) {
			return (${mutateArgs(a)}) -> {
				IO temp = createOp.apply(io);
				copyOp.accept(io, temp);
				t.mutate(${mutateTempParams(a)});
				return temp;
			};
		}
	}
"""
        }
        +"""
}
"""
    }
}