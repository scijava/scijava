package org.scijava.ops.engine.struct;

import java.util.Map;

import org.scijava.struct.FunctionalMethodType;

public class MethodParamInfo {
	private final Map<FunctionalMethodType, String> fmtNames;
	private final Map<FunctionalMethodType, String> fmtDescriptions;

	public MethodParamInfo(final Map<FunctionalMethodType, String> fmtNames,
		final Map<FunctionalMethodType, String> fmtDescriptions)
	{
		this.fmtNames = fmtNames;
		this.fmtDescriptions = fmtDescriptions;
	}

	public String name(FunctionalMethodType fmt) {
		return fmtNames.get(fmt);
	}

	public String description(FunctionalMethodType fmt) {
		return fmtDescriptions.get(fmt);
	}
}
