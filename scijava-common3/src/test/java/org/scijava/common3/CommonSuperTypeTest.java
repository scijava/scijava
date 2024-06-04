/*-
 * #%L
 * Common functionality widely used across SciJava modules.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.common3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests {@link Types#commonSuperTypeOf(Type[])}. */
public class CommonSuperTypeTest {

	interface Thing { }

	interface Stuff { }

	static class XThing extends Base implements Thing { }

	static class Base { }

	static class NThing extends Base implements Thing, Stuff { }

	static class QThing extends YThing implements Stuff { }

	static class YThing implements Thing { }

	static abstract class RecursiveThing<T extends RecursiveThing<T>> { }

	static class StrangeThing extends RecursiveThing<StrangeThing> { }

	static class WeirdThing extends RecursiveThing<WeirdThing> { }

	@Test
	public void testDouble() {
		Type t1 = Double.class;
		Type t2 = Double.class;
		Type superType = Types.commonSuperTypeOf(t1, t2);
		assertEquals(Double.class, superType);
	}

	@Test
	public void testNumber() {
		Type t1 = Double.class;
		Type t2 = Long.class;
		Type superType = Types.commonSuperTypeOf(t1, t2);
		assertEquals(Number.class, superType);
	}

	@Test
	public void testObject() {
		Type t1 = Double.class;
		Type t2 = String.class;
		Type superType = Types.commonSuperTypeOf(t1, t2);
		Type expected = Types.parameterize(Comparable.class, Types.wildcard());
		assertEquals(expected, superType);
	}

	@Test
	public void testListOfSame() {
		Type listOfDouble = Types.parameterize(List.class, Double.class);
		Type superType = Types.commonSuperTypeOf(listOfDouble, listOfDouble);
		assertEquals(listOfDouble, superType);
		Type listOfObject = Types.parameterize(List.class, Object.class);
		assertNotEquals(listOfObject, superType, "Class Double should take precedence over Object");
		Type listOfQ = Types.parameterize(List.class, Types.wildcard());
		assertNotEquals(listOfQ, superType, "Class Double should be discernable over wildcard");
		assertNotEquals(List.class, superType, "Class Double should be discernable, rawtype should not be returned");
	}

	@Test
	public void testListOfDifferent() {
		Type t1 = Types.parameterize(List.class, Double.class);
		Type t2 = Types.parameterize(List.class, String.class);
		Type superType = Types.commonSuperTypeOf(t1, t2);
		Type comparableQ = Types.parameterize(Comparable.class, Types.wildcard());
		Type expectedListType = Types.wildcard(comparableQ);
		Type expected = Types.parameterize(List.class, expectedListType);
		assertEquals(expected, superType);
	}

	@Test
	public void testListOfListOfDifferent() {
		Type listDouble = Types.parameterize(List.class, Double.class);
		Type listString = Types.parameterize(List.class, String.class);
		Type t1 = Types.parameterize(List.class, listDouble);
		Type t2 = Types.parameterize(List.class, listString);
		Type superType = Types.commonSuperTypeOf(t1, t2);
		Type comparableQ = Types.parameterize(Comparable.class, Types.wildcard());
		Type expectedType = Types.wildcard(comparableQ);
		Type expectedList = Types.parameterize(List.class, expectedType);
		Type expectedListType = Types.wildcard(expectedList);
		Type expected = Types.parameterize(List.class, expectedListType);
		assertEquals(expected, superType);
	}

	@Test
	public void testArrayListAndList() {
		Type t1 = Types.parameterize(List.class, Double.class);
		Type t2 = Types.parameterize(ArrayList.class, Double.class);
		Type superType = Types.commonSuperTypeOf(t1, t2);
		assertEquals(t1, superType);
		Type superType2 = Types.commonSuperTypeOf(t2, t1);
		assertEquals(t1, superType2);
	}

	@Test
	public void testNThingQThing() {
		Type t3 = NThing.class;
		Type t4 = QThing.class;
		Type superType = Types.commonSuperTypeOf(t3, t4);
		Type expected = Types.wildcard(Thing.class, Stuff.class);
		assertEquals(expected, superType);
	}

	@Test
	public void testNThingYThing() {
		Type t3 = NThing.class;
		Type t4 = YThing.class;
		Type superType = Types.commonSuperTypeOf(t3, t4);
		Type expected = Types.wildcard(Thing.class);
		assertNotEquals(expected, superType, "Greatest common type should not be a wildcard");
		assertEquals(Thing.class, superType);
	}

	@Test
	public void testNThingXThing() {
		Type t3 = NThing.class;
		Type t4 = XThing.class;
		Type superType = Types.commonSuperTypeOf(t3, t4);
		assertEquals(Base.class, superType);
		assertNotEquals(Thing.class, superType, "Non-Object classes should take precedence over interfaces");
	}

	@Test
	public void testRecursiveClass() {
		Type t1 = StrangeThing.class;
		Type t2 = WeirdThing.class;
		Type superType = Types.commonSuperTypeOf(t1, t2);
		Type expected = Types.parameterize(RecursiveThing.class, Types.wildcard());
		assertEquals(expected, superType);
	}

	@Test
	public void testTypeVar() {
		class C<T extends Base> {}
		Type t1 = C.class.getTypeParameters()[0];
		Type t2 = NThing.class;
		Type superType = Types.commonSuperTypeOf(t1, t2);
		assertEquals(Base.class, superType);
	}

	@Test
	public void testWildcardType() {
		Type qNThing = Types.wildcard(NThing.class);
		ParameterizedType typeWithWildcard = Types.parameterize(List.class, qNThing);
		Type t1 = typeWithWildcard.getActualTypeArguments()[0];
		Type t2 = XThing.class;
		Type superType = Types.commonSuperTypeOf(t1, t2);
		assertEquals(Base.class, superType);
	}
}
