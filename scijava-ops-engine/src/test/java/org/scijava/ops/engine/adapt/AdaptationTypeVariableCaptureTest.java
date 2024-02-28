
package org.scijava.ops.engine.adapt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.*;
import org.scijava.priority.Priority;
import org.scijava.types.Any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * When the user requests an {@link Any} as the output, we should ensure that an
 * output is used that can satisfy the typing constraints of the underlying Op.
 * In other words, ensure that when an Op is matched for adaptation, we test
 * that its type variable assignments are captured and used for dependency
 * matching.
 *
 * @param <N>
 * @author Gabriel Selzer
 */
public class AdaptationTypeVariableCaptureTest<N extends Number> extends
	AbstractTestEnvironment implements OpCollection
{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new Computer1ToFunction1ViaFunction<>());
		ops.register(new AdaptationTypeVariableCaptureTest<>());
	}

	/** Adaptor */
	@OpClass(names = "engine.adapt")
	public static class Computer1ToFunction1ViaFunction<I, O> implements
		Function<Computers.Arity1<I, O>, Function<I, O>>, Op
	{

		@OpDependency(name = "engine.create", adaptable = false)
		Function<I, O> creator;

		/**
		 * @param computer the Computer to adapt
		 * @return computer, adapted into a Function
		 */
		@Override
		public Function<I, O> apply(Computers.Arity1<I, O> computer) {
			return (in) -> {
				O out = creator.apply(in);
				computer.compute(in, out);
				return out;
			};
		}

	}

	/** Op that should be adapted */
	@OpField(names = "test.adaptedCapture")
	public final Computers.Arity1<List<N>, List<Double>> original = (in, out) -> {
		out.clear();
		for (var n : in) {
			out.add(n.doubleValue());
		}
	};

	/** Op that will match if type variables aren't captured */
	@OpField(names = "engine.create", priority = Priority.HIGH)
	public final Function<List<Byte>, List<Byte>> highCopier = (list) -> {
		throw new IllegalStateException("This Op should not be called");
	};

	/** Op that should match if type variables are captured */
	@OpField(names = "engine.create", priority = Priority.LOW)
	public final Function<List<Byte>, List<Double>> lowCopier = (
		list) -> new ArrayList<>();

	@Test
	public void testCapture() {
		List<Byte> in = Arrays.asList((byte) 0, (byte) 1);
		Object out = ops.op("test.adaptedCapture") //
			.arity1() //
			.input(in) //
			.apply();
		Assertions.assertInstanceOf(List.class, out);
		Assertions.assertEquals(2, in.size());
	}

}
