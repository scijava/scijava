package function

import Generator
import dontEdit

object Consumers : Generator(){

    //    .include Globals.list
    override val arities = 3..numConsumers

    val genericsList get() = (1..arity).map { "I$it" }

    override val genericsNamesList get() = genericsList.map { "in${it.substring(1)}" }

    override val nilNames get() = genericsNamesList.map{ "${it}Type"}

override val typeParamsList: List<String> get()  {
        val generics = genericsList
        val names = genericsNamesList
        return (0 until arity).map{"${generics[it]} ${names[it]}"}
    }

    //    [Consumers.java]
    val objectString get() = (1..arity).joinToString { "Object" }

    override val generics get() = '<' + genericsList.joinToString() + '>'

    val superGenerics get() = '<' + genericsList.joinToString { "? super $it" } + '>'

    fun genericName(num: Int) = genericsNamesList[num - 1]

    fun generic(num: Int) = genericsList[num - 1]

    override fun generate() {

        +"""
$dontEdit

package org.scijava.function;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Container class for
 * higher-<a href="https://en.wikipedia.org/wiki/Arity">arity</a>
 * {@link Consumer}-style functional interfaces&mdash;i.e. with functional
 * method {@code accept} with a number of arguments corresponding to the arity.
 * <ul>
 * <li>For 1-arity (unary) consumers, use {@link Consumer}.</li>
 * <li>For 2-arity (binary) consumers, use {@link BiConsumer}.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class Consumers {

	private Consumers() {
		// NB: Prevent instantiation of container class.
	}

	/**
	 * Represents an operation that accepts no input arguments and returns no
	 * result. This is the zero-arity specialization of {@link Consumer}. Unlike
	 * most other functional interfaces, this interface is expected to operate via
	 * side-effects.
	 * <p>
	 * This is a functional interface whose functional method is
	 * {@link #accept()}.
	 * </p>
	 *
	 * @see Consumer
	 */
	@FunctionalInterface
	public interface Arity0 extends Runnable {

		/**
		 * Performs the operation.
		 */
		void accept();

		@Override
		default void run() {
			accept();
		}

		/**
		 * Returns a composed {@code Consumer.Arity0} that performs, in sequence,
		 * this operation followed by the {@code after} operation. If performing
		 * either operation throws an exception, it is relayed to the caller of the
		 * composed operation. If performing this operation throws an exception, the
		 * {@code after} operation will not be performed.
		 *
		 * @param after the operation to perform after this operation
		 * @return a composed {@code Consumer.Arity0} that performs in sequence this
		 *         operation followed by the {@code after} operation
		 * @throws NullPointerException if {@code after} is null
		 */
		default Arity0 andThen(Arity0 after) {
			Objects.requireNonNull(after);

			return () -> {
				accept();
				after.accept();
			};
		}
	}
"""
        forEachArity {
            +"""
	/**
	 * Represents an operation that accepts $arity input arguments and returns no
	 * result. This is the $arity-arity specialization of {@link Consumer}. Unlike most
	 * other functional interfaces, this interface is expected to operate via
	 * side-effects.
	 * <p>
	 * This is a functional interface whose functional method is
	 * {@link #accept($objectString)}.
	 * </p>
	 *"""

            for (a in 1..arity)
                +"""
	 * @param <${generic(a)}> the type of argument $a."""

            +"""
	 * @see Consumer
	 */
	 @FunctionalInterface
	 public interface Arity$arity$generics {

		/**
		 * Performs this operation on the given arguments.
		 *"""
            for (a in 1..arity)
                +"""
		 * @param ${genericName(a)} input argument $a."""

            +"""
		 */
		void accept($acceptParams);

		/**
		 * Returns a composed {@code Consumer.Arity$arity} that performs, in sequence,
		 * this operation followed by the {@code after} operation. If performing
		 * either operation throws an exception, it is relayed to the caller of the
		 * composed operation. If performing this operation throws an exception, the
		 * {@code after} operation will not be performed.
		 *
		 * @param after the operation to perform after this operation
		 * @return a composed {@code Consumer.Arity$arity} that performs in sequence this
		 *         operation followed by the {@code after} operation
		 * @throws NullPointerException if {@code after} is null
		 */
		 default Arity$arity$generics andThen(Arity$arity$superGenerics after)
		 {
		 	Objects.requireNonNull(after);

			return ($acceptArgs) -> {
				accept($acceptArgs);
				after.accept($acceptArgs);
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