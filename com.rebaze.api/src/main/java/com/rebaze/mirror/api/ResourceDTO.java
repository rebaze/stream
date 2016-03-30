package com.rebaze.mirror.api;

import java.net.URI;

import com.rebaze.stream.api.StreamSourceDTO;

public class ResourceDTO  {
	
	final StreamSourceDTO origin;
	final private String hash;
	final private URI uri;
	
	public ResourceDTO(StreamSourceDTO origin, URI uri, String hash) {
		this.origin = origin;
		this.uri = uri;
		this.hash = hash;
	}

	public StreamSourceDTO getOrigin() {
		return this.origin;
	}
	
	public String getHash() {
		return hash;
	}

	public URI getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "ResourceDTO [origin=" + origin + ", hash=" + hash + ", uri=" + uri + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceDTO other = (ResourceDTO) obj;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
}
