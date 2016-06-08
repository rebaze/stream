package com.rebaze.repository.osgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.index.api.IndexAdmin;
import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.stream.api.StreamSourceDTO;
import com.rebaze.stream.api.StreamSourceResourcesDTO;

import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;
import okio.BufferedSource;
import okio.Okio;

@Component(immediate=true)
public class R5RepoIndexAdmin implements IndexAdmin {
	private static final Logger LOG = LoggerFactory.getLogger(R5RepoIndexAdmin.class);

	private final R5RepoContentProvider r5provider = new R5RepoContentProvider();

	@Reference
	private StreamDefinitionDTO definition;

	// used in OSGi with ConfigAdmin
	public R5RepoIndexAdmin( ) {
	}
	
	@Activate
	private void activate(ComponentContext context ) {
		LOG.info("# Activating " + context.getProperties().get("component.name"));
	}
	
	@Deactivate
	private void deactivate(ComponentContext context ) {
		LOG.info("# Deactivating " + context.getProperties().get("component.name"));
	}
	
	// testing constructor
	public R5RepoIndexAdmin(StreamDefinitionDTO def) {
		this.definition = def;
	}

	@Override
	public List<URI> index(List<ResourceDTO> streamResources) {
		List<URI> indexes = new ArrayList<>();
		// indexes per origin repo:
		Map<StreamSourceDTO, List<URI>> map = mapByOrigin(streamResources);
		for (StreamSourceDTO origin : map.keySet()) {
			URI singleIndexFile = index(new File(definition.localPath, origin.name + "/" + getFileName(origin.url)),
					map.get(origin));
			indexes.add(singleIndexFile);
		}

		return indexes;

		// then we use that index file to create a compound index:
		/**
		 * for (ContentAccessRepositoryIndexProcessor processor : processed) {
		 * for (LoadableArtifactDTO art : processor.getArtifacts()) { // we know
		 * its a local artifact resources.add(createLocalPath(processor, art));
		 * } }
		 * 
		 * // then create composite index: File f = new File(baseFolder,
		 * "index.xml"); System.out.println("Creating composite index " +
		 * f.getAbsolutePath() + " for " + resources.size() + " resources in " +
		 * definition.sources.length + " source streams.");
		 * 
		 * try (FileOutputStream fout = new FileOutputStream(f)) {
		 * r5provider.generateIndex(asLocal(resources), fout, definition.name,
		 * baseFolder.toURI(), true, null, null); }
		 **/
	}

	private Map<StreamSourceDTO, List<URI>> mapByOrigin(List<ResourceDTO> streamResources) {
		Map<StreamSourceDTO, List<URI>> map = new HashMap<>();
		for (ResourceDTO resource : streamResources) {
			List<URI> repoResource = map.get(resource.getOrigin());
			if (repoResource == null) {
				repoResource = new ArrayList<>();
				map.put(resource.getOrigin(), repoResource);
			}
			repoResource.add(resource.getUri());
		}
		return map;
	}

	@Override
	public URI index(StreamSourceResourcesDTO index) {
		return index(new File(definition.localPath, index.name + "/" + getFileName(index.url)).getAbsoluteFile(), index.resources);
	}

	private URI index(File indexFileName, List<URI> indexable) {
		LOG.info("Indexing for " + indexFileName.getAbsolutePath());
		indexFileName.getParentFile().mkdirs();
		try (OutputStream out = new FileOutputStream(indexFileName)) {
			String repoName = "Mirror " + definition.name;

			r5provider.generateIndex(asLocal(indexable), out, repoName, indexFileName.getParentFile().toURI(), true,
					null, null);
			LOG.info("Created index (" + indexFileName.getAbsolutePath() + ") for " + indexable.size()
					+ " files in repo: " + repoName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return indexFileName.toURI();
	}

	private Set<File> asLocal(Collection<URI> indexable) {
		Set<File> ret = new HashSet<>();
		for (URI uri : indexable) {
			ret.add(new File(uri));
		}
		return ret;
	}

	private String getFileName(String path) {
		// strip the gz
		if (path.endsWith(".gz")) {
			path = path.substring(0, path.length() - 3);
		}
		if (!path.endsWith(".xml")) {
			path = path + ".xml";
		}
		int idx = path.lastIndexOf("/");
		if (idx >= 0) {
			return path.substring(idx + 1);
		} else {
			return path;
		}
	}

	

	@Override
	public URI compositeIndex(List<URI> indexes) {
		List<URI> streamResources = new ArrayList<>();
		StreamSourceDTO origin = new StreamSourceDTO();
		origin.name = "composite";
		ContentAccessRepositoryIndexProcessor processor = new ContentAccessRepositoryIndexProcessor(origin);

		for (URI index : indexes) {
			URI base = new File(index).getParentFile().toURI();
			// read that index and get their resources.
			
			try (BufferedSource s = Okio.buffer(Okio.source(index.toURL().openStream()))) {
				r5provider.parseIndex(s.inputStream(), base , processor, null);
				for (ResourceDTO res : processor.getArtifacts()) {
					streamResources.add(res.getUri());
				}
			}catch(Exception e) {
				LOG.warn("Problem opening index at " + index,e);
			}
		}
		// then create a single composite
		URI composite = index(new File(definition.localPath, "index.xml"), streamResources);
		System.out.println("Created compositeIndex (" + composite.getPath() + ") for " + streamResources.size() + " resources from " + indexes.size() + " indexes.");
		return composite;
	}
}
