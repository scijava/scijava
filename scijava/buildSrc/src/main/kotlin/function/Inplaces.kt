package function

import Generator
import dontEdit

object Inplaces : Generator() {

    //.include Globals.list
    override val arities = 1..maxArity

    //[Inplaces.java]
    fun simplifiedClass(num: Int) = simplifiedInplace(num) + ".class"

    fun inplaceClass(num: Int) = inplaceType(num) + ".class"

    fun typeVarNums(io: Int) = (1..arity).take(io - 1) + 'O' + (1..arity).drop(io)

    fun genericsList(io: Int) = typeVarNums(io).map { "I$it" }

    fun generics(io: Int) = '<' + genericsList(io).joinToString() + '>'

    val allMutableGenericsList get() = (1..arity).map { "IO$it" }

    val allMutableGenerics get() = '<' + allMutableGenericsList.joinToString() + '>'

    fun basicParamsList(io: Int) = typeVarNums(io).map { if (it == 'O') "ioType" else "in${it}Type" }

    fun basicParams(io: Int) = basicParamsList(io).joinToString()

    fun matchName(num: Int) = if (arity == 1) "match" else "match$num"

    fun matchParams(io: Int) = genericsList(io).map {
        val arg = if (it[1] == 'O') it.lowercase() else "in" + it.substring(1)
        "final Nil<$it> ${arg}Type"
    }.joinToString()

    fun typeArgs(io: Int) = basicParamsList(io).joinToString { "$it.getType()" }

    val allMutableMutateParams get() = allMutableGenericsList.joinToString { "@Mutable $it ${it.lowercase()}" }

    val allMutableMutateArgs get() = allMutableGenericsList.joinToString { "${it.lowercase()}" }

    fun mutateArgs(io: Int) = genericsList(io).joinToString {
        if (it.substring(1) == "O")
            "@Mutable IO io"
        else
            "$it in${it.substring(1)}"
    }

    override fun generate() {

        +"""
$dontEdit

package org.scijava.function;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Container class for inplace-style functional interfaces at various
 * <a href="https://en.wikipedia.org/wiki/Arity">arities</a>.
 * <p>
 * An inplace has functional method {@code mutate} with a number of arguments
 * corresponding to the arity. Any of the arguments annotated
 * with @{@link Mutable} may be mutated during execution. Some interfaces narrow
 * this behavior to only a specific argument; most ops in practice will
 * implement one of these narrowed interfaces. For example,
 * {@link Inplaces.Arity2_1} is a binary inplace op that mutates the first of
 * two arguments&mdash;e.g., an {@code a /= b} division operation would be an
 * {@link Inplaces.Arity2_1}, whereas {@code b = a / b} would be an
 * {@link Inplaces.Arity2_2}.
 * </p>
 * <p>
 * Each inplace interface implements a corresponding {@link Consumer}-style
 * interface (see {@link Consumers}) with same arity; the consumer's
 * {@code accept} method simply delegates to {@code mutate}. This pattern allows
 * inplace ops to be used directly as consumers as needed.
 * </p>
 * <p>
 * Note that there is no nullary (arity 0) inplace interface, because there
 * would be no arguments to mutate; see also {@link Consumers.Arity0},
 * {@link Computers.Arity0} and {@link Producer}.
 * </p>
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public final class Inplaces {

	private Inplaces() {
		// NB: Prevent instantiation of container class.
	}

	/**
	 * All known inplace types and their arities and mutable positions. The
	 * entries are sorted by arity and mutable position.
	 */
	public static final Map<InplaceInfo, Class<?>> ALL_INPLACES;
	public static final Map<Class<?>, InplaceInfo> ALL_ARITIES;

	static {
		ALL_INPLACES = new HashMap<>();
		ALL_ARITIES = new HashMap<>();"""
        forEachArity {
            for (a in 1..arity) {
                val inplaceIndex = a - 1
                +"""
        InplaceInfo info_${arity}_$a = new InplaceInfo($arity, $inplaceIndex);
		ALL_INPLACES.put(info_${arity}_$a, ${inplaceClass(a)});
		ALL_ARITIES.put(${inplaceClass(a)}, info_${arity}_$a);"""
            }
        }
        +"""
	}

	/**
	 * @return {@code true} if the given type is a known
	 *         inplace type, {@code false} otherwise. <br>
	 *         Note that only the type itself and not its type hierarchy is
	 *         considered.
	 * @throws NullPointerException If {@code c} is {@code null}.
	 */
	public static boolean isInplace(Class<?> c) {
		return ALL_ARITIES.containsKey(c);
	}

	public static List<Class<?>> getInplacesOfArity(final int arity) {
		return ALL_INPLACES.entrySet().stream() //
			.filter(e -> e.getKey().arity() == arity) //
			.map(Entry::getValue) //
			.collect(Collectors.toList());
	}

	/**
	 * @param arity an {@code int} corresponding to a {@code Inplace} of that
	 *          arity.
	 * @param mutableIndex an {@code int} corresponding to a the mutable index of d
	 *          arity.
	 * @return the {@code Inplace} of arity {@code arity}.
	 * @throws IllegalArgumentException iff there is no known {@code Inplace} of
	 *           arity {@code arity}.
	 */
	public static Class<?> inplaceOfArity(int arity, int mutableIndex) {
		InplaceInfo info = new InplaceInfo(arity, mutableIndex);
		if (ALL_INPLACES.containsKey(info)) return ALL_INPLACES.get(info);
		throw new IllegalArgumentException("No Inplace of arity " + arity +
			" and mutable index " + mutableIndex);
	}

	/**
	 * @param c the {@link Class} of unknown arity
	 * @return the arity of {@code c}, or {@code -1} if {@code c} is <b>not</b> a
	 *         {@code Inplace}.
	 */
	public static InplaceInfo arityOf(Class<?> c) {
		return ALL_ARITIES.getOrDefault(c, new InplaceInfo(-1, -1));
	}

	@FunctionalInterface
	public interface Arity1<IO> extends Consumer<IO> {

		void mutate(@Mutable IO io);

		@Override
		default void accept(IO io) {
			mutate(io);
		}
	}
"""
        forEachArity(2..maxArity) {
            arity--
            val consumerArity = consumerArity
            arity++
            +"""
	@FunctionalInterface
	public interface Arity$arity$allMutableGenerics extends $consumerArity$allMutableGenerics
	{

		void mutate($allMutableMutateParams);

		@Override
		default void accept($allMutableMutateParams)
		{
			mutate($allMutableMutateArgs);
		}
	}
"""
            for (a in 1..arity)
                +"""
	@FunctionalInterface
	public interface ${simplifiedInplace(a)}${generics(a)} extends Arity$arity${generics(a)}
	{

		@Override
		void mutate(${mutateArgs(a)});
	}
"""
        }
        +"""
	public static class InplaceInfo {

		private final int arity;
		private final int mutablePosition;

		public InplaceInfo(final int arity, final int mutablePosition) {
			this.arity = arity;
			this.mutablePosition = mutablePosition;
		}

		public int arity() {
			return arity;
		}

		public int mutablePosition() {
			return mutablePosition;
		}
	}
}
"""
    }
}