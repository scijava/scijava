package org.scijava.ops.api;

import java.lang.reflect.Type;
import java.util.UUID;

public class OpMetadata {

	private final Type type;
	private final OpInfo info;
	private final UUID executionID;
	private final Hints hints;
	private final OpHistory history;

	public OpMetadata(Type type, OpInfo info, UUID executionID,
		Hints hints, OpHistory history)
	{
		this.type = type;
		this.history = history;
		this.info = info;
		this.executionID = executionID;
		this.hints = hints;
	}

	public OpHistory history() {
		return history;
	}

	public OpInfo info() {
		return info;
	}

	public Hints hints() {
		return hints;
	}

	public UUID executionID() {
		return executionID;
	}

	public Type type() {
		return type;
	}
}
