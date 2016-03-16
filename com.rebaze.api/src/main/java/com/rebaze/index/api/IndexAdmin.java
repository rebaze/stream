package com.rebaze.index.api;

import java.net.URI;
import java.util.List;

import com.rebaze.stream.api.StreamSourceResourcesDTO;

public interface IndexAdmin {

	URI index(List<StreamSourceResourcesDTO> streamResources) throws Exception;
	
}
