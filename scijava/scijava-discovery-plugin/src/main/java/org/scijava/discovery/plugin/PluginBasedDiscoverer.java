package org.scijava.discovery.plugin;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.scijava.InstantiableException;
import org.scijava.discovery.Discoverer;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginIndex;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.SciJavaPlugin;

public class PluginBasedDiscoverer implements Discoverer {

	private final PluginIndex index;

	public PluginBasedDiscoverer() {
		this.index = new PluginIndex();
		this.index.discover();
	}

	private String getTag(Plugin annotation) {
		String tagType = annotation.type().getSimpleName().toLowerCase();
		String priority = "priority='" + annotation.priority() + "'";
		return String.join(" ", tagType, priority);
	}

	@Override
	public <T> List<T> discover(Class<T> c) {
		if (!SciJavaPlugin.class.isAssignableFrom(c))
			return Collections.emptyList();
		return discoverFrom((Class<? extends SciJavaPlugin>) c).stream().map(o -> (T) o).collect(Collectors.toList());
	}

	private <PT extends SciJavaPlugin> List<PT> discoverFrom(Class<PT> c) {
		List<PluginInfo<PT>> infos = index.getPlugins(c);
		return infos.parallelStream().map(info -> {
			try {
				return info.createInstance();
			} catch (InstantiableException e) {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}
}
