package org.scijava.ops.engine;

import org.scijava.ops.api.OpHistory;
import org.scijava.service.SciJavaService;


public interface OpHistoryService extends SciJavaService {

	OpHistory getHistory();

}
