package com.rebaze.mirror.api;

import java.net.URI;

import com.rebaze.stream.api.StreamSourceDTO;
import com.rebaze.tree.api.HashAlgorithm;
import com.rebaze.tree.api.Selector;
import com.rebaze.tree.api.Tag;
import com.rebaze.tree.api.Tree;

public class ResourceDTO implements Tree {
	
	final StreamSourceDTO origin;
	final private String hash;
	final private URI uri;
	final private HashAlgorithm hashtype;
	final private Selector selector;

	public ResourceDTO(StreamSourceDTO origin, URI uri, String hash, HashAlgorithm hashType) {
		this.origin = origin;
		this.uri = uri;
		this.hash = hash;
		this.hashtype = hashType;
		this.selector = Selector.selector(uri.toASCIIString());
	}

	public StreamSourceDTO getOrigin() {
		return this.origin;
	}

	public URI getUri() {
		return uri;
	}

	public boolean isHashType(HashAlgorithm type) {
		return type == hashtype;
	}

	@Override
	public String toString() {
		return "ResourceDTO [origin=" + origin + ", hash=" + hash + ", hashtype=" + hashtype + ", uri=" + uri + "]";
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

	@Override
	public HashAlgorithm algorithm() {
		return hashtype;
	}

	@Override
	public String fingerprint() {
		return hash;
	}

	@Override
	public Selector selector() {
		return selector;
	}

	@Override
	public Tree[] branches() {
		return new Tree[0];
	}

	@Override
	public Tag tags() {
		return null;
	}
}
