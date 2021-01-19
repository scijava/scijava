
package org.scijava.ops.matcher;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.matcher.MatchingUtils.TypeInferenceException;
import org.scijava.ops.matcher.MatchingUtils.TypeMapping;
import org.scijava.ops.matcher.MatchingUtilsTest.StrangeThing;
import org.scijava.ops.matcher.MatchingUtilsTest.Thing;
import org.scijava.types.Nil;

public class InferTypeVariablesTest {

	@Test
	public <I, O> void testSupertypeTypeInference()
		throws TypeInferenceException
	{
		final Type t = new Nil<Function<Thing<I>, List<O>>>() {}.getType();
		final Type[] tArgs = ((ParameterizedType) t).getActualTypeArguments();
		final Type dest =
			new Nil<Function<StrangeThing<Double, String>, List<Double>>>()
			{}.getType();
		final Type[] destArgs = ((ParameterizedType) dest).getActualTypeArguments();

		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns =
			new HashMap<>();
		MatchingUtils.inferTypeVariablesWithTypeMappings(tArgs, destArgs, typeAssigns);

		// We expect I=String, O=Double
		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected =
			new HashMap<>();
		TypeVariable<?> typeVarI = (TypeVariable<?>) ((ParameterizedType) tArgs[0])
			.getActualTypeArguments()[0];
		expected.put(typeVarI, new TypeMapping(typeVarI, String.class, false));
		TypeVariable<?> typeVarO = (TypeVariable<?>) ((ParameterizedType) tArgs[1])
			.getActualTypeArguments()[0];
		expected.put(typeVarO, new TypeMapping(typeVarO, Double.class, false));

		assertEquals(typeAssigns, expected);
	}

	@Test
	public <T> void testWildcardTypeInference() throws TypeInferenceException {
		final Type t = new Nil<T>() {}.getType();
		final Type listWild = new Nil<List<? extends T>>() {}.getType();
		final Type integer = new Nil<Integer>() {}.getType();
		final Type listDouble = new Nil<List<Double>>() {}.getType();

		final Type[] types = { listWild, t };
		final Type[] inferFroms = { listDouble, integer };

		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns =
			new HashMap<>();
		MatchingUtils.inferTypeVariablesWithTypeMappings(types, inferFroms, typeAssigns);

		// We expect T=Number
		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected =
			new HashMap<>();
		TypeVariable<?> typeVar = (TypeVariable<?>) t;
		expected.put(typeVar, new TypeMapping(typeVar, Number.class, true));

		assertEquals(expected, typeAssigns);

		final Type[] types2 = { t, t };
		final Type listWildcardNumber = new Nil<List<? extends Number>>() {}
			.getType();
		final Type wildcardNumber = ((ParameterizedType) listWildcardNumber)
			.getActualTypeArguments()[0];
		final Type listWildcardDouble = new Nil<List<? extends Double>>() {}
			.getType();
		final Type wildcardDouble = ((ParameterizedType) listWildcardDouble)
			.getActualTypeArguments()[0];

		final Type[] inferFroms2 = { wildcardNumber, wildcardDouble };

		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns2 =
			new HashMap<>();
		MatchingUtils.inferTypeVariablesWithTypeMappings(types2, inferFroms2, typeAssigns2);

		// We expect T=Number
		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected2 =
			new HashMap<>();
		TypeVariable<?> typeVar2 = (TypeVariable<?>) t;
		expected2.put(typeVar2, new TypeMapping(typeVar, Number.class, true));

		assertEquals(expected2, typeAssigns2);
	}

	@Test
	public <T extends Number> void
		testInferFromWildcardExtendingParameterizedType()
			throws TypeInferenceException
	{
		final Nil<List<? extends Comparable<T>>> listT = new Nil<>() {};
		final Nil<List<? extends Comparable<Double>>> listWildcard = new Nil<>() {};

		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns =
			new HashMap<>();
		MatchingUtils.inferTypeVariables(listT.getType(), listWildcard.getType(),
			typeAssigns);

		// We expect T=Double
		final Type t = new Nil<T>() {}.getType();
		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected =
			new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, false));

		assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void testInferFromWildcardExtendingClass()
		throws TypeInferenceException
	{
		final Nil<List<? extends T>> listT = new Nil<>() {};
		final Nil<List<? extends Double>> listWildcard = new Nil<>() {};

		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns =
			new HashMap<>();
		MatchingUtils.inferTypeVariables(listT.getType(), listWildcard.getType(),
			typeAssigns);

		// We expect T= (? extends Double)
		final Type t = new Nil<T>() {}.getType();
		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected =
			new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		Type mappedType = ((ParameterizedType) listWildcard.getType())
			.getActualTypeArguments()[0];
		WildcardType mappedWildcard = (WildcardType) mappedType;
		expected.put(typeVarT, new MatchingUtils.WildcardTypeMapping(typeVarT,
			mappedWildcard, true));

		assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void testInferWildcardAndClass()
		throws TypeInferenceException
	{
		final Nil<List<? super T>> listT = new Nil<>() {};
		final Nil<T> t = new Nil<>() {};
		final Nil<List<? super Number>> listWildcard = new Nil<>() {};

		Type[] types = new Type[] { listT.getType(), t.getType() };
		Type[] inferFroms = new Type[] { listWildcard.getType(), Double.class };

		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns =
			new HashMap<>();
		MatchingUtils.inferTypeVariablesWithTypeMappings(types, inferFroms, typeAssigns);

		// We expect T=Number
		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected =
			new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t.getType();
		expected.put(typeVarT, new TypeMapping(typeVarT, Number.class, true));

		assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void testInferSuperWildcard()
		throws TypeInferenceException
	{
		final Nil<List<? super T>> listT = new Nil<>() {};
		final Nil<List<? super Number>> listWildcard = new Nil<>() {};

		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns =
			new HashMap<>();
		MatchingUtils.inferTypeVariables(listT.getType(), listWildcard.getType(), typeAssigns);

		// We expect T=Number
		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected =
			new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) new Nil<T>() {}.getType();
		expected.put(typeVarT, new TypeMapping(typeVarT, Number.class, true));

		assertEquals(expected, typeAssigns);
	}

	@Test
	public <T, U extends Comparable<Double>> void testInferFromTypeVar()
		throws TypeInferenceException
	{
		final Type compT = new Nil<Comparable<T>>() {}.getType();
		final Type u = new Nil<U>() {}.getType();

		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns =
			new HashMap<>();
		MatchingUtils.inferTypeVariables(compT, u, typeAssigns);

		// We expect T=Double
		final Type t = new Nil<T>() {}.getType();
		final Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected =
			new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, false));

		assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void testInferTypeVarInconsistentMapping()
		throws TypeInferenceException
	{

		final Type t = new Nil<T>() {}.getType();

		final Type[] tArr = { t, t };
		final Type[] badInferFrom = { Integer.class, Double.class };

		Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns =
			new HashMap<>();
		MatchingUtils.inferTypeVariablesWithTypeMappings(tArr, badInferFrom, typeAssigns);

		// We expect T=Number
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected = new HashMap<>();
		expected.put(typeVarT, new TypeMapping(typeVarT, Number.class, true));

		assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void
		testInferGenericArrayTypeFromExtendingWildcardType()
			throws TypeInferenceException
	{
		final Type type = new Nil<List<T[]>>() {}.getType();
		final Type inferFrom = new Nil<List<? extends Double[]>>() {}.getType();

		Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns =
			new HashMap<>();
		MatchingUtils.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect T=Double
		TypeVariable<?> typeVarT = (TypeVariable<?>) new Nil<T>() {}.getType();
		Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected = new HashMap<>();
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, true));

		assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void
		testInferGenericArrayTypeFromSuperWildcardType()
			throws TypeInferenceException
	{
		final Type type = new Nil<List<T[]>>() {}.getType();
		final Type inferFrom = new Nil<List<? super Double[]>>() {}.getType();

		Map<TypeVariable<?>, MatchingUtils.TypeMapping> typeAssigns =
			new HashMap<>();
		MatchingUtils.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect T=Double
		TypeVariable<?> typeVarT = (TypeVariable<?>) new Nil<T>() {}.getType();
		Map<TypeVariable<?>, MatchingUtils.TypeMapping> expected = new HashMap<>();
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, true));

		assertEquals(expected, typeAssigns);
	}
}
