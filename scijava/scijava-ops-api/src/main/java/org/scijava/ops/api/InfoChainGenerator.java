
package org.scijava.ops.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
	static InfoChainGenerator findSuitableGenerator(String signature,
		Collection<InfoChainGenerator> generators)
	{
		List<InfoChainGenerator> suitableGenerators = generators.stream() //
			.filter(g -> g.canGenerate(signature)) //
			.collect(Collectors.toList());
		if (suitableGenerators.size() == 0) throw new IllegalArgumentException(
			"No InfoChainGenerator in given collection " + generators +
				" is able to reify signature " + signature);
		if (suitableGenerators.size() > 1) throw new IllegalArgumentException(
			"Signature " + signature +
				" is able to be reified by multiple generators: " + suitableGenerators);
		return suitableGenerators.get(0);
	}

	static InfoChain generateDependencyChain(String subsignature,
		Map<String, OpInfo> idMap, Collection<InfoChainGenerator> generators)
	{
		InfoChainGenerator genOpt = InfoChainGenerator
			.findSuitableGenerator(subsignature, generators);
		return genOpt.generate(subsignature, idMap, generators);
	}

	/**
	 * Finds the subsignature in {@link String} {@code signature}. The
	 * subsignature is assumed to start at index {@code start}.
	 * 
	 * @param signature
	 * @param start
	 * @return a signature contained withing {@code signature}
	 */
	static String subSignatureFrom(String signature, int start) {
		int depsStart = signature.indexOf(DEP_START_DELIM, start);
		int depth = 0;
		for (int i = depsStart; i < signature.length(); i++) {
			char ch = signature.charAt(i);
			if (ch == DEP_START_DELIM) depth++;
			else if (ch == DEP_END_DELIM) {
				depth--;
				if (depth == 0) {
					int depsEnd = i;
					return signature.substring(start, depsEnd + 1);
				}
			}
		}
		throw new IllegalArgumentException(
			"There is no complete signature starting from index " + start +
				" in signature " + signature);
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

}
