package org.scijava.ops.types;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.core.Priority;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.core.Op;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

public class AnyTest extends AbstractTestEnvironment {

	@Test
	public void testAny() {

		NestedThing<String, Thing<String>> nthing = new NestedThing<>();
		Double e = (Double) ops().run("test.nestedAny", nthing);

		Thing<Double> thing = new Thing<>();
		Double d = (Double) ops().run("test.any", thing);

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
		Double d = (Double) ops().run("test.exceptionalAny", ething);

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
@Parameter(key = "output", type = ItemIO.OUTPUT)
class ThingFunction implements Function<Thing<String>, Double> {

	@Override
	public Double apply(Thing<String> t) {
		return t.create("Hello");
	}

}

@Plugin(type = Op.class, name = "test.exceptionalAny")
@Parameter(key = "thing")
@Parameter(key = "output", type = ItemIO.OUTPUT)
class ExceptionalThingFunction implements Function<ExceptionalThing<String>, Double> {

	@Override
	public Double apply(ExceptionalThing<String> t) {
		String s = t.getU();
		return t.create("Hello");
	}

}

@Plugin(type = Op.class, name = "test.nestedAny")
@Parameter(key = "nestedThing")
@Parameter(key = "output", type = ItemIO.OUTPUT)
class NestedThingFunction implements Function<NestedThing<String, Thing<String>>, Double> {

	@Override
	public Double apply(NestedThing<String, Thing<String>> t) {
		return 5.;
	}

}
