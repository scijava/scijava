
package org.scijava.ops.hints;

import java.util.Map;

/**
 * A basic interface for storing and accessing Hints.
 *
 * @author Gabriel Selzer
 */
public interface Hints {

	public String setHint(String hint);

	public String getHint(String hintType);

	public boolean containsHint(String hint);

	public boolean containsHintType(String hintType);

	public Map<String, String> getHints();

	public Hints getCopy();

}
