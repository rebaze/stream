package com.rebaze.osgirepo.api;

import java.net.URI;

public class LoadableArtifactDTO  {
	final private String hash;
	final private URI uri;
	
	public LoadableArtifactDTO(URI uri, String hash) {
		this.uri = uri;
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}

	public URI getUri() {
		return uri;
	}	
	
}
