package opsEngine.main.lift

import Generator
import license
import dontEdit

object FunctionToIterables : Generator() {

    //.include templates/main/java/org/scijava/ops/engine/util/Globals.list
    override val arities get() = 1..maxArity

    //[FunctionToIterables.java]

    val iterableGenerics get() = '<' + genericParamTypes.joinToString { "Iterable<$it>" } + '>'

    val classGenerics get() = "<I, " + genericParamTypes.joinToString() + '>'

    val iteratorsHaveNext get() = (1..arity).joinToString(" && ") { "itr$it.hasNext()" }

    val iteratorsNext get() = (1..arity).joinToString { "itr$it.next()" }

    val iteratorInputs get() = (1..arity).joinToString { "in$it" }

    val funcItrsNext: String
        get() {
            val gpt = genericParamTypes
            return (0 until arity).joinToString { "(${gpt[it]}) itrs[$it].next()" }
        }

    override fun generate() {
        arity = maxArity
        +"""
$license

$dontEdit

package org.scijava.ops.engine.adapt.lift;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.function.Functions;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpCollection;

/**
 * Converts {@link Functions} operating on single types to {@link Functions}
 * that operate on {@link Iterable}s of types. N.B. it is the user's
 * responsibility to pass {@link Iterable}s of the same length (otherwise the Op
 * will stop when one of the {@link Iterable}s runs out of {@link Object}s).
 * 
 * @author Gabriel Selzer
 */
public class FunctionToIterables$classGenerics implements OpCollection {

	// NOTE: we cannot convert Producers since there is no way to determine the
	// length of the output Iterable
"""
        forEachArity {
            +"""
	@SuppressWarnings("unchecked")
	@OpField(names = "adapt")
	public final Function<$functionArity$generics, $functionArity$iterableGenerics> liftFunction$arity =
		(function) -> {
			return ($iteratorInputs) -> lazyIterable(itrs -> function.apply($funcItrsNext), $iteratorInputs);
		};
"""
        }
        +"""
	/**
	 * Lazily zips N {@link Iterable}s into one {@link Iterable} through the use
	 * of {@code nexter}
	 * 
	 * @param <E> - the output type of the function wrapped by {@code nexter}
	 * @param nexter a {@link Function} that wraps some other N-arity
	 *          {@link Function}. It is assumed that the arity of the wrapped
	 *          {@link Function} is equal to the length of {@code iterables}.
	 *          {@code nexter} can, given a list of {@link Iterator}s (these will
	 *          be iterators on {@code iterables}), call the wrapped function and
	 *          return the output.
	 * @param iterables the list of {@link Iterable}s that wil sequentially be
	 *          iterated over usignd {@code nexter}
	 * @return {@link Iterable} generated lazily using {@code nexter}.
	 */
	static <E> Iterable<E> lazyIterable(final Function<Iterator<?>[], E> nexter,
		final Iterable<?>... iterables)
	{
		return new Iterable<>() {

			@Override
			public Iterator<E> iterator() {
				return new Iterator<>() {

					private Iterator<?>[] iterators;
					{
						iterators = new Iterator<?>[iterables.length];
						for (int i = 0; i < iterables.length; i++)
							iterators[i] = iterables[i].iterator();
					}

					@Override
					public boolean hasNext() {
						for (Iterator<?> itr : iterators)
							if (!itr.hasNext()) return false;
						return true;
					}

					@Override
					public E next() {
						return nexter.apply(iterators);
					}

				};
			}
		};
	}
}
"""
    }
}