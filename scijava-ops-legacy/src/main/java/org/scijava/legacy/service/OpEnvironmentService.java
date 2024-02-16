
package org.scijava.legacy.service;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;

/**
 * A SciJava {@link Service} that instantiates a single {@link OpEnvironment}.
 * The primary use case is in SciJava scripting, but within scripts this service
 * should not be used - instead, users should declare an {@link OpEnvironment}
 * parameter, which will be filled using a preprocessor plugin.
 *
 * @author Gabriel Selzer
 */
public interface OpEnvironmentService extends SciJavaService {

	/**
	 * Returns this Service's {@link OpEnvironment}.
	 *
	 * @return an {@link OpEnvironment}
	 */
	OpEnvironment env();

}
