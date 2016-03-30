package com.rebaze.transport.api;

import java.util.List;

import com.rebaze.mirror.api.ResourceDTO;

/**
 * Is able to turn a list of resources to another location.
 * 
 * @author tonit
 *
 */
public interface TransportAgent {
	
	List<ResourceDTO> transport( TransportMonitor monitor, List<ResourceDTO> resource);	

}
