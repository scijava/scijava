package org.scijava.discovery.therapi;

import org.scijava.parse2.Parser;

import java.util.Optional;
import java.util.ServiceLoader;

public class TaggedElementDiscoverer extends TherapiDiscoverer {

	private final String tagType;

	public TaggedElementDiscoverer(String tagType) {
		super(ServiceLoader.load(Parser.class).findFirst().get());
		this.tagType = tagType;
	}

	@Override public boolean canDiscover(Class<?> cls) {
		return cls == TaggedElement.class;
	}

	@Override public String tagType() {
		return tagType;
	}

	@Override protected <U> Optional<U> convert(TaggedElement e, Class<U> c) {
		return (Optional<U>) Optional.of(e);
	}
}
