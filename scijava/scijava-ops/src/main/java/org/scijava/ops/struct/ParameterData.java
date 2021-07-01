package org.scijava.ops.struct;

import java.util.List;

import org.scijava.struct.FunctionalMethodType;

public interface ParameterData {

	public List<SynthesizedParameterMember<?>> synthesizeMembers(List<FunctionalMethodType> fmts);

}
