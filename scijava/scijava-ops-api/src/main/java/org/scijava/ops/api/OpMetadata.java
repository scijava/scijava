package org.scijava.ops.api;

import java.lang.reflect.Type;

public class OpMetadata {

	private final Type type;
	private final InfoChain info;
	private final Hints hints;
	private final OpHistory history;

	public OpMetadata(Type type, InfoChain info,
		Hints hints, OpHistory history)
	{
		this.type = type;
		this.history = history;
		this.info = info;
		this.hints = hints;
	}

	public OpHistory history() {
		return history;
	}

	public InfoChain info() {
		return info;
	}

	public Hints hints() {
		return hints;
	}

	public Type type() {
		return type;
	}
}
