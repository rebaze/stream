package com.rebaze.mirror.api;

import java.util.List;

/**
 * A service that is able to fully mirror remote repositories.
 * Implementations should allow incremental updates where possible.
 * 
 * @author tonit
 *
 */
public interface MirrorAdmin {

	List<ResourceDTO> fetchResources() throws Exception;

	List<ResourceDTO> download(List<ResourceDTO> resource) throws Exception;	
}
