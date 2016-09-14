package com.rebaze.repository.osgi;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamSourceDTO;
import com.rebaze.tree.api.HashAlgorithm;

import aQute.bnd.deployer.repository.api.IRepositoryIndexProcessor;
import aQute.bnd.deployer.repository.api.Referral;

public class ContentAccessRepositoryIndexProcessor implements IRepositoryIndexProcessor {

	private final List<ResourceDTO> list;
	private StreamSourceDTO origin;

	ContentAccessRepositoryIndexProcessor(StreamSourceDTO origin) {
		this.origin =origin;
		this.list = new ArrayList<>();
	}

	@Override
	public void processResource(Resource resource) {
		List<Capability> cap = resource.getCapabilities("osgi.content");

		for (Capability c : cap) {
			URI url = (URI) c.getAttributes().get("url");
			String hash = (String) c.getAttributes().get("osgi.content");
			// TODO make ResourceDTO have Tree structure instead of alg+hash.
			try {
				list.add(new ResourceDTO(origin, url, hash, HashAlgorithm.SHA256));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void processReferral(URI parentUri, Referral referral, int maxDepth, int currentDepth) {
		throw new UnsupportedOperationException("Unsupported in ContentAccessRepositoryIndexProcessor");
	}
	
	public List<ResourceDTO> getArtifacts() {
		return list;
	}
}
