
package org.scijava.ops.engine;

import java.util.Collections;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.function.Producer;
import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

/**
 * Tests the functionality of {@link InfoChain} instantiation.
 *
 * @author Gabriel Selzer
 */
public class InfoChainTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeClass
	public static void addNeededOps() {
		discoverer.register("opcollection", new InfoChainTest());
		discoverer.register("op", new ComplexOp());
	}

	public static final String S = "this Op is cool";

	@OpField(names = "test.infoChain")
	public final Producer<String> foo = () -> S;

	@Test
	public void testInfoChainInstantiation() {
		OpInfo info = singularInfoOfName("test.infoChain");
		InfoChain chain = new InfoChain(info);
		Nil<Producer<String>> nil = new Nil<>() {};
		Producer<String> op = ops.opFromInfoChain(chain, nil);
		Assert.assertEquals(S, op.create());
	}

	@Test
	public void testInfoChainWithDependenciesInstantiation() {
		// Find dependency
		OpInfo info = singularInfoOfName("test.infoChain");
		InfoChain dependencyChain = new InfoChain(info);

		// Find dependent Op
		OpInfo baseInfo = singularInfoOfName("test.infoChainBase");
		InfoChain chain = new InfoChain(baseInfo, Collections.singletonList(
			dependencyChain));

		Nil<Producer<String>> nil = new Nil<>() {};
		Producer<String> op = ops.opFromInfoChain(chain, nil);
		Assert.assertEquals(S, op.create());
	}

	private OpInfo singularInfoOfName(String name) {
		Iterator<OpInfo> infos = ops.infos(name).iterator();
		Assert.assertTrue(infos.hasNext());
		OpInfo info = infos.next();
		Assert.assertFalse(infos.hasNext());
		return info;
	}

}

@OpClass(names = "test.infoChainBase")
class ComplexOp implements Producer<String>, Op {

	@OpDependency(name = "test.infoChain")
	private Producer<String> op;

	@Override
	public String create() {
		return op.create();
	}

}
