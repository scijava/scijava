package org.scijava.ops.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

@Plugin(type = OpCollection.class)
public class GreatestCommonSupertypeTest extends AbstractTestEnvironment {

	interface Thing {

	}

	interface Stuff {

	}

	static class XThing extends Base implements Thing {

	}

	static class Base {

	}

	static class NThing extends Base implements Thing, Stuff {

	}

	static class QThing extends YThing implements Stuff {

	}

	static class YThing implements Thing {

	}

	@Test
	public void DoubleTest() {
		Type t1 = new Nil<Double>() {}.getType();
		Type t2 = new Nil<Double>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t1, t2 }, false);
		assertTrue(superType.equals(Double.class));
	}

	@Test
	public void NumberTest() {
		Type t1 = new Nil<Double>() {}.getType();
		Type t2 = new Nil<Long>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t1, t2 }, false);
		assertTrue(superType.equals(Number.class));
	}

	@Test
	public void ObjectTest() {
		Type t1 = new Nil<Double>() {}.getType();
		Type t2 = new Nil<String>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t1, t2 }, false);
		Type expected = new Nil<Comparable<?>>() {}.getType();
		assertTrue(superType.equals(expected));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void ListOfSameTest() {
		Type t1 = new Nil<List<Double>>() {}.getType();
		Type t2 = new Nil<List<Double>>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t1, t2 }, false);
		assertTrue(superType.equals(new Nil<List<Double>>() {}.getType()));
		assertFalse("Class Double should take precedence over Object",
				superType.equals(new Nil<List<Object>>() {}.getType()));
		assertFalse("Class Double should be discernable over wildcard",
				superType.equals(new Nil<List<?>>() {}.getType()));
		assertFalse("Class Double should be discernable, rawtype should not be returned",
				superType.equals(new Nil<List>() {}.getType()));
	}

	@Test
	public void ListOfDifferentTest() {
		Type t1 = new Nil<List<Double>>() {}.getType();
		Type t2 = new Nil<List<String>>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t1, t2 }, false);
		Type expectedListType = Types.wildcard(new Type[] { new Nil<Comparable<?>>() {}.getType() }, new Type[] {});
		Type expected = Types.parameterize(List.class, new Type[] { expectedListType });
		assertTrue(superType.equals(expected));
	}

	@Test
	public void ListOfListOfDifferentTest() {
		Type t1 = new Nil<List<List<Double>>>() {}.getType();
		Type t2 = new Nil<List<List<String>>>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t1, t2 }, false);
		Type expectedType = Types.wildcard(new Type[] { new Nil<Comparable<?>>() {}.getType() }, new Type[] {});
		Type expectedList = Types.parameterize(List.class, new Type[] { expectedType });
		Type expectedListType = Types.wildcard(new Type[] { expectedList }, new Type[] {});
		Type expected = Types.parameterize(List.class, new Type[] { expectedListType });
		assertTrue(superType.equals(expected));
	}

	@Test
	public void ArrayListAndListTest() {
		Type t1 = new Nil<List<Double>>() {}.getType();
		Type t2 = new Nil<ArrayList<Double>>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t1, t2 }, false);
		assertTrue(superType.equals(new Nil<List<Double>>() {}.getType()));
		Type t3 = new Nil<ArrayList<Double>>() {}.getType();
		Type t4 = new Nil<List<Double>>() {}.getType();
		Type superType2 = Types.greatestCommonSuperType(new Type[] { t3, t4 }, false);
		assertTrue(superType2.equals(new Nil<List<Double>>() {}.getType()));
	}

	@Test
	public void NThingQThingTest() {
		Type t3 = new Nil<NThing>() {}.getType();
		Type t4 = new Nil<QThing>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t3, t4 }, false);
		Type expected = Types.wildcard(new Type[] { new Nil<Thing>() {}.getType(), new Nil<Stuff>() {}.getType() },
				new Type[] {});
		assertTrue(superType.equals(expected));
	}

	@Test
	public void NThingYThingTest() {
		Type t3 = new Nil<NThing>() {}.getType();
		Type t4 = new Nil<YThing>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t3, t4 }, false);
		Type expected = Types.wildcard(new Type[] { new Nil<Thing>() {}.getType() }, new Type[] {});
		assertFalse("Greatest common type should not be a wildcard", superType.equals(expected));
		assertTrue(superType.equals(new Nil<Thing>() {}.getType()));
	}

	@Test
	public void NThingXThingTest() {
		Type t3 = new Nil<NThing>() {}.getType();
		Type t4 = new Nil<XThing>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t3, t4 }, false);
		assertTrue(superType.equals(new Nil<Base>() {}.getType()));
		assertFalse("Non-Object classes should take precedence over interfaces",
				superType.equals(new Nil<Thing>() {}.getType()));
	}

	@OpField(names = "test.listTypeReification")
	@Parameter(key = "input")
	@Parameter(key = "output")
	public static final Function<List<? extends Thing>, List<Double>> fooOP = (in) -> {
		List<Double> returnList = new ArrayList<>();
		returnList.add(0.);
		return returnList;
	};

	@Test
	public void OpMatchingIntegrationTest() {
		List<Thing> things = new ArrayList<>();
		things.add(new NThing());
		things.add(new XThing());
		things.add(new YThing());
		List<Double> actual = fooOP.apply(things);
		// N.B. The type reifier reifies this list to a List<Thing>
		List<Double> expected = new OpBuilder(ops, "test.listTypeReification").input(things)
				.outType(new Nil<List<Double>>() {}).apply();
		assertEquals(actual, expected);
	}
}
