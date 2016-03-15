package com.rebaze.osgirepo.api;

import java.net.URI;

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
