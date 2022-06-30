
package org.scijava.discovery.plugin;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.discovery.Discoverer;
import org.scijava.plugin.PluginService;

public class PluginDiscovererTest {

	private static Context ctx;
	private static PluginService plugins;

	@BeforeClass
	public static void setUp() {
		ctx = new Context(PluginService.class);
		plugins = ctx.getService(PluginService.class);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		ctx.close();
		plugins = null;
	}

	@Test
	public void testPluginDiscovery() {
		Discoverer d = new PluginBasedDiscoverer();
		List<TestPlugin> discoveries = d.discover(TestPlugin.class);
		Assert.assertTrue(discoveries.stream().anyMatch(o -> o.getClass() == TestPluginImpl.class));
	}

}
