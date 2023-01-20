package function

import Generator
import dontEdit
import joinToStringComma

object Functions : Generator() {

    //    .include Globals.list
    override val arities = 3..maxArity

    //    [Functions.java]
    val genericsO2 get() = '<' + (1..arity).joinToStringComma { "I$it" } + "O2>"

    val matchParams: String
        get() {
            val generics = genericParamTypes
            val names = genericsNamesList
            return (0..arity).joinToString { "final Nil<${generics[it]}> ${names[it]}Type" }
        }

    val nilArgs: String
        get() {
            val names = genericsNamesList as ArrayList
            names.add(0, names.last())
            names.dropLast(1)
            return names.joinToString { "${it}Type" }
        }

    val objectGenerics get() = '<' + (1..arity).joinToStringComma { "Object" } + "O>"

    val insArgs get() = (0 until arity).joinToString { "ins[$it]" }

    val javadocLink
        get() = when (arity) {
            0 -> "org.scijava.function.Producer"
            1 -> "java.util.function.Function"
            2 -> "java.util.function.BiFunction"
            else -> "Arity$arity"
        }

    val wildcardGenerics get() = '<' + (1..arity).joinToStringComma { "?" } + "O>"

    val fMethod get() = if (arity == 0) "create" else "apply"

    override fun generate() {
        val pound = '#'
        +"""
$dontEdit

package org.scijava.function;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Container class for
 * higher-<a href="https://en.wikipedia.org/wiki/Arity">arity</a>
 * {@link Function}-style functional interfaces&mdash;i.e. with functional
 * method {@code apply} with a number of arguments corresponding to the arity.
 * <ul>
 * <li>For 0-arity (nullary) functions, use {@link Producer} (and notice the
 * functional method there is named {@link Producer${pound}create()}).</li>
 * <li>For 1-arity (unary) functions, use {@link Function}.</li>
 * <li>For 2-arity (binary) functions, use {@link BiFunction}.</li>
 * </ul>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class Functions {

	private Functions() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * All known function types and their arities. The entries are sorted by
	 * arity, i.e., the {@code i}-th entry has an arity of {@code i}. It might be
	 * nice to use a BiMap from e.g. Google Guava, but that would require a
	 * dependency on that component :(
	 */
	public static final HashMap<Integer, Class<?>> ALL_FUNCTIONS;
	public static final HashMap<Class<?>, Integer> ALL_ARITIES;

	static {
		ALL_FUNCTIONS = new HashMap<>(10);
		ALL_ARITIES = new HashMap<>(10);"""
        forEachArity(0..maxArity) {
            +"""
		ALL_FUNCTIONS.put($arity, $functionArity.class);
		ALL_ARITIES.put($functionArity.class, $arity);"""
        }
        +"""
	}

	/**
	 * @return {@code true} if the given type is a known
	 *         function type, {@code false} otherwise.<br>
	 *         Note that only the type itself and not its type hierarchy is
	 *         considered.
	 * @throws NullPointerException If {@code c} is {@code null}.
	 */
	public static boolean isFunction(Class<?> c) {
		try {
			Class<?> superType = superType(c);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static Class<?> superType(Class<?> c) {
		if (ALL_FUNCTIONS.containsValue(c)) return c;
		for(Class<?> func : ALL_ARITIES.keySet()) {
			if (func.isAssignableFrom(c)) return func;
		}
		throw new IllegalArgumentException(c + " is not a subclass of any known Functions!");
	}

	/**
	 * @param arity an {@code int} corresponding to a {@code Function} of that
	 *          arity.
	 * @return the {@code Function} of arity {@code arity}.
	 * @throws IllegalArgumentException iff there is no known {@link Function} of
	 *           arity {@code arity}.
	 */
	public static Class<?> functionOfArity(int arity) {
		if (ALL_FUNCTIONS.containsKey(arity)) return ALL_FUNCTIONS.get(arity);
		throw new IllegalArgumentException("No Function of arity " + arity);
	}

	/**
	 * @param c the {@link Class} of unknown arity
	 * @return the arity of {@code c}, or {@code -1} if {@code c} is <b>not</b> a
	 *         {@code Function}.
	 */
	public static int arityOf(Class<?> c) {
		return ALL_ARITIES.getOrDefault(c, -1);
	}
"""
        forEachArity {
            +"""
	/**
	 * A $arity-arity specialization of {@link Function}.
	 *"""
            for(a in 1..arity)
                +"""
	 * @param <I$a> the type of argument $a to the function"""

            +"""
	 * @param <O> the type of the output of the function
	 * @see Function
	 */
	@FunctionalInterface
	public interface Arity$arity$generics {

		/**
		 * Applies this function to the given arguments.
		 *"""
            for(a in 1..arity)
                +"""
		 * @param in$a function argument $a"""

            +"""
		 * @return the function output
		 */
		O apply($applyParams);

		/**
		 * Returns a composed function that first applies this function to its
		 * input, and then applies the {@code after} function to the result. If
		 * evaluation of either function throws an exception, it is relayed to the
		 * caller of the composed function.
		 *
		 * @param <O2> the type of output of the {@code after} function, and of the
		 *          composed function
		 * @param after the function to apply after this function is applied
		 * @return a composed function that first applies this function and then
		 *         applies the {@code after} function
		 * @throws NullPointerException if after is null
		 */
		default <O2> Arity$arity$genericsO2 andThen(Function<? super O, ? extends O2> after)
		{
			Objects.requireNonNull(after);
			return ($applyParams) -> after.apply(apply($applyArgs));
		}
	}
"""
        }
        +"""
	public interface ArityN<O> {

		O apply(Object... ins);

		Object getOp();
	}

	/**
	 * Converts a {@link org.scijava.function.Producer} to a {@link ArityN}.
	 * 
	 * @param <O> the type variable linked the output of the {@link org.scijava.function.Producer}
	 * @param f the {@link org.scijava.function.Producer} to be converted into a {@link ArityN}
	 * @return a {@link ArityN} created from the {@link org.scijava.function.Producer}
	 */
	public static <O> Functions.ArityN<O> nary(Producer<O> f) {

		return new ArityN<>() {

			@Override
			public O apply(Object... ins) {
				return f.create();
			}

			@Override
			public Producer<O> getOp() {
				return f;
			}
		};
	}"""
        forEachArity(1..maxArity) {
            +"""

	/**
	 * Converts a {@link $javadocLink} to a {@link ArityN}.
	 * 
	 * @param <O> the type variable linked the output of the {@link $javadocLink}
	 * @param f the {@link $javadocLink} to be converted into a {@link ArityN}
	 * @return a {@link ArityN} created from the {@link $javadocLink}
	 */
	public static <O> Functions.ArityN<O> nary($functionArity$wildcardGenerics f) {

		// NB f must be cast to accept a set of input Objects for apply
		@SuppressWarnings("unchecked")
		$functionArity$objectGenerics func =
			($functionArity$objectGenerics) f;

		return new ArityN<>() {

			@Override
			public O apply(Object... ins) {
				return func.$fMethod($insArgs);
			}

			@Override
			public $functionArity$wildcardGenerics getOp() {
				return f;
			}
		};
	}"""
        }
        +"""

}
"""
    }
}