package org.scijava.ops.serviceloader;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.scijava.ops.discovery.Discoverer;

public class ServiceLoaderDiscoverer implements Discoverer {

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<Class<T>> implementingClasses(Class<T> c) {
		ServiceLoader<T> loader = ServiceLoader.load(c);
		return loader.stream().map(p -> (Class<T>) p.get().getClass()) //
				.collect(Collectors.toList());
	}

}
