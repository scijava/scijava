
package org.scijava.discovery.plugin;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;

public class PluginDiscovererTest {

	@Test
	public void testPluginDiscovery() {
		Discoverer d = new PluginBasedDiscoverer();
		List<TestPlugin> discoveries = d.discover(TestPlugin.class);
		Assertions.assertTrue(discoveries.stream().anyMatch(o -> o.getClass() == TestPluginImpl.class));
	}

}
