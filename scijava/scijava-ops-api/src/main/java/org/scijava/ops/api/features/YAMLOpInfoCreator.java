
package org.scijava.ops.api.features;

import java.net.URI;
import java.util.Map;

import org.scijava.ops.api.OpInfo;

public interface YAMLOpInfoCreator {

	boolean canCreateFrom(URI identifier);

	OpInfo create(final URI identifier, final Map<String, Object> yaml)
		throws Exception;

}
