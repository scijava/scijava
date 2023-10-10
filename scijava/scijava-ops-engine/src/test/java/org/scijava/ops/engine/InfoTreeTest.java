
package org.scijava.ops.engine;

import java.util.Collections;
import java.util.Iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Producer;
import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

/**
 * Tests the functionality of {@link InfoTree} instantiation.
 *
 * @author Gabriel Selzer
 */
public class InfoTreeTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new InfoTreeTest());
		ops.register(new ComplexOp());
	}

	public static final String S = "this Op is cool";

	@OpField(names = "test.infoTree")
	public final Producer<String> foo = () -> S;

	@Test
	public void testInfoChainInstantiation() {
		OpInfo info = singularInfoOfName("test.infoTree");
		InfoTree tree = new InfoTree(info);
		Nil<Producer<String>> nil = new Nil<>() {};
		Producer<String> op = ops.opFromInfoChain(tree, nil);
		Assertions.assertEquals(S, op.create());
	}

	@Test
	public void testInfoChainWithDependenciesInstantiation() {
		// Find dependency
		OpInfo info = singularInfoOfName("test.infoTree");
		InfoTree dependencyChain = new InfoTree(info);

		// Find dependent Op
		OpInfo baseInfo = singularInfoOfName("test.infoTreeBase");
		InfoTree tree = new InfoTree(baseInfo, Collections.singletonList(
			dependencyChain));

		Nil<Producer<String>> nil = new Nil<>() {};
		Producer<String> op = ops.opFromInfoChain(tree, nil);
		Assertions.assertEquals(S, op.create());
	}

	private OpInfo singularInfoOfName(String name) {
		Iterator<OpInfo> infos = ops.infos(name).iterator();
		Assertions.assertTrue(infos.hasNext());
		OpInfo info = infos.next();
		Assertions.assertFalse(infos.hasNext());
		return info;
	}

}

@OpClass(names = "test.infoTreeBase")
class ComplexOp implements Producer<String>, Op {

	@OpDependency(name = "test.infoTree")
	private Producer<String> op;

	@Override
	public String create() {
		return op.create();
	}

}
