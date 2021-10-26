package org.scijava.ops.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.scijava.Context;
import org.scijava.cache.CacheService;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.StaticDiscoverer;
import org.scijava.log2.Logger;
import org.scijava.log2.StderrLogFactory;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.features.MatchingRoutine;
import org.scijava.ops.engine.impl.DefaultOpEnvironment;
import org.scijava.ops.engine.impl.DefaultOpHistory;
import org.scijava.ops.engine.impl.OpClassBasedClassOpInfoGenerator;
import org.scijava.ops.engine.impl.OpCollectionInfoGenerator;
import org.scijava.ops.engine.impl.PluginBasedClassOpInfoGenerator;
import org.scijava.ops.engine.impl.TagBasedOpInfoGenerator;
import org.scijava.ops.engine.matcher.impl.OpWrappers;
import org.scijava.ops.engine.matcher.impl.RuntimeSafeMatchingRoutine;
import org.scijava.parse2.Parser;
import org.scijava.plugin.PluginService;
import org.scijava.thread.ThreadService;
import org.scijava.types.DefaultTypeReifier;
import org.scijava.types.TypeReifier;

public abstract class AbstractTestEnvironment {

	protected static Context context;
	protected static OpService ops;
	protected static PluginService plugins;
	protected static Logger log;

	@BeforeClass
	public static void setUp() {
		context = new Context(OpService.class, CacheService.class, ThreadService.class, Parser.class, PluginService.class);
		ops = context.getService(OpService.class);
		plugins = context.getService(PluginService.class);
		log = new StderrLogFactory().create();
	}

	@AfterClass
	public static void tearDown() {
		context.dispose();
		context = null;
		plugins = null;
		log = null;
	}

	protected static OpEnvironment barebonesEnvironment(Class<?>... opClasses) {
		List<Class<? extends MatchingRoutine>> routines = Arrays.asList(RuntimeSafeMatchingRoutine.class);
		return barebonesEnvironment(routines, opClasses);
	}

	protected static OpEnvironment barebonesEnvironment(List<Class<? extends MatchingRoutine>> routines, Class<?>... opClasses) {
		return barebonesEnvironment(routines, Arrays.asList(opClasses));
	}

	protected static OpEnvironment barebonesEnvironment(List<Class<? extends MatchingRoutine>> routines, List<Class<?>> opClasses) {
		TypeReifier t = new DefaultTypeReifier(log, Discoverer.using(ServiceLoader::load));
		StaticDiscoverer d = new StaticDiscoverer();
		d.registerAll(routines, "matchingroutine");
		d.registerAll(OpWrappers.class.getDeclaredClasses(), "opwrapper");
		d.registerAll(opClasses, "");
		List<OpInfoGenerator> generators = new ArrayList<>();
		generators.add( new OpCollectionInfoGenerator(log, d));
		generators.add( new OpClassBasedClassOpInfoGenerator(log, d));
		generators.add( new PluginBasedClassOpInfoGenerator(log, d));
		generators.add( new TagBasedOpInfoGenerator(log, d));
		
		return new DefaultOpEnvironment(t, log, new DefaultOpHistory(), generators, d);
	}

	protected static OpEnvironment fullEnvironment() {
		return context.getService(OpService.class).env();
	}
	
	protected static boolean arrayEquals(double[] arr1, Double... arr2) {
		return Arrays.deepEquals(Arrays.stream(arr1).boxed().toArray(Double[]::new), arr2);
	}

	protected static <T> void assertIterationsEqual(final Iterable<T> expected,
		final Iterable<T> actual)
	{
		final Iterator<T> e = expected.iterator();
		final Iterator<T> a = actual.iterator();
		while (e.hasNext()) {
			assertTrue("Fewer elements than expected", a.hasNext());
			assertEquals(e.next(), a.next());
		}
		assertFalse("More elements than expected", a.hasNext());
	}
}
