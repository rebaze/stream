package com.rebaze.workspace.api;

import java.net.URI;
import java.net.URISyntaxException;

import com.rebaze.tree.api.HashAlgorithm;
import com.rebaze.tree.api.Selector;
import com.rebaze.tree.api.StaticTree;
import com.rebaze.tree.api.Tag;
import com.rebaze.tree.api.Tree;

/**
 * Format URI: streamlink:<SHATYPE>/<SHA>
 * E.g.
 * streamlink:SHA-256/IBIUBI6IB6767
 * TODO: hash://sha256/c8909ef4e934d5954f1ef8c8
 * @author tonit
 *
 */
public class ResourceLink implements Tree {
	
	public final static String SCHEME = "streamlink";

	private final Tree tree;
	private final URI uri;
	
	public static ResourceLink from( URI uri) {
		if (SCHEME.equals(uri.getScheme())) {
			String rawPath = uri.getPath();
			return from( uri, rawPath);
		}else {
			throw new IllegalArgumentException("Incoming Uri is not a ResourceLink: " + uri);
		}
	}
	
	public static ResourceLink from( URI uri, String rawPath) {
		String[] path = rawPath.split("/");
		if (path.length != 3) {
			throw new IllegalArgumentException("Incoming ResourceLink is not correctly formated: " + rawPath);
		}
		String type = path[1];
		String id = path[2];
		Tree tree = new StaticTree(Selector.selector(""),HashAlgorithm.valueOf(type),id,new Tree[0],(Tag)null);
		return new ResourceLink(tree, uri);
	}

	public static ResourceLink from( String rawPath) {
		return from( null,rawPath);
	}

	public ResourceLink(Tree tree) {
		this(tree,null); // will create the uri automatically.
	}
	
	public ResourceLink(Tree tree, URI uri) {
		this.tree = tree;
		if (uri == null) {
			try {
				this.uri = new URI(SCHEME + ":/" + tree.algorithm().value() + "/" + tree.fingerprint());
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("Cannot create URI from " + toString());
			}
		}else {
			this.uri = uri;
		}
	}
	
	public URI toUri() {
		return uri ;
	}
	
	@Override
	public String toString() {
		return "ResourceLink[" + tree.toString() + "]";
	}
	
	@Override
	public int hashCode() {
		return tree.hashCode();
		/**
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
		**/
	}

	@Override
	public boolean equals(Object obj) {
		return tree.equals(obj);
	}

	@Override
	public HashAlgorithm algorithm() {
		return tree.algorithm();
	}

	@Override
	public String fingerprint() {
		return tree.fingerprint();
	}

	@Override
	public Selector selector() {
		return tree.selector();
	}

	@Override
	public Tree[] branches() {
		return tree.branches();
	}

	@Override
	public Tag tags() {
		return tree.tags();
	}
	
	

	
}
