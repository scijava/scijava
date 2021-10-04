
package org.scijava.ops.serviceloader;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.scijava.discovery.Discoverer;

public class ServiceLoaderDiscoverer implements Discoverer {

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<Class<T>> implsOfType(Class<T> c) {
		// If we cannot use c, we cannot find any implementations
		Module thisModule = this.getClass().getModule();
		if (!thisModule.canUse(c)) return Collections.emptyList();

		// If we can use c, look up the implementations
		ServiceLoader<T> loader = ServiceLoader.load(c);
		return loader.stream().map(p -> (Class<T>) p.get().getClass()) //
			.collect(Collectors.toList());
	}

}
