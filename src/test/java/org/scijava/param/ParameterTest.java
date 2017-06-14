
package org.scijava.param;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.scijava.ItemIO;
import org.scijava.ValidityException;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInfo;
import org.scijava.struct.StructItem;

/**
 * Tests {@link org.scijava.param} classes.
 *
 * @author Curtis Rueden
 * @author Christian Dietz
 */
public class ParameterTest {

	@Test
	public void testNested() throws ValidityException {
		final StructInfo<ParameterItem<?>> hlpInfo = //
			ParameterStructs.infoOf(HighLevelParameters.class);

		// check toplevel parameters
		final List<ParameterItem<?>> hlpItems = hlpInfo.items();
		final ParameterItem<?> npItem = hlpItems.get(0);
		assertParam("np", NestedParameters.class, ItemIO.INPUT, npItem);
		assertParam("junk", String.class, ItemIO.INPUT, hlpItems.get(1));

		// check one level down
		assertTrue(npItem.isStruct());
		final StructInfo<? extends StructItem<?>> npInfo = npItem.childInfo();
		final List<? extends StructItem<?>> npItems = npInfo.items();
		assertParam("stuff", String.class, ItemIO.INPUT, npItems.get(0));
		final StructItem<?> vpItem = npItems.get(1);
		assertParam("vp", VariousParameters.class, ItemIO.INPUT, vpItem);
		assertParam("things", String.class, ItemIO.OUTPUT, npItems.get(2));

		// check two levels down
		assertTrue(vpItem.isStruct());
		final StructInfo<? extends StructItem<?>> vpInfo = vpItem.childInfo();
		assertNotNull(vpInfo);
		final List<? extends StructItem<?>> vpItems = vpInfo.items();
		assertParam("a", int.class, ItemIO.INPUT, vpItems.get(0));
		assertParam("b", Double.class, ItemIO.INPUT, vpItems.get(1));
		assertParam("c", byte.class, ItemIO.INPUT, vpItems.get(2));
		assertParam("d", Object.class, ItemIO.INPUT, vpItems.get(3));
		assertParam("o", double.class, ItemIO.OUTPUT, vpItems.get(4));
		assertParam("p", String.class, ItemIO.OUTPUT, vpItems.get(5));

		// check nested Structs
		final NestedParameters np = new NestedParameters();
		np.vp = new VariousParameters();
		np.vp.a = 9;
		np.vp.b = 8.7;
		np.vp.c = 6;
		np.vp.d = "asdf";
		np.vp.o = 5.4;
		np.vp.p = "fdsa";
		final Map<String, Object> nestedItems = new HashMap<>();
		final Struct<NestedParameters> npStruct = new Struct<>(npInfo, np);
		for (final StructItem<?> item : npInfo) {
			if (!item.isStruct()) continue;
			final StructInfo<? extends StructItem<?>> nestedInfo = vpItem.childInfo();
//			final Struct<?> nested = nestedInfo.structOf(npStruct.get(item));
			final Struct<?> nested = //
				new Struct<>(nestedInfo, npStruct.get(item.getKey()));
			for (final StructItem<?> nestedItem : nested.info()) {
				final String key = item.getKey() + "." + nestedItem.getKey();
				nestedItems.put(key, nested.get(nestedItem.getKey()));
			}
		}
		assertEquals(6, nestedItems.size());
		assertEquals(9, nestedItems.get("vp.a"));
		assertEquals(8.7, nestedItems.get("vp.b"));
		assertEquals((byte) 6, nestedItems.get("vp.c"));
		assertEquals("asdf", nestedItems.get("vp.d"));
		assertEquals(5.4, nestedItems.get("vp.o"));
		assertEquals("fdsa", nestedItems.get("vp.p"));
	}

	@Test
	public void testBijection() throws ValidityException {
		final StructInfo<ParameterItem<?>> info = //
			ParameterStructs.infoOf(VariousParameters.class);

		final VariousParameters vp = new VariousParameters();
		vp.a = 5;
		vp.b = 3.3;
		vp.c = 2;
		vp.d = "Hello";
		vp.o = 12.3;
		vp.p = "Goodbye";

		final Struct<VariousParameters> struct = new Struct<>(info, vp);
		assertEquals(5, struct.get("a"));
		assertEquals(3.3, struct.get("b"));
		assertEquals((byte) 2, struct.get("c"));
		assertEquals("Hello", struct.get("d"));
		assertEquals(12.3, struct.get("o"));
		assertEquals("Goodbye", struct.get("p"));

		struct.set("a", 6);
		assertEquals(6, vp.a);

		struct.set("p", "Yo");
		assertEquals("Yo", vp.p);
	}

	@Test
	public void testInfo() throws ValidityException {
		final StructInfo<ParameterItem<?>> p = //
			ParameterStructs.infoOf(VariousParameters.class);
		final List<ParameterItem<?>> items = p.items();
		assertParam("a", int.class, ItemIO.INPUT, items.get(0));
		assertParam("b", Double.class, ItemIO.INPUT, items.get(1));
		assertParam("c", byte.class, ItemIO.INPUT, items.get(2));
		assertParam("d", Object.class, ItemIO.INPUT, items.get(3));
		assertParam("o", double.class, ItemIO.OUTPUT, items.get(4));
		assertParam("p", String.class, ItemIO.OUTPUT, items.get(5));
	}

	// -- Helper methods --

	private void assertParam(final String key, final Type type,
		final ItemIO ioType, final StructItem<?> pInfo)
	{
		assertEquals(key, pInfo.getKey());
		assertEquals(type, pInfo.getType());
		assertSame(ioType, pInfo.getIOType());
	}

	// -- Helper classes --

	public static class VariousParameters {

		@Parameter
		public int a;
		@Parameter
		public Double b;
		@Parameter
		public byte c;
		@Parameter
		public Object d;
		@Parameter(type = ItemIO.OUTPUT)
		public double o;
		@Parameter(type = ItemIO.OUTPUT)
		public String p;

		public void go() {}
	}

	public static class NestedParameters {

		@Parameter
		private String stuff;

		@Parameter(struct = true)
		private VariousParameters vp;

		@Parameter(type = ItemIO.OUTPUT)
		private String things;

		public void win() {
			things = "VfB Stuttgart";
		}
	}
	
	public static class HighLevelParameters {
		@Parameter(struct = true)
		private NestedParameters np;
		
		@Parameter
		private String junk;
	}
}

///////////

/* HOW THIS MIGHT WORK IN A SCRIPT
-----------------------
#@double sigma
#@advanced {
	double minCutoff
	double maxCutoff
}
#@VariousParameter(structured = true) vp
#@OUTPUT String result
-----------------------

StructInfo<StructItem<?>> scriptInfo = scriptService.getScriptInfo("awesome.groovy");

StructItem<?> advancedItem = scriptInfo.items().get(1);
StructItem<?> vpItem = scriptInfo.items().get(2);

assertTrue(advancedItem instanceof StructInfo)
assertTrue(vpItem instanceof StructInfo)
*/
