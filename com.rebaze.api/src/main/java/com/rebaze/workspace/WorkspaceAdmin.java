package com.rebaze.workspace;

import java.io.File;

import com.rebaze.mirror.api.ResourceDTO;

public interface WorkspaceAdmin {
	
	File getPathFor(ResourceDTO artifact);

	boolean existsInWorkspace(ResourceDTO artifact);
	
}
