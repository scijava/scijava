package org.scijava.ops.engine.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.discovery.StaticDiscoverer;
import org.scijava.discovery.plugin.PluginBasedDiscoverer;
import org.scijava.log2.LogService;
import org.scijava.log2.Logger;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.engine.impl.DefaultOpEnvironment;
import org.scijava.ops.engine.impl.OpClassBasedClassOpInfoGenerator;
import org.scijava.ops.engine.impl.OpCollectionInfoGenerator;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.PluginService;
import org.scijava.types.DefaultTypeReifier;
import org.scijava.types.TypeReifier;

public class ExplicitOpEnvironmentTest implements OpCollection {

	protected static Context ctx;
	protected static Logger log;
	protected static PluginService plugins;

	@BeforeClass
	public static void setUp() {
		ctx = new Context(LogService.class, PluginService.class);
		log = ctx.getService(LogService.class);
		plugins = ctx.getService(PluginService.class);
	}

	@AfterClass
	public static void tearDown() {
		
	}

	private static OpEnvironment makeEnvironment(Class<?>... opClasses) {
		TypeReifier t = new DefaultTypeReifier(log, new PluginBasedDiscoverer(plugins));
		StaticDiscoverer d = new StaticDiscoverer();
		for(Class<?> c : opClasses) {
			d.register(c, "");
		}
		List<OpInfoGenerator> generators = new ArrayList<>();
		generators.add( new OpCollectionInfoGenerator(d));
		generators.add( new OpClassBasedClassOpInfoGenerator(d));
		
		return new DefaultOpEnvironment(t, log, generators, d);
	}

	@Test
	public void basicExplicitOpEnvironmentTest() {
		OpEnvironment env = makeEnvironment(this.getClass());
	}

	@OpField(names = "test.explicitOpEnv")
	public final Function<Integer, Integer> foo = (in) -> in + 1;

}
