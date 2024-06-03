/*-
 * #%L
 * A library for building and introspecting structs.
 * %%
 * Copyright (C) 2021 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.scijava.struct;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link org.scijava.struct} classes.
 *
 * @author Curtis Rueden
 * @author Christian Dietz
 */
public class StructTest {

    @Test
    public void testBasicStruct() {
        final Struct p = //
          ParameterStructs.structOf(VariousParameters.class);
        final List<Member<?>> items = p.members();
        assertParam("a", int.class, ItemIO.INPUT, items.get(0));
        assertParam("b", Double.class, ItemIO.INPUT, items.get(1));
        assertParam("c", byte.class, ItemIO.INPUT, items.get(2));
        assertParam("d", Object.class, ItemIO.INPUT, items.get(3));
        assertParam("o", double.class, ItemIO.OUTPUT, items.get(4));
        assertParam("p", String.class, ItemIO.OUTPUT, items.get(5));
    }

    @Test
    public void testFunctionalParameters() {
        final Struct info = //
          ParameterStructs.structOf(TruncAndMultiply.class);
        final List<Member<?>> items = info.members();
        assertEquals(3, items.size());
        assertParam("input", Double.class, ItemIO.INPUT, items.get(0));
        assertParam("result", Long.class, ItemIO.OUTPUT, items.get(1));
        assertParam("multiplier", long.class, ItemIO.INPUT, items.get(2));
    }

    @Test
    public void testStructAccess() {
        final Struct struct = //
          ParameterStructs.structOf(VariousParameters.class);

        final VariousParameters vp = new VariousParameters();
        vp.a = 5;
        vp.b = 3.3;
        vp.c = 2;
        vp.d = "Hello";
        vp.o = 12.3;
        vp.p = "Goodbye";

        final StructInstance<VariousParameters> vpInstance = //
          struct.createInstance(vp);
        assertEquals(5, vpInstance.member("a").get());
        assertEquals(3.3, vpInstance.member("b").get());
        assertEquals((byte) 2, vpInstance.member("c").get());
        assertEquals("Hello", vpInstance.member("d").get());
        assertEquals(12.3, vpInstance.member("o").get());
        assertEquals("Goodbye", vpInstance.member("p").get());

        vpInstance.member("a").set(6);
        assertEquals(6, vp.a);

        vpInstance.member("p").set("Yo");
        assertEquals("Yo", vp.p);
    }

    @Test
    public void testNestedStructs() {
        final Struct hlpStruct = //
          ParameterStructs.structOf(HighLevelParameters.class);

        // check toplevel parameters
        final List<Member<?>> hlpMembers = hlpStruct.members();
        final Member<?> npMember = hlpMembers.get(0);
        assertParam("np", NestedParameters.class, ItemIO.INPUT, npMember);
        assertParam("junk", String.class, ItemIO.INPUT, hlpMembers.get(1));

        // check one level down
        assertTrue(npMember.isStruct());
        final Struct npStruct = npMember.childStruct();
        final List<? extends Member<?>> npMembers = npStruct.members();
        assertParam("stuff", String.class, ItemIO.INPUT, npMembers.get(0));
        final Member<?> vpMember = npMembers.get(1);
        assertParam("vp", VariousParameters.class, ItemIO.INPUT, vpMember);
        assertParam("things", String.class, ItemIO.OUTPUT, npMembers.get(2));

        // check two levels down
        assertTrue(vpMember.isStruct());
        final Struct vpStruct = vpMember.childStruct();
        assertNotNull(vpStruct);
        final List<? extends Member<?>> vpMembers = vpStruct.members();
        assertParam("a", int.class, ItemIO.INPUT, vpMembers.get(0));
        assertParam("b", Double.class, ItemIO.INPUT, vpMembers.get(1));
        assertParam("c", byte.class, ItemIO.INPUT, vpMembers.get(2));
        assertParam("d", Object.class, ItemIO.INPUT, vpMembers.get(3));
        assertParam("o", double.class, ItemIO.OUTPUT, vpMembers.get(4));
        assertParam("p", String.class, ItemIO.OUTPUT, vpMembers.get(5));

        // check nested Structs
        final NestedParameters np = new NestedParameters();
        np.vp = new VariousParameters();
        np.vp.a = 9;
        np.vp.b = 8.7;
        np.vp.c = 6;
        np.vp.d = "asdf";
        np.vp.o = 5.4;
        np.vp.p = "fdsa";
        final Map<String, Object> nestedMembers = new HashMap<>();
        final StructInstance<NestedParameters> npInstance = //
          npStruct.createInstance(np);
        for (final MemberInstance<?> memberInstance : npInstance) {
            if (!memberInstance.member().isStruct()) continue;
            final StructInstance<?> nestedInstance = Structs.expand(memberInstance);
            for (final Member<?> nestedMember : nestedInstance.struct()) {
                final String key = memberInstance.member().key() + "." + nestedMember.key();
                nestedMembers.put(key, nestedInstance.member(nestedMember.key()).get());
            }
        }
        assertEquals(6, nestedMembers.size());
        assertEquals(9, nestedMembers.get("vp.a"));
        assertEquals(8.7, nestedMembers.get("vp.b"));
        assertEquals((byte) 6, nestedMembers.get("vp.c"));
        assertEquals("asdf", nestedMembers.get("vp.d"));
        assertEquals(5.4, nestedMembers.get("vp.o"));
        assertEquals("fdsa", nestedMembers.get("vp.p"));

        var expectedString =
          "np: org.scijava.struct.StructTest$NestedParameters [INPUT]\n" +
            "  stuff: java.lang.String [INPUT]\n" +
            "  vp: org.scijava.struct.StructTest$VariousParameters [INPUT]\n" +
            "    a: int [INPUT]\n" +
            "    b: java.lang.Double [INPUT]\n" +
            "    c: byte [INPUT]\n" +
            "    d: java.lang.Object [INPUT]\n" +
            "    o: double [OUTPUT]\n" +
            "    p: java.lang.String [OUTPUT]\n" +
            "  things: java.lang.String [OUTPUT]\n" +
            "junk: java.lang.String [INPUT]";
        assertEquals(expectedString, Structs.toString(hlpStruct));
    }

    // -- Helper methods --

    private void assertParam(final String key, final Type type,
                             final ItemIO ioType, final Member<?> pMember)
    {
        assertEquals(key, pMember.key());
        assertEquals(type, pMember.type());
        assertSame(ioType, pMember.getIOType());
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

    @Parameter(key = "input")
    @Parameter(type = ItemIO.OUTPUT, key = "result")
    public static class TruncAndMultiply implements Function<Double, Long> {

        @Parameter
        long multiplier;

        @Override
        public Long apply(final Double t) {
            return t.intValue() * multiplier;
        }
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
