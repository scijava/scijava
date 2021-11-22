package org.scijava.discovery.therapi;

import org.scijava.parse2.Parser;

import java.lang.reflect.AnnotatedElement;
import java.util.ServiceLoader;

public class TherapiDiscoveryDiscoverer extends TherapiDiscoverer {

	private final String tagType;

	public TherapiDiscoveryDiscoverer(String tagType) {
		super(ServiceLoader.load(Parser.class).findFirst().get());
		this.tagType = tagType;
	}

	@Override public boolean canDiscover(Class<?> cls) {
		return cls == TherapiDiscovery.class;
	}

	@Override public String tagType() {
		return tagType;
	}

	@Override protected <U> U convert(TherapiDiscovery e, Class<U> c) {
		return (U) e;
	}
}
