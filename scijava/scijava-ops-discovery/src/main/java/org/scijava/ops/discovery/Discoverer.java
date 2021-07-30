
package org.scijava.ops.discovery;

import java.util.List;

public interface Discoverer {

	<T> List<Class<T>> implementingClasses(Class<T> c);

}
