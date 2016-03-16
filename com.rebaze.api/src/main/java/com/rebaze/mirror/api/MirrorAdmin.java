package com.rebaze.mirror.api;

import java.util.List;

import com.rebaze.stream.api.StreamSourceResourcesDTO;

/**
 * A service that is able to fully mirror remote repositories.
 * Implementations should allow incremental updates where possible.
 * 
 * @author tonit
 *
 */
public interface MirrorAdmin {

	public List<StreamSourceResourcesDTO> mirror() throws Exception;
}
