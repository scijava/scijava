package org.scijava.discovery.plugin;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.Discovery;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;

public class PluginBasedDiscoverer implements Discoverer {

	private final PluginService p;

	public PluginBasedDiscoverer(PluginService p) {
		this.p = p;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<Discovery<Class<T>>> discoveriesOfType(Class<T> c) {
		if (!SciJavaPlugin.class.isAssignableFrom(c)) {
			throw new UnsupportedOperationException(
				"Current discovery mechanism tied to SciJava Context; only able to search for SciJavaPlugins");
		}
		List<PluginInfo<SciJavaPlugin>> infos = p.getPluginsOfType(
			(Class<SciJavaPlugin>) c);
		return infos.stream() //
			.map(info -> makeDiscoveryOrNull(c, info)) //
			.filter(Objects::nonNull).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private <T> Discovery<Class<T>> makeDiscoveryOrNull(@SuppressWarnings("unused") Class<T> type,
		PluginInfo<SciJavaPlugin> instance)
	{
		try {
			Class<T> c = (Class<T>) instance.loadClass();
			String tag = getTag(instance.getAnnotation());
			return new Discovery<>(c, tag);
		}
		catch (InstantiableException exc) {
			return null;
		}
	}

	private String getTag(Plugin annotation) {
		String tagType = annotation.type().getTypeName().toLowerCase();
		String priority = "priority " + annotation.priority();
		return String.join(" ", tagType, priority);
	}

}