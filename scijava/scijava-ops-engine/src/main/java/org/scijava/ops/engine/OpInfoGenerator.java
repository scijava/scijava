package org.scijava.ops.engine;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.scijava.ops.api.OpInfo;

public interface OpInfoGenerator {

	boolean canGenerateFrom(Object o);

	List<OpInfo> generateInfosFrom(Object o);
	
	static List<OpInfo> generateAll(Object o) {
		return StreamSupport.stream(ServiceLoader.load(OpInfoGenerator.class).spliterator(), true) //
				.flatMap(g -> g.generateInfosFrom(o).stream()) //
				.collect(Collectors.toList());
	}

}
