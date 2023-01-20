package function

import Generator
import dontEdit

object Computers : Generator() {

    //	.include Globals.list
//    override val arities = 0..maxArity

//    	[Computers.java]
    val nilArgs: String
        get() {
    		var names = nilNames as ArrayList
    		names.add(0, names.last())
    		return names.dropLast(1).joinToString()
    	}

    val matchParams: String
        get() {
    		// contains "I1, I2, ..., IN, O"
    		val generics = genericParamTypes
    		// contains "in1, in2, ..., inN, out"
    		val names = nilNames
    		// constructing strings of the term "final Nil<I1> in1"
    		return (0..arity).joinToString{ "final Nil<${generics[it]}> ${names[it]}"}
    	}

    override fun generate() {

        +"""
$dontEdit

package org.scijava.function;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Container class for computer-style functional interfaces at various
 * <a href="https://en.wikipedia.org/wiki/Arity">arities</a>.
 * <p>
 * A computer has functional method {@code compute} with a number of arguments
 * corresponding to the arity, plus an additional argument for the preallocated
 * output to be populated by the computation.
 * </p>
 * <p>
 * Each computer interface implements a corresponding {@link Consumer}-style
 * interface (see {@link Consumers}) with arity+1; the consumer's {@code accept}
 * method simply delegates to {@code compute}. This pattern allows computer ops
 * to be used directly as consumers as needed.
 * </p>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class Computers {

	private Computers() {
		// NB: Prevent instantiation of utility class.
	}

	// -- Static Utility Methods -- //

	/**
	 * All known computer types and their arities. The entries are sorted by
	 * arity, i.e., the {@code i}-th entry has an arity of {@code i}. It might be
	 * nice to use a BiMap from e.g. Google Guava, but that would require a
	 * dependency on that component :(
	 */
	public static final HashMap<Integer, Class<?>> ALL_COMPUTERS;
	public static final HashMap<Class<?>, Integer> ALL_ARITIES;

	static {
		ALL_COMPUTERS = new HashMap<>();
		ALL_ARITIES = new HashMap<>();"""
        forEachArity {
            +"""
		ALL_COMPUTERS.put($arity, Computers.Arity$arity.class);
		ALL_ARITIES.put(Computers.Arity$arity.class, $arity);"""
        }
        +"""
	}

	/**
	 * @return {@code true} if the given type is a known
	 *         computer type, {@code false} otherwise. <br>
	 *         Note that only the type itself and not its type hierarchy is
	 *         considered.
	 * @throws NullPointerException If {@code c} is {@code null}.
	 */
	public static boolean isComputer(Class<?> c) {
		return ALL_COMPUTERS.containsValue(c);
	}

	/**
	 * @param arity an {@code int} corresponding to a {@code Computer} of that
	 *          arity.
	 * @return the {@code Computer} of arity {@code arity}.
	 * @throws IllegalArgumentException iff there is no known {@code Computer} of
	 *           arity {@code arity}.
	 */
	public static Class<?> computerOfArity(int arity) {
		if (ALL_COMPUTERS.containsKey(arity)) return ALL_COMPUTERS.get(arity);
		throw new IllegalArgumentException("No Computer of arity " + arity);
	}

	/**
	 * @param c the {@link Class} of unknown arity
	 * @return the arity of {@code c}, or {@code -1} if {@code c} is <b>not</b> a
	 *         {@code Computer}.
	 */
	public static int arityOf(Class<?> c) {
		return ALL_ARITIES.getOrDefault(c, -1);
	}
"""
        forEachArity {
            val rawClass = "Arity$arity"
            val genericParams = generics
            val gClass = "$rawClass$genericParams"
            +"""

	@FunctionalInterface
	public interface $gClass extends
		$consumerArity$genericParams
	{

		void compute($computeParams);

		@Override
		default void accept($acceptParams)
		{
			compute($computeArgs);
		}
	}"""
        }
        +"""
}
"""
    }
}