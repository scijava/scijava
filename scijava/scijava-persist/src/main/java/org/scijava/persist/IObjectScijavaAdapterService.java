
package org.scijava.persist;

import java.util.List;

import org.scijava.plugin.PTService;
import org.scijava.plugin.PluginInfo;
import org.scijava.service.SciJavaService;

public interface IObjectScijavaAdapterService extends
	PTService<IObjectScijavaAdapter>, SciJavaService
{

	<PT extends IObjectScijavaAdapter> List<PluginInfo<PT>> getAdapters(
		Class<PT> adapterClass);
}
