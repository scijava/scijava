package org.scijava.param;

import java.util.List;

public interface ParameterData {

	public List<Parameter> synthesizeAnnotations(List<FunctionalMethodType> fmts);

}
