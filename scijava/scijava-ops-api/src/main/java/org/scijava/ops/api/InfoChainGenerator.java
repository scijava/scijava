
package org.scijava.ops.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.scijava.plugin.SciJavaPlugin;

public interface InfoChainGenerator extends SciJavaPlugin {

	public static final Character DEP_START_DELIM = '{';
	public static final Character DEP_END_DELIM = '}';

	/**
	 * Generates an {@link InfoChain}. This {@link InfoChainGenerator} is only
	 * responsible for generating the <b>outer layer</b> of the {@link InfoChain},
	 * and delegates to {@code generators} to
	 * 
	 * @param signature the signature for which an {@link InfoChainGenerator} must be generated
	 * @param idMap the available {@link OpInfo}s, keyed by their id
	 * @param generators the available {@link InfoChainGenerator}s
	 * @return an {@link InfoChain} matching the specifications of
	 *         {@code signature}
	 */
	InfoChain generate(String signature, Map<String, OpInfo> idMap,
		Collection<InfoChainGenerator> generators);

	/**
	 * Filters through a list of {@link InfoChainGenerator}s to find the generator
	 * best suited towards generating {@code signature}
	 * <p>
	 * 
	 * @param signature the signature that must be generated
	 * @param generators the list of {@link InfoChainGenerator}s
	 * @return the {@link InfoChainGenerator} best suited to the task
	 */
	static Optional<InfoChainGenerator> findSuitableGenerator(String signature,
		Collection<InfoChainGenerator> generators)
	{
		return generators.stream() //
			.filter(g -> g.canGenerate(signature)) //
			.max((i1, i2) -> (int) (i1.priority() - i2.priority()));
	}

	/**
	 * Describes whether this {@link InfoChainGenerator} is designed to generate
	 * the <b>outer layer</b> of the {@link InfoChain}
	 *
	 * @param signature the signature to use as the template for the
	 *          {@link InfoChain}
	 * @return true iff this {@link InfoChainGenerator} can generate the outer
	 *         layer of the {@link InfoChain}
	 */
	boolean canGenerate(String signature);

	/**
	 * Returns the priority of this {@link InfoChainGenerator}
	 * @return the priority
	 */
	double priority();

}
