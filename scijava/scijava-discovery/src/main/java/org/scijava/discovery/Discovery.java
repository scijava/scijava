
package org.scijava.discovery;

import java.lang.reflect.Type;

/**
 * A container for {@link Object}s returned by a {@link Discoverer}, grouping
 * differentiating metadata.
 * 
 * @author Gabriel Selzer
 * @param <T> the generic {@link Type} of this discovery.
 */
public class Discovery<T> {

	private final T discovery;

	private final String tag;

	public Discovery(T discovery, String tag) {
		this.discovery = discovery;
		this.tag = tag;
	}


	public T discovery() {
		return discovery;
	}

	public String tag() {
		return tag;
	}

}
