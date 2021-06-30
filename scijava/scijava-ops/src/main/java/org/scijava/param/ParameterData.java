package org.scijava.param;

import java.util.List;

import org.scijava.struct.Member;

public interface ParameterData {

	public List<Member<?>> synthesizeMembers(List<FunctionalMethodType> fmts);

}
