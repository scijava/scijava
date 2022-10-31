package org.scijava.ops.api;

import java.lang.reflect.Type;

/**
 * Container for metadata pertaining to an Op <b>instance</b>. This data is
 * wrapped up with the Op instance into a {@link RichOp}
 *
 * @author Gabriel Selzer
 * @see RichOp#metadata()
 */
public class OpMetadata {

	/* The functional Type of the Op */
	private final Type type;
	/* The hierarchy of OpInfos used to make the Op */
	private final InfoChain info;
	/* The set of Hints used to in the initial Op call */
	private Hints hints;
	/* The OpHistory used by the Op */
	private final OpHistory history;

	public OpMetadata(Type type, InfoChain info, Hints hints, OpHistory history) {
		this.type = type;
		this.history = history;
		this.info = info;
		this.hints = hints;
	}

	/**
	 * Gets the functional {@link Type} of the Op
	 * 
	 * @return the functional {@link Type} of the Op
	 */
	public Type type() {
		return type;
	}

	/**
	 * Gets the hierarchy of {@link OpInfo}s used to make the Op
	 * 
	 * @return the hierarchy of {@link OpInfo}s used to make the Op
	 */
	public InfoChain info() {
		return info;
	}

	/**
	 * Gets the {@link Hints} used in the initial matcher call
	 * 
	 * @return the {@link Hints} used in the initial matcher call
	 */
	public Hints hints() {
		return hints;
	}

	/**
	 * Gets the {@link OpHistory} used by this Op
	 * 
	 * @return the {@link OpHistory} used by this Op
	 */
	public OpHistory history() {
		return history;
	}

	/**
	 * Sets the {@link Hints} for this Op
	 *
	 * @param hints New {@link Hints} to use for this Op
	 */
	public void setHints(Hints hints) {
		this.hints = hints;
	}
}
