package org.scijava.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GreatestCommonSupertypeTest {

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
	
	static abstract class RecursiveThing<T extends RecursiveThing<T>> {
		
	}
	
	static class StrangeThing extends RecursiveThing<StrangeThing> {
		
	}
	
	static class WeirdThing extends RecursiveThing<WeirdThing> {
		
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
		assertFalse(superType.equals(new Nil<List<Object>>() {}.getType()),
			"Class Double should take precedence over Object");
		assertFalse(superType.equals(new Nil<List<?>>() {}.getType()),
			"Class Double should be discernable over wildcard");
		assertFalse(superType.equals(new Nil<List>() {}.getType()),
			"Class Double should be discernable, rawtype should not be returned");
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
		assertFalse(superType.equals(expected),
			"Greatest common type should not be a wildcard");
		assertTrue(superType.equals(new Nil<Thing>() {}.getType()));
	}

	@Test
	public void NThingXThingTest() {
		Type t3 = new Nil<NThing>() {}.getType();
		Type t4 = new Nil<XThing>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t3, t4 }, false);
		assertTrue(superType.equals(new Nil<Base>() {}.getType()));
		assertFalse(superType.equals(new Nil<Thing>() {}.getType()),
			"Non-Object classes should take precedence over interfaces");
	}
	
	@Test
	public void RecursiveClassTest() {
		Type t1 = new Nil<StrangeThing>() {}.getType();
		Type t2 = new Nil<WeirdThing>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] {t1, t2}, false);
		Nil<RecursiveThing<?>> expected = new Nil<>() {};
		assertTrue(superType.equals(expected.getType()));
	}

	@Test
	public <T extends Base> void typeVarTest() {
		Type t1 = new Nil<T>() {}.getType();
		Type t2 = new Nil<NThing>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t1, t2 },
			false);
		Nil<Base> expected = new Nil<>() {};
		assertTrue(superType.equals(expected.getType()));
	}

	@Test
	public void wildcardTypeTest() {
		Type typeWithWildcard = new Nil<List<? extends NThing>>() {}.getType();
		Type t1 = ((ParameterizedType) typeWithWildcard)
			.getActualTypeArguments()[0];
		Type t2 = new Nil<XThing>() {}.getType();
		Type superType = Types.greatestCommonSuperType(new Type[] { t1, t2 },
			false);
		Nil<Base> expected = new Nil<>() {};
		assertTrue(superType.equals(expected.getType()));
	}
}
