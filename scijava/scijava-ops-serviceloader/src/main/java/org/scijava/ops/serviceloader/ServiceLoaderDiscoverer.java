
package org.scijava.ops.serviceloader;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.scijava.discovery.Discoverer;
import org.scijava.discovery.Discovery;

public class ServiceLoaderDiscoverer implements Discoverer {

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<Discovery<Class<T>>> discoveriesOfType(Class<T> c) {
		// If we cannot use c, we cannot find any implementations
		Module thisModule = this.getClass().getModule();
		if (!thisModule.canUse(c)) return Collections.emptyList();

		// If we can use c, look up the implementations
		ServiceLoader<T> loader = ServiceLoader.load(c);
		return loader.stream().map(p -> formDiscovery(c, (Class<T>) p.get().getClass())) //
			.collect(Collectors.toList());
	}

	private <T> Discovery<Class<T>> formDiscovery(Class<T> c, Class<T> impl) {
		String tagType = c.getTypeName().toLowerCase();
		return new Discovery<>(impl, tagType);
	}

}
