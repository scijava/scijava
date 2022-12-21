package org.scijava.ops.engine.yaml;

import java.util.Map;

import org.scijava.ops.api.OpInfo;

public interface YAMLOpInfoCreator {

	boolean canCreateFrom(String source, String identifier);

	OpInfo create(final Map<String, Object> yaml, final String version)
			throws Exception;

}
