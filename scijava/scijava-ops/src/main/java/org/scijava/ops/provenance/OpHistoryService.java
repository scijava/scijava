package org.scijava.ops.provenance;

import org.scijava.service.SciJavaService;


public interface OpHistoryService extends SciJavaService {

	OpHistory getHistory();

}
