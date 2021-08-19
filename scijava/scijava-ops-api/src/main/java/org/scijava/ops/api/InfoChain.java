
package org.scijava.ops.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InfoChain {

	private final OpInfo info;
	private final List<InfoChain> dependencies;

	public InfoChain(OpInfo info) {
		this.info = info;
		this.dependencies = Collections.emptyList();
	}

	public InfoChain(OpInfo info, List<InfoChain> dependencies) {
		this.info = info;
		this.dependencies = new ArrayList<>(dependencies);
	}

	public Object op() {
		List<Object> dependencyInstances = dependencies.stream() //
			.map(d -> d.op()) //
			.collect(Collectors.toList());
		return info.createOpInstance(dependencyInstances).object();
	}

	public String id() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public OpInfo info() {
		return info;
	}

	public List<InfoChain> dependencies() {
		return dependencies;
	}

}
