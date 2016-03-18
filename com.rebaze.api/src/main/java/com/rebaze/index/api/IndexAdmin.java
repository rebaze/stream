package com.rebaze.index.api;

import java.net.URI;
import java.util.List;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamSourceResourcesDTO;
import com.rebaze.tree.api.Tree;

public interface IndexAdmin {

	List<URI> index(List<ResourceDTO> streamResources) throws Exception;

	URI index(StreamSourceResourcesDTO index);
	
	URI compositeIndex(List<URI> indexes) throws Exception;	

	Tree createTree(String prefix, List<ResourceDTO> resources);
	
	Tree createTree(List<ResourceDTO> resources);

	
	
	
}
