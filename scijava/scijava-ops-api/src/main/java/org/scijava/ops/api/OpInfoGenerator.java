package org.scijava.ops.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface OpInfoGenerator {

	boolean canGenerateFrom(Object o);

	List<OpInfo> generateInfosFrom(Object o);

}
