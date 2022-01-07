
package org.scijava.ops.engine.impl;

import java.util.Optional;
import java.util.ServiceLoader;

import org.scijava.discovery.therapi.TaggedElement;
import org.scijava.discovery.therapi.TherapiDiscoverer;
import org.scijava.ops.api.OpInfo;
import org.scijava.parse2.Parser;

/**
 * Generates {@link OpInfo}s using a {@link TaggedElement}. The tag syntax is
 * expected to be as follows:
 * <p>
 * {@code @implNote op names=<op names, comma delimited> [priority=<op priority>]}
 * <p>
 * For example, an Op wishing to be discoverable with names "foo.bar" and
 * "foo.baz", with a priority of 100, should be declared as
 * <p>
 * {@code @implNote op names=foo.bar,foo.baz priority=100}
 * <p>
 * 
 * @author Gabriel Selzer
 */
public class TherapiOpInfoDiscoverer extends TherapiDiscoverer {

	private static final String TAGTYPE = "op";

	public TherapiOpInfoDiscoverer() {
		super(serviceLoadParser());
	}

	public static Parser serviceLoadParser() {
		Optional<Parser> optional = ServiceLoader.load(Parser.class).findFirst();
		if (optional.isEmpty())
			throw new IllegalArgumentException("Tried to create a TherapiOpInfoDiscoverer without a Parser available!" //
					+ " Ensure a Parser implementation is provided via module!");
		return optional.get();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <U> Optional<U> convert(TaggedElement discovery, Class<U> info) {
		return (Optional<U>) TherapiOpInfoGenerator.infoFrom(discovery);
	}

	@Override
	public boolean canDiscover(Class<?> cls) {
		return cls == OpInfo.class;
	}

	@Override
	public String tagType() {
		return TAGTYPE;
	}

}
