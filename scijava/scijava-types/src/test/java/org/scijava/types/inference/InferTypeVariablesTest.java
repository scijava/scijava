
package org.scijava.types.inference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.types.Any;
import org.scijava.types.Nil;

public class InferTypeVariablesTest {

	class Bar {}

	static class FooThing extends RecursiveThing<FooThing> {}

	static abstract class RecursiveThing<T extends RecursiveThing<T>> {}

	class StrangeThing<N extends Number, T> extends Thing<T> {}

	class Thing<T> {}

	class TypedBar<E> extends Bar {

		E type;
	}

	@Test
	public <T, U extends Comparable<Double>> void testInferFromTypeVar()
		throws TypeInferenceException
	{
		final Type compT = new Nil<Comparable<T>>() {}.getType();
		final Type u = new Nil<U>() {}.getType();

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(compT, u, typeAssigns);

		// We expect T=Double
		final Type t = new Nil<T>() {}.getType();
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, false));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void testInferFromWildcardExtendingClass()
		throws TypeInferenceException
	{
		final Nil<List<? extends T>> listT = new Nil<>() {};
		final Nil<List<? extends Double>> listWildcard = new Nil<>() {};

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(listT.getType(), listWildcard
			.getType(), typeAssigns);

		// We expect T= (? extends Double)
		final Type t = new Nil<T>() {}.getType();
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		Type mappedType = ((ParameterizedType) listWildcard.getType())
			.getActualTypeArguments()[0];
		WildcardType mappedWildcard = (WildcardType) mappedType;
		expected.put(typeVarT, new WildcardTypeMapping(typeVarT, mappedWildcard,
			true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void
		testInferFromWildcardExtendingParameterizedType()
			throws TypeInferenceException
	{
		final Nil<List<? extends Comparable<T>>> listT = new Nil<>() {};
		final Nil<List<? extends Comparable<Double>>> listWildcard = new Nil<>() {};

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(listT.getType(), listWildcard
			.getType(), typeAssigns);

		// We expect T=Double
		final Type t = new Nil<T>() {}.getType();
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, false));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void
		testInferGenericArrayTypeFromExtendingWildcardType()
			throws TypeInferenceException
	{
		final Type type = new Nil<List<T[]>>() {}.getType();
		final Type inferFrom = new Nil<List<? extends Double[]>>() {}.getType();

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect T=Double
		TypeVariable<?> typeVarT = (TypeVariable<?>) new Nil<T>() {}.getType();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void
		testInferGenericArrayTypeFromSuperWildcardType()
			throws TypeInferenceException
	{
		final Type type = new Nil<List<T[]>>() {}.getType();
		final Type inferFrom = new Nil<List<? super Double[]>>() {}.getType();

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect T=Double
		TypeVariable<?> typeVarT = (TypeVariable<?>) new Nil<T>() {}.getType();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarT, new TypeMapping(typeVarT, Double.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends Number> void testInferOToAny()
		throws TypeInferenceException
	{
		final Type iterableO = new Nil<Iterable<O>>() {}.getType();
		final Type object = Object.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(iterableO, object, typeAssigns);

		// We expect O = Any
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.getType();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarO, new TypeMapping(typeVarO, new Any(), true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends Number> void testInferOToAnyWithClass()
		throws TypeInferenceException
	{
		final Type type = new Nil<TypedBar<O>>() {}.getType();
		final Type inferFrom = Bar.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect O = Any
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.getType();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarO, new TypeMapping(typeVarO, new Any(), true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends Number> void testInferOToAnyWithInterface()
		throws TypeInferenceException
	{
		final Type type = new Nil<ArrayList<O>>() {}.getType();
		final Type inferFrom = Cloneable.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect O = Any
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.getType();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarO, new TypeMapping(typeVarO, new Any(), true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends Number> void testInferOToAnyWithRawType()
		throws TypeInferenceException
	{
		final Type type = new Nil<TypedBar<O>>() {}.getType();
		final Type inferFrom = TypedBar.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect O = Any
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.getType();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarO, new TypeMapping(typeVarO, new Any(), true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends RecursiveThing<O>> void testInferRecursiveTypeVar() {
		final Type type = new Nil<O>() {}.getType();
		final Type inferFrom = FooThing.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect O = FooThing
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.getType();
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarO, new TypeMapping(typeVarO, FooThing.class, false));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void testInferSuperWildcard()
		throws TypeInferenceException
	{
		final Nil<List<? super T>> listT = new Nil<>() {};
		final Nil<List<? super Number>> listWildcard = new Nil<>() {};

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(listT.getType(), listWildcard
			.getType(), typeAssigns);

		// We expect T=Number
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) new Nil<T>() {}.getType();
		expected.put(typeVarT, new TypeMapping(typeVarT, Number.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <O extends Number, I extends O> void
		testInferTypeVarExtendingTypeVar()
	{
		final Type type = new Nil<I>() {}.getType();
		final Type inferFrom = Double.class;

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariables(type, inferFrom, typeAssigns);

		// We expect I= Double, O = Double
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarI = (TypeVariable<?>) new Nil<I>() {}.getType();
		expected.put(typeVarI, new TypeMapping(typeVarI, Double.class, true));
		TypeVariable<?> typeVarO = (TypeVariable<?>) new Nil<O>() {}.getType();
		expected.put(typeVarO, new TypeMapping(typeVarO, Double.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

	@Test
	public <T extends Number> void testInferTypeVarInconsistentMapping()
		throws TypeInferenceException
	{

		final Type t = new Nil<T>() {}.getType();

		final Type[] tArr = { t, t };
		final Type[] badInferFrom = { Integer.class, Double.class };

		Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariablesWithTypeMappings(tArr, badInferFrom,
			typeAssigns);

		// We expect T=Number
		TypeVariable<?> typeVarT = (TypeVariable<?>) t;
		Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		expected.put(typeVarT, new TypeMapping(typeVarT, Number.class, true));

		Assertions.assertEquals(expected, typeAssigns);
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

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariablesWithTypeMappings(types, inferFroms,
			typeAssigns);

		// We expect T=Number
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarT = (TypeVariable<?>) t.getType();
		expected.put(typeVarT, new TypeMapping(typeVarT, Number.class, true));

		Assertions.assertEquals(expected, typeAssigns);
	}

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

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariablesWithTypeMappings(tArgs, destArgs,
			typeAssigns);

		// We expect I=String, O=Double
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVarI = (TypeVariable<?>) ((ParameterizedType) tArgs[0])
			.getActualTypeArguments()[0];
		expected.put(typeVarI, new TypeMapping(typeVarI, String.class, false));
		TypeVariable<?> typeVarO = (TypeVariable<?>) ((ParameterizedType) tArgs[1])
			.getActualTypeArguments()[0];
		expected.put(typeVarO, new TypeMapping(typeVarO, Double.class, false));

		Assertions.assertEquals(typeAssigns, expected);
	}

	@Test
	public <T> void testWildcardTypeInference() throws TypeInferenceException {
		final Type t = new Nil<T>() {}.getType();
		final Type listWild = new Nil<List<? extends T>>() {}.getType();
		final Type integer = new Nil<Integer>() {}.getType();
		final Type listDouble = new Nil<List<Double>>() {}.getType();

		final Type[] types = { listWild, t };
		final Type[] inferFroms = { listDouble, integer };

		final Map<TypeVariable<?>, TypeMapping> typeAssigns = new HashMap<>();
		GenericAssignability.inferTypeVariablesWithTypeMappings(types, inferFroms,
			typeAssigns);

		// We expect T=Number
		final Map<TypeVariable<?>, TypeMapping> expected = new HashMap<>();
		TypeVariable<?> typeVar = (TypeVariable<?>) t;
		expected.put(typeVar, new TypeMapping(typeVar, Number.class, true));

		Assertions.assertEquals(expected, typeAssigns);

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

		final Map<TypeVariable<?>, TypeMapping> typeAssigns2 = new HashMap<>();
		GenericAssignability.inferTypeVariablesWithTypeMappings(types2, inferFroms2,
			typeAssigns2);

		// We expect T=Number
		final Map<TypeVariable<?>, TypeMapping> expected2 = new HashMap<>();
		TypeVariable<?> typeVar2 = (TypeVariable<?>) t;
		expected2.put(typeVar2, new TypeMapping(typeVar, Number.class, true));

		Assertions.assertEquals(expected2, typeAssigns2);
	}

}
