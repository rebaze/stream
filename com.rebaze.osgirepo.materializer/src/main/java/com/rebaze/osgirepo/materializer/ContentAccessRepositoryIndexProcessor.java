package com.rebaze.osgirepo.materializer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

import com.rebaze.osgirepo.api.LoadableArtifactDTO;

import aQute.bnd.deployer.repository.api.IRepositoryIndexProcessor;
import aQute.bnd.deployer.repository.api.Referral;

public class ContentAccessRepositoryIndexProcessor implements IRepositoryIndexProcessor {

	private final List<LoadableArtifactDTO> list;
	private String name;

	ContentAccessRepositoryIndexProcessor(String name) {
		this.name = name;
		this.list = new ArrayList<>();
	}

	@Override
	public void processResource(Resource resource) {
		List<Capability> cap = resource.getCapabilities("osgi.content");

		for (Capability c : cap) {
			URI url = (URI) c.getAttributes().get("url");
			String hash = (String) c.getAttributes().get("osgi.content");
			try {
				list.add(new LoadableArtifactDTO(url, hash));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void processReferral(URI parentUri, Referral referral, int maxDepth, int currentDepth) {
		
	}
	
	public String getName() {
		return this.name;
	}

	public List<LoadableArtifactDTO> getArtifacts() {
		return list;
	}
}
