
package org.scijava.ops.engine.struct;

import java.util.Collection;
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

	public boolean containsAll(Collection<FunctionalMethodType> fmts) {
		return fmtNames.keySet().containsAll(fmts) && fmtDescriptions.keySet()
			.containsAll(fmts);
	}

	public Map<FunctionalMethodType, String> getFmtNames() {
		return fmtNames;
	}

	public Map<FunctionalMethodType, String> getFmtDescriptions() {
		return fmtDescriptions;
	}
}
