package com.rebaze.index.api;

import java.net.URI;
import java.util.List;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamSourceResourcesDTO;

public interface IndexAdmin {

	List<URI> index(List<ResourceDTO> streamResources) ;

	URI index(StreamSourceResourcesDTO index);
	
	URI compositeIndex(List<URI> indexes);
	
	
}
