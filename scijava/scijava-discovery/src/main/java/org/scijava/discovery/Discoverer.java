
package org.scijava.discovery;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.List;

public interface Discoverer {

	@SuppressWarnings("unused")
	default <T> List<Class<T>> implsOfType(Class<T> c) {
		return Collections.emptyList();
	}

	@SuppressWarnings("unused")
	default List<AnnotatedElement> elementsTaggedWith(String... tags) {
		return Collections.emptyList();
	}

}
