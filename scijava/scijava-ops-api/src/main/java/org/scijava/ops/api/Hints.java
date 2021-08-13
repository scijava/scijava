
package org.scijava.ops.api;

import java.util.Deque;
import java.util.Map;
import java.util.UUID;

/**
 * A basic interface for storing and accessing Hints. The general structure for
 * a Hint is
 * <p>
 * {@code hint = hintType.option}
 * <p>
 * <ul>
 * <li>{@code hintType} designates the category of hint
 * <li>{@code option} designates the preference within the category
 * <li>{@code hint} is the combination of {@code hintType} and {@code option}
 * with a delimiting {@code .}
 * </ul>
 * <p>
 * For example, you might want a {@code hintType} to designate preferences on
 * lossiness (as a tradeoff for performance). That {@code hintType} might be
 * {@code Lossiness}, with {@option}s {@code LOSSLESS} and {@LOSSY}.
 * 
 * @author Gabriel Selzer
 */
public interface Hints {

	public String setHint(String hint);

	public String getHint(String hintType);

	public boolean containsHint(String hint);

	public boolean containsHintType(String hintType);

	public Map<String, String> getHints();

	/**
	 * Generates a new {@link Hints} with identical hints but <b>the same</b>
	 * {@link UUID}.
	 * 
	 * @return a new {@link Hints} Object with the same hints as this
	 *         {@link Hints}
	 */
	public Hints copy();

	/**
	 * Generates a new {@link Hints} with identical hints, and a <b>random</b>
	 * {@link UUID}
	 * 
	 * @return a new {@link Hints} Object with the same hints as this
	 *         {@link Hints}
	 */
	public Hints copyRandomUUID();

	/**
	 * Returns the {@link UUID} uniquely identifying the associated
	 * {@link Deque} in the {@link OpHistory}
	 * 
	 * @return the {@link UUID} corresponding to the execution chain (logged
	 *         within the {@link OpHistory}) in which these {@link Hints} are
	 *         being used.
	 */
	public UUID executionChainID();

}
