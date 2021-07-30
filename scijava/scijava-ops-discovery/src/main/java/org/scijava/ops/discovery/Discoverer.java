
package org.scijava.ops.discovery;

import java.util.List;

public interface Discoverer {

	<T> List<? extends Class<T>> implementingClasses(Class<T> c);

}
