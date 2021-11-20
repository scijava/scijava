package org.scijava.ops.impl;

import org.scijava.ops.OpHistory;
import org.scijava.ops.OpHistoryService;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;


/**
 * {@link OpHistoryService} containing a singleton {@link DefaultOpHistory}
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = OpHistoryService.class)
public class SingletonOpHistoryService extends AbstractService implements
	OpHistoryService
{
	private final OpHistory history = new DefaultOpHistory();

	@Override
	public OpHistory getHistory() {
		return history;
	}

}
