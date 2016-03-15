package com.rebaze.osgirepo.api;

import java.net.URI;

public interface MirrorAdmin {

	public URI mirror(StreamDefinitionDTO def) throws Exception;

	public URI mirror(String name, URI index) throws Exception;
}
