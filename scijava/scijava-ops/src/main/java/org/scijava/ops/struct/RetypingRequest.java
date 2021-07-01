
package org.scijava.ops.struct;

import java.util.List;

import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.Struct;

public class RetypingRequest {

	private final Struct originalStruct;
	private final List<FunctionalMethodType> newFmts;

	public RetypingRequest(Struct originalStruct,
		List<FunctionalMethodType> newFmts)
	{
		this.originalStruct = originalStruct;
		this.newFmts = newFmts;
	}

	public Struct struct() {
		return originalStruct;
	}

	public List<FunctionalMethodType> newFmts() {
		return newFmts;
	}

}
