package org.scijava.test_util;

import java.util.Iterator;

public class AssertIterations {

	public static <T> boolean equal(final Iterable<T> expected,
			final Iterable<T> actual)
		{
			final Iterator<T> e = expected.iterator();
			final Iterator<T> a = actual.iterator();
			while (e.hasNext()) {
				if (!a.hasNext()) return false;
				if (!(e.next().equals(a.next()))) return false;
			}
			if (a.hasNext()) return false;
			return true;
		}
}
