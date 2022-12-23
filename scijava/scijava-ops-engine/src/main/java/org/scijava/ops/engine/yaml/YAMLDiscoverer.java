
package org.scijava.ops.engine.yaml;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.OpInfo;
import org.yaml.snakeyaml.Yaml;

public class YAMLDiscoverer implements Discoverer {

	private final Yaml yaml = new Yaml();

	private final List<YAMLOpInfoCreator> creators = Discoverer.using(
		ServiceLoader::load).discover(YAMLOpInfoCreator.class);

	@SuppressWarnings("unchecked")
	@Override
	public <U> List<U> discover(Class<U> c) {
		// TODO: Consider supertypes
		if (!c.equals(OpInfo.class)) return Collections.emptyList();

		try {
			Enumeration<URL> opFiles = Thread.currentThread().getContextClassLoader()
				.getResources("ops.yaml");
			List<OpInfo> infos = new ArrayList<>();
			while (opFiles.hasMoreElements()) {
				parse(infos, opFiles.nextElement());
			}
			return (List<U>) infos;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void parse(List<OpInfo> infos, final URL url) throws IOException {
		Map<String, Object> yamlData = yaml.load(url.openStream());

		Map<String, Object> yamlInfo = subMap(yamlData, "info");
		String version = value(yamlInfo, "version");
		String source = value(yamlInfo, "source");

		for ( //
		Map<String, Object> op : //
		(List<Map<String, Object>>) yamlData.get("ops")) //
		{
			Map<String, Object> opData = subMap(op, "op");
			String identifier = value(opData, "source");
			try {
				Optional<YAMLOpInfoCreator> c = creators.stream() //
					.filter(f -> f.canCreateFrom(source, identifier)) //
					.findFirst();
				if (c.isPresent()) infos.add(c.get().create(opData, version));
			}
			catch (Exception e) {
				// TODO: Use SciJava Log2's Logger to notify the user.
				// See https://github.com/scijava/scijava/issues/106 for discussion
				// and progress
				System.out.println("Could not add op " + identifier + ":");
				e.printStackTrace();
			}
		}
	}

	private static Map<String, Object> subMap(final Map<String, Object> map,
		String key)
	{
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException("YAML map " + map +
				" does not contain key " + key);
		}
		Object value = map.get(key);
		if (!(value instanceof Map)) {
			throw new IllegalArgumentException("YAML map " + map +
				" has a non-map value for key " + key);
		}
		return (Map<String, Object>) value;
	}

	private static String value(final Map<String, Object> map, String key) {
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException("YAML map " + map +
				" does not contain key " + key);
		}
		Object value = map.get(key);
		if (!(value instanceof String)) {
			throw new IllegalArgumentException("YAML map " + map +
				" has a non-string value for key " + key);
		}
		return (String) value;
	}

}
