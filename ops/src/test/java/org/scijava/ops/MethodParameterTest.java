
package org.scijava.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import net.imglib2.type.numeric.RealType;
import org.junit.Test;
import org.scijava.param.Parameter;
import org.scijava.param.ValidityException;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;

/**
 * Playing with method parameters.
 *
 * @author Curtis Rueden
 */
public class MethodParameterTest<I extends RealType<I>, O extends RealType<O>, T extends RealType<T>> {

	// Calling a static method via reflection is slow.
	// Wrapping a static method as a lambda is potentially faster.
	// Static methods in imglib2-algorithm can be lambdaed using :: syntax.
	// May want to make TriFunction, Function4, Function5, etc. ifaces.
	// Can we write new ops as lambdas?

	//@SomeAnnotationHere?
	public BiFunction<T, T, T> adder = (x, y) -> {
		final T result = x.copy();
		result.add(y);
		return result;
	};

	// The above is reasonably concise, but does not implement e.g. MathAddOp.
	// What if we get rid of the need to type it that way? Why do we need it?
	// The string name of the op should be enough, no?
	// There is no benefit to the MathAddOp interface; we don't use it.
	// Will be simpler, and less convoluted and confusing, without it.

	// To get type safety back, we could do:
	// ops.op(MathOps.ADD, new Nil<...>() {});

	// Another challenge is: how can/do we annotate the parameters?
	// It's fine if everything can be inferred with defaults, but in some
	// cases the programmer will want to define some of the attributes.

	//@SomeAnnotationHere?
	@Parameter(key = "y", description = "The Y thing. It has some properties.")
	@Parameter(key = "x")
	@Parameter(key = "result", type = ItemIO.BOTH)
	public BiComputerOp<T, T, T> adderComputer = (x, y, result) -> {
		result.set(x);
		result.add(y);
	};

	// Above is a computer. Also concise. Does it make sense to reuse @Parameter?

	// Also: how do we discover this? The OpEnvironment will need its own
	// op cache. Its currency needs to be Structs. And we need new logic for
	// structifying the above field. When a new instance is requested,
	// it could call clone() on the existing instance (?), rather than
	// using the no-args constructor.

	// And what about static methods from e.g. imglib2-algorithm?
	// Here comes a method. Let's pretend it resides in a different class
	// somewhere we don't control, but we want it as an op:

	public static <V extends RealType<V>> V myAwesomeAdderFunction(
		final V x,
		final V y)
	{
		final V result = x.copy();
		result.add(y);
		return result;
	}

	// The above method is not a functional interface, but can become one:

	//@SomeAnnotationHere?
	public BiFunction<T, T, T> adderFromMethod =
		MethodParameterTest::myAwesomeAdderFunction;

	// What about extra/secondary parameters? Can we do that, too?
	// Yes, but the syntax becomes much less concise:

	//@SomeAnnotationHere?
	public BiComputerOp<T, T, T> addAndScale = new BiComputerOp<T, T, T>() {

		@Parameter
		private T scale;

		@Override
		public void accept(
			final @Parameter(key = "x") T x,
			final @Parameter(key = "y") T y,
			final @Parameter(key = "result") T result)
		{
			result.set(x);
			result.add(y);
			result.mul(scale);
		}
	};

	// At that point, it is just another class. May as well define it
	// explicitly as such using "public static class" instead of as a field.
	// Same number of lines of code!

	@Test
	public void testStuff() throws Exception {
		BiFunction<String, String, String> stringFunc = UsefulMethods::concat;
		System.out.println(stringFunc.apply("x", "y"));

		BiFunction<Integer, Integer, Integer> intFunc = UsefulMethods::concat;
		System.out.println(intFunc.getClass());
		Stream.of(intFunc.getClass().getDeclaredMethods()).forEach(System.out::println);
		System.out.println(intFunc.getClass().getSuperclass());
		Stream.of(intFunc.getClass().getInterfaces()).forEach(System.out::println);
		System.out.println(intFunc.apply(5, 6));

		@Parameter
		final Function<String, String> doubler = v -> v + v;

		final Struct p = //
			MethodStructs.function2(UsefulMethods::concat);
		final List<Member<?>> items = p.members();
		assertParam("a", int.class, ItemIO.INPUT, items.get(0));
		assertParam("b", Double.class, ItemIO.INPUT, items.get(1));
		assertParam("c", byte.class, ItemIO.INPUT, items.get(2));
		assertParam("d", Object.class, ItemIO.INPUT, items.get(3));
		assertParam("o", double.class, ItemIO.OUTPUT, items.get(4));
		assertParam("p", String.class, ItemIO.OUTPUT, items.get(5));
	}

	// -- Helper methods --

	private void assertParam(final String key, final Type type,
		final ItemIO ioType, final Member<?> pMember)
	{
		assertEquals(key, pMember.getKey());
		assertEquals(type, pMember.getType());
		assertSame(ioType, pMember.getIOType());
	}

	// -- Helper classes --

	public static class UsefulMethods {

		public static String concat(final String s, final String t) {
			return "String:" + s + t;
		}

		public static Integer concat(final Integer s, final Integer t) {
			return s + t + 1;
		}

		public static int concat(final int s, final int t) {
			return s + t;
		}

		public static String concat(final int a, final Double b, final byte c, final Object d) {
			return "" + a + b + c + d;
		}
	}
}
