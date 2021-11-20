
package org.scijava.discovery;

import java.util.List;

public interface Discoverer {

	<T> List<Class<T>> implementingClasses(Class<T> c);

}
