package com.rebaze.mirror.api;

import java.net.URI;

import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.stream.api.StreamSourceDTO;

/**
 * A service that is able to fully mirror remote repositories.
 * Implementations should allow incremental updates where possible.
 * 
 * @author tonit
 *
 */
public interface MirrorAdmin {

	public URI mirror(StreamDefinitionDTO def) throws Exception;

	public URI mirror(StreamSourceDTO src) throws Exception;
}
