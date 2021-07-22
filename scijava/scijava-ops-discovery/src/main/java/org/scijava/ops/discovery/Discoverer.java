
package org.scijava.ops.discovery;

import java.util.List;

public interface Discoverer {

	<T> List<? extends Class<T>> implementingClasses(Class<T> c);

	<T> List<? extends T> implementingInstances(Class<T> c, Class<?>[] constructorClasses, Object[] constructorArgs);

	<T> List<Implementation<T>> implementationsOf(Class<T> c);

}
