
package org.scijava.ops.engine.matcher.reduce;

import org.scijava.ops.engine.impl.DefaultInfoTreeGenerator;

/**
 * An {@link org.scijava.ops.engine.InfoTreeGenerator} that is used to generate
 * {@link org.scijava.ops.api.InfoTree}s for {@link ReducedOpInfo}s.
 * <p>
 * This implementation is based on the fact that {@link ReducedOpInfo}s are
 * treated like other "default" {@link org.scijava.ops.api.OpInfo}s, such that
 * they will be in the {@code idMap} passed to {@link #generate}. Using that
 * idea, we simply extend {@link DefaultInfoTreeGenerator}, with a slighly
 * different {@link #canGenerate(String)}. If {@code idMap} is rewritten to no
 * longer contain all {@link ReducedOpInfo}s, this implementation should change
 * as well.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class ReducedInfoTreeGenerator extends DefaultInfoTreeGenerator {

	@Override
	public boolean canGenerate(String signature) {
		return signature.startsWith(ReducedOpInfo.IMPL_DECLARATION);
	}
}
