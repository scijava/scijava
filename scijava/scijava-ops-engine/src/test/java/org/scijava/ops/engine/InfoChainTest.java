
package org.scijava.ops.engine;

import java.util.Collections;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.function.Producer;
import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;

/**
 * Tests the functionality of {@link InfoChain} instantiation.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class InfoChainTest extends AbstractTestEnvironment {

	public static final String S = "this Op is cool";

	@OpField(names = "test.infoChain")
	public final Producer<String> foo = () -> S;

	@Test
	public void testInfoChainInstantiation() {
		Iterator<OpInfo> infos = ops.env().infos("test.infoChain").iterator();
		Assert.assertTrue(infos.hasNext());
		OpInfo info = infos.next();
		Assert.assertFalse(infos.hasNext());
		InfoChain chain = new InfoChain(info);
		Nil<Producer<String>> nil = new Nil<>() {};
		Producer<String> op = ops.env().opFromInfoChain(chain, nil);
		Assert.assertEquals(S, op.create());
	}

	@Test
	public void testInfoChainWithDependenciesInstantiation() {
		// Find dependency
		Iterator<OpInfo> infos = ops.env().infos("test.infoChain").iterator();
		Assert.assertTrue(infos.hasNext());
		OpInfo info = infos.next();
		Assert.assertFalse(infos.hasNext());
		InfoChain dependencyChain = new InfoChain(info);

		// Find dependent Op
		infos = ops.env().infos("test.infoChainBase").iterator();
		Assert.assertTrue(infos.hasNext());
		info = infos.next();
		Assert.assertFalse(infos.hasNext());
		InfoChain chain = new InfoChain(info, Collections.singletonList(
			dependencyChain));

		Nil<Producer<String>> nil = new Nil<>() {};
		Producer<String> op = ops.env().opFromInfoChain(chain, nil);
		Assert.assertEquals(S, op.create());
	}

}

@Plugin(type = Op.class, name = "test.infoChainBase")
class ComplexOp implements Producer<String> {

	@OpDependency(name = "test.infoChain")
	private Producer<String> op;

	@Override
	public String create() {
		return op.create();
	}

}
