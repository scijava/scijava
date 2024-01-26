package org.scijava.legacy.service;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;

/**
 * A SciJava {@link Service} that instantiates a single {@link OpEnvironment}.
 *
 * The primary use case is in SciJava scripting, and within scripts this
 * service should not be used
 */
public interface OpEnvironmentService extends SciJavaService {

	OpEnvironment env();

}
