
package org.scijava.discovery;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@link Discoverer} implementation that can be set up by the user. Useful
 * when a small number of implementation are already exposed.
 * 
 * @author Gabriel Selzer
 */
public class ManualDiscoverer implements Discoverer {

	/**
	 * The implementations
	 */
	Set<Object> set;

	public ManualDiscoverer() {
		set = new HashSet<>();
	}

	public void register(Object[]... objects) {
		for (Object[] arr : objects) {
			for (Object o : arr) {
				set.add(o);
			}
		}
	}

	public void register(Object... objects) {
		for (Object o : objects) {
			set.add(o);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> discover(Class<T> c) {
		return set.parallelStream() //
				.filter(o -> c.isAssignableFrom(o.getClass())) //
				.map(o -> (T) o) //
				.collect(Collectors.toList());
	}

}
