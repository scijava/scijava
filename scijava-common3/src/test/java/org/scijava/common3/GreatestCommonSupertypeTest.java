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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests {@link Types#superTypeOf}. */
public class GreatestCommonSupertypeTest {

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
		Type superType = Types.superTypeOf(new Type[] { t1, t2 },
			false);
		assertTrue(superType.equals(Double.class));
	}

	@Test
	public void testNumber() {
		Type t1 = Double.class;
		Type t2 = Long.class;
		Type superType = Types.superTypeOf(new Type[] { t1, t2 },
			false);
		assertTrue(superType.equals(Number.class));
	}

	@Test
	public void testObject() {
		Type t1 = Double.class;
		Type t2 = String.class;
		Type superType = Types.superTypeOf(new Type[] { t1, t2 },
			false);
		Type expected = Types.parameterize(Comparable.class, new Type[] { Types.wildcard() });
		assertTrue(superType.equals(expected));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testListOfSame() {
		Type listOfDouble = Types.parameterize(List.class, new Type[] { Double.class });
		Type superType = Types.superTypeOf(new Type[] { listOfDouble, listOfDouble },
			false);
		assertTrue(superType.equals(listOfDouble));
		Type listOfObject = Types.parameterize(List.class, new Type[] { Object.class });
		assertFalse(superType.equals(listOfObject),
			"Class Double should take precedence over Object");
		Type listOfQ = Types.parameterize(List.class, new Type[] { Types.wildcard() });
		assertFalse(superType.equals(listOfQ),
			"Class Double should be discernable over wildcard");
		assertFalse(superType.equals(List.class),
			"Class Double should be discernable, rawtype should not be returned");
	}

	@Test
	public void testListOfDifferent() {
		Type t1 = Types.parameterize(List.class, new Type[] { Double.class });
		Type t2 = Types.parameterize(List.class, new Type[] { String.class });
		Type superType = Types.superTypeOf(new Type[] { t1, t2 },
			false);
		Type comparableQ = Types.parameterize(Comparable.class, new Type[] { Types.wildcard() });
		Type expectedListType = Types.wildcard(new Type[] { comparableQ }, new Type[] {});
		Type expected = Types.parameterize(List.class, new Type[] { expectedListType });
		assertTrue(superType.equals(expected));
	}

	@Test
	public void testListOfListOfDifferent() {
		Type listDouble = Types.parameterize(List.class, new Type[] { Double.class });
		Type listString = Types.parameterize(List.class, new Type[] { String.class });
		Type t1 = Types.parameterize(List.class, new Type[] { listDouble });
		Type t2 = Types.parameterize(List.class, new Type[] { listString });
		Type superType = Types.superTypeOf(new Type[] { t1, t2 }, false);
		Type comparableQ = Types.parameterize(Comparable.class, new Type[] { Types.wildcard() });
		Type expectedType = Types.wildcard(new Type[] { comparableQ }, new Type[] {});
		Type expectedList = Types.parameterize(List.class, new Type[] { expectedType });
		Type expectedListType = Types.wildcard(new Type[] { expectedList }, new Type[] {});
		Type expected = Types.parameterize(List.class, new Type[] { expectedListType });
		assertTrue(superType.equals(expected));
	}

	@Test
	public void testArrayListAndList() {
		Type t1 = Types.parameterize(List.class, new Type[] { Double.class });
		Type t2 = Types.parameterize(ArrayList.class, new Type[] { Double.class });
		Type superType = Types.superTypeOf(new Type[] { t1, t2 }, false);
		assertTrue(superType.equals(t1));
		Type superType2 = Types.superTypeOf(new Type[] { t2, t1 }, false);
		assertTrue(superType2.equals(t1));
	}

	@Test
	public void testNThingQThing() {
		Type t3 = NThing.class;
		Type t4 = QThing.class;
		Type superType = Types.superTypeOf(new Type[] { t3, t4 }, false);
		Type expected = Types.wildcard(new Type[] { Thing.class, Stuff.class }, new Type[] {});
		assertTrue(superType.equals(expected));
	}

	@Test
	public void testNThingYThing() {
		Type t3 = NThing.class;
		Type t4 = YThing.class;
		Type superType = Types.superTypeOf(new Type[] { t3, t4 }, false);
		Type expected = Types.wildcard(new Type[] { Thing.class }, new Type[] {});
		assertFalse(superType.equals(expected),
			"Greatest common type should not be a wildcard");
		assertTrue(superType.equals(Thing.class));
	}

	@Test
	public void testNThingXThing() {
		Type t3 = NThing.class;
		Type t4 = XThing.class;
		Type superType = Types.superTypeOf(new Type[] { t3, t4 }, false);
		assertTrue(superType.equals(Base.class));
		assertFalse(superType.equals(Thing.class),
			"Non-Object classes should take precedence over interfaces");
	}

	@Test
	public void testRecursiveClass() {
		Type t1 = StrangeThing.class;
		Type t2 = WeirdThing.class;
		Type superType = Types.superTypeOf(new Type[] { t1, t2 },
			false);
		Type expected = Types.parameterize(RecursiveThing.class, new Type[] { Types.wildcard() });
		assertTrue(superType.equals(expected));
	}

	@Test
	public void testTypeVar() {
		class C<T extends Base> {}
		Type t1 = C.class.getTypeParameters()[0];
		Type t2 = NThing.class;
		Type superType = Types.superTypeOf(new Type[] { t1, t2 }, false);
		assertTrue(superType.equals(Base.class));
	}

	@Test
	public void testWildcardType() {
		Type qNThing = Types.wildcard(NThing.class);
		Type typeWithWildcard = Types.parameterize(List.class, new Type[] { qNThing });
		Type t1 = ((ParameterizedType) typeWithWildcard).getActualTypeArguments()[0];
		Type t2 = XThing.class;
		Type superType = Types.superTypeOf(new Type[] { t1, t2 }, false);
		assertTrue(superType.equals(Base.class));
	}
}
