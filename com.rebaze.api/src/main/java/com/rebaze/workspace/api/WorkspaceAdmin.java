package com.rebaze.workspace.api;

import java.io.File;
import java.util.Collection;

import com.rebaze.mirror.api.ResourceDTO;

public interface WorkspaceAdmin {
	
	//File getPathFor(ResourceDTO artifact);
	
	DataSink sink(ResourceDTO artifact);

	DataSource resolve(ResourceDTO artifact);
	
	DataSource resolve(ResourceLink link);

	Collection<DataSource> list();

	File getLocation();
	
}
