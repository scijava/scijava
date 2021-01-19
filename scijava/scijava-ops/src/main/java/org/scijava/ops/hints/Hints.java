
package org.scijava.ops.hints;

public interface Hints {

	public static final String BASE = "hints";

	public String setHint(String hint);

	public String getHint(String hintType);

	public boolean isActive(String hint);

	boolean containsHintType(String hintType);

}
