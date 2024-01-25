package org.scijava.legacy.service;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.service.SciJavaService;

// TODO: Avoid name clash with ImageJ Ops
public interface OpService extends SciJavaService {

	OpEnvironment env();

}
