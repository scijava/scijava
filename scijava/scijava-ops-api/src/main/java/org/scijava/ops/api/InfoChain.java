
package org.scijava.ops.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A data structure wrangling a hierarchy of {@link OpInfo}s.
 *
 * @author Gabriel Selzer
 */
public class InfoChain {

	public static final String DEP_END_DELIM = "]";
	public static final String DEP_START_DELIM = "[";

	private final List<InfoChain> dependencies;
	private String id;

	private final OpInfo info;

	public InfoChain(OpInfo info) {
		this.info = info;
		this.dependencies = Collections.emptyList();
	}

	public InfoChain(OpInfo info, List<InfoChain> dependencies) {
		this.info = info;
		this.dependencies = new ArrayList<>(dependencies);
	}

	public List<InfoChain> dependencies() {
		return dependencies;
	}

	@Override
	public boolean equals(Object obj) {
		// Since the id is unique, we can check equality on that
		if (!(obj instanceof InfoChain)) return false;
		return signature().equals(((InfoChain) obj).signature());
	}

	@Override
	public int hashCode() {
		// Since the id is unique, we can hash on that
		return signature().hashCode();
	}

	/**
	 * Builds a String uniquely identifying this tree of Ops. As each
	 * {@link OpInfo#id()} should be unique to <b>that</b> {@link OpInfo}, we can
	 * use those to uniquely identify a hierarchy of {@link OpInfo}s, and thus by
	 * extension a unique {@link InfoChain}.
	 *
	 * @return a {@link String} uniquely identifying this {@link InfoChain}
	 */
	public String signature() {
		if (id == null) generateSignature();
		return id;
	}

	public OpInfo info() {
		return info;
	}

	public Object op() {
		List<Object> dependencyInstances = dependencies().stream() //
			.map(d -> d.op()) //
			.collect(Collectors.toList());
		return info().createOpInstance(dependencyInstances).object();
	}

	private synchronized void generateSignature() {
		if (id != null) return;
		String s = info().id();
		s = s.concat(DEP_START_DELIM);
		for (InfoChain dependency : dependencies()) {
			s = s.concat(dependency.signature());
		}
		id = s.concat(DEP_END_DELIM);
	}

}
