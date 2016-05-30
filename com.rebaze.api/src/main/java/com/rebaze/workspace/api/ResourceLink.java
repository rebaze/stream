package com.rebaze.workspace.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

import com.rebaze.mirror.api.ResourceDTO.HashType;

/**
 * Format URI: streamlink:<SHATYPE>/<SHA>
 * E.g.
 * streamlink:SHA-256/IBIUBI6IB6767
 * @author tonit
 *
 */
public class ResourceLink {
	
	public final static String SCHEME = "streamlink";

	private final HashType type;
	private final String id;
	private final URI uri;
	
	public static ResourceLink from(URI uri) {
		if (SCHEME.equals(uri.getScheme())) {
			String rawPath = uri.getPath();
			return from(uri, rawPath);
		}else {
			throw new IllegalArgumentException("Incoming Uri is not a ResourceLink: " + uri);
		}
	}
	
	public static ResourceLink from(URI uri, String rawPath) {
		String[] path = rawPath.split("/");
		if (path.length != 3) {
			throw new IllegalArgumentException("Incoming ResourceLink is not correctly formated: " + rawPath);
		}
		String type = path[1];
		String id = path[2];
		return new ResourceLink(HashType.valueOf(type), id, uri);
	}

	public static ResourceLink from(String rawPath) {
		return from(null,rawPath);
	}

	public ResourceLink(HashType type, String id) {
		this(type,id,null); // will create the uri automatically.
	}
	
	public ResourceLink(HashType type, String id, URI uri) {
		this.type = type;
		this.id = id;
		if (uri == null) {
			try {
				this.uri = new URI(SCHEME + ":/" + type + "/" + id);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("Cannot create URI from " + toString());
			}
		}else {
			this.uri = uri;
		}
	}
	
	public HashType type() {
		return type;
	}
	
	public String id() {
		return id;
	}

	public URI toUri() {
		return uri ;
	}
	
	@Override
	public String toString() {
		return "ResourceLink[" + type + ";" + id + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ResourceLink other = (ResourceLink) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type != other.type)
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	
}
