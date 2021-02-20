package org.scijava.ops.matcher;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.Priority;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Producer;
import org.scijava.param.Mutable;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;
import org.scijava.types.Any;
import org.scijava.types.TypeExtractor;

/**
 * Tests op matcher functionality with {@link Any} types.
 * 
 * @author Gabriel Selzer
 */
public class MatchingWithAnyTest extends AbstractTestEnvironment {

	@Test
	public void testAny() {

		NestedThing<String, Thing<String>> nthing = new NestedThing<>();
		Double e = ops.op("test.nestedAny").input(nthing).outType(Double.class).apply();

		Thing<Double> thing = new Thing<>();
		Double d = ops.op("test.any").input(thing).outType(Double.class).apply();

		assert d == 5.;
		assert e == 5.;

	}

	/**
	 * NOTE: this is where ops.run() and the Any paradigm fail. However, this can
	 * easily be avoided by making TypeExtractors for any class for which this kind
	 * of exception can happen.
	 */
	@Test(expected = ClassCastException.class)
	public void testExceptionalThing() {

		ExceptionalThing<Double> ething = new ExceptionalThing<>(0.5);
		Double d = ops.op("test.exceptionalAny").input(ething).outType(Double.class).apply();

	}

	@Plugin(type = TypeExtractor.class, priority = Priority.LOW)
	public static class ThingTypeExtractor implements TypeExtractor<Thing<?>> {

		@Override
		public Type reify(final Thing<?> o, final int n) {
			if (n != 0)
				throw new IndexOutOfBoundsException();

			return new Any();
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Class<Thing<?>> getRawType() {
			return (Class) Thing.class;
		}

	}

	// TODO: Note that this wouldn't work for Computer -> Function because here
	// LiftFunctionToArrayTransformer is the first transformer which is asked for
	// source refs. This transformer doesn't support Any and would fail.
	// TODO: can we remove this test?
	@Test
	public void testRunAnyFunction1FromComputer2() {
		final int in1 = 11;
		final long in2 = 31;
		final MutableNotAny out = ops.op("test.integerAndLongAndNotAnyComputer").input(in1, in2).outType(MutableNotAny.class).apply();
		assertEquals(Long.toString(in1 + in2), out.getValue());
	}
	
}

@Plugin(type = Op.class, name = "test.functionAndLongToLong")
@Parameter(key = "input")
@Parameter(key = "op")
@Parameter(key = "output")
class FunctionAndLongToLong implements BiFunction<Function<Long, Long>, Long, Long> {

	@Override
	public Long apply(Function<Long, Long> t, Long u) {
		return t.apply(u);
	}
	
}

@Plugin(type = Op.class, name = "test.integerAndLongAndNotAnyComputer")
@Parameter(key = "input1")
@Parameter(key = "input2")
@Parameter(key = "output")
class IntegerAndLongAndNotAnyComputer implements Computers.Arity2<Integer, Long, MutableNotAny> {

	@Override
	public void compute(Integer in1, Long in2, @Mutable MutableNotAny out) {
		out.setValue(Long.toString(in1 + in2));
	}
}

class MutableNotAny {

	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

@Plugin(type = Op.class, name = "create, create.mutableNotAny")
@Parameter(key = "mutableNotAny")
class MutableNotAnyCreator implements Producer<MutableNotAny> {

	@Override
	public MutableNotAny create() {
		return new MutableNotAny();
	}
}

class Thing<U> {

	public double create(U u) {
		return 5.;
	}
}

class ExceptionalThing<U> {

	public ExceptionalThing(U u) {
		thing = u;
	};

	U thing;

	U getU() {
		return thing;
	}

	public double create(U u) {
		thing = u;
		return 5.;
	}
}

class NestedThing<U, V extends Thing<?>> {
	public double create(V u) {
		return 5.;
	}
}

@Plugin(type = Op.class, name = "test.any")
@Parameter(key = "thing")
@Parameter(key = "output")
class ThingFunction implements Function<Thing<String>, Double> {

	@Override
	public Double apply(Thing<String> t) {
		return t.create("Hello");
	}

}

@Plugin(type = Op.class, name = "test.exceptionalAny")
@Parameter(key = "thing")
@Parameter(key = "output")
class ExceptionalThingFunction implements Function<ExceptionalThing<String>, Double> {

	@Override
	public Double apply(ExceptionalThing<String> t) {
		String s = t.getU();
		return t.create("Hello");
	}

}

@Plugin(type = Op.class, name = "test.nestedAny")
@Parameter(key = "nestedThing")
@Parameter(key = "output")
class NestedThingFunction implements Function<NestedThing<String, Thing<String>>, Double> {

	@Override
	public Double apply(NestedThing<String, Thing<String>> t) {
		return 5.;
	}

}
