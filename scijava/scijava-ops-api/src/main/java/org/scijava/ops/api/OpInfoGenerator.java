package org.scijava.ops.api;

import java.util.List;

public interface OpInfoGenerator {

	boolean canGenerateFrom(Object o);

	List<OpInfo> generateInfosFrom(Object o);

}
