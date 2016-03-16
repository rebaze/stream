package com.rebaze.osgirepo.materializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rebaze.index.api.IndexAdmin;
import com.rebaze.mirror.api.LoadableArtifactDTO;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.stream.api.StreamSourceDTO;
import com.rebaze.stream.api.StreamSourceResourcesDTO;

import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;

public class R5RepoIndexAdmin implements IndexAdmin {

	private final R5RepoContentProvider r5provider = new R5RepoContentProvider();

	private final File baseFolder;

	private final StreamDefinitionDTO definition;

	public R5RepoIndexAdmin(File base, StreamDefinitionDTO input) {
		this.baseFolder = base;
		this.definition = input;
	}

	@Override
	public URI index(List<StreamSourceResourcesDTO> streamResources) throws Exception {
		Set<URI> resources = new HashSet<>();		
		for (StreamSourceResourcesDTO index : streamResources) {
			// The single index:
			File singleIndexFile = index(new File(baseFolder, index.name + "/" + getFileName(index.url)),
					index.resources);

			//ContentAccessRepositoryIndexProcessor processor = new ContentAccessRepositoryIndexProcessor(index.name);
			//processed.add(processor);
			//try (InputStream input = new URL(index.url).openStream()) {
			//	r5provider.parseIndex(input, new URI(index.name), processor, null);
			//}
			resources.addAll(index.resources);

		}
		
		// then we use that index file to create a compound index:
	/**
		for (ContentAccessRepositoryIndexProcessor processor : processed) {
			for (LoadableArtifactDTO art : processor.getArtifacts()) {
				// we know its a local artifact
				resources.add(createLocalPath(processor, art));
			}
		}
		
		// then create composite index:
		File f = new File(baseFolder, "index.xml");
		System.out.println("Creating composite index " + f.getAbsolutePath() + " for " + resources.size()
				+ " resources in " + definition.sources.length + " source streams.");

		try (FileOutputStream fout = new FileOutputStream(f)) {
			r5provider.generateIndex(asLocal(resources), fout, definition.name, baseFolder.toURI(), true, null, null);
		}
		**/
		return null; //f.toURI();
	}

	private File index(File indexFileName, List<URI> indexable) throws Exception {

		indexFileName.getParentFile().mkdirs();
		try (OutputStream out = new FileOutputStream(indexFileName)) {
			String repoName = "Mirror " + baseFolder.getName();
			
			r5provider.generateIndex(asLocal(indexable), out, repoName, indexFileName.getParentFile().toURI(), true,
					null, null);
			System.out.println("Created index (" + indexFileName.getAbsolutePath() + ") for " + indexable.size()
					+ " files in repo: " + repoName);
		}

		return indexFileName;
	}

	private Set<File> asLocal(Collection<URI> indexable) {
		Set<File> ret = new HashSet<>();
		for (URI uri : indexable) {
			ret.add(new File(uri));
		}
		return ret;
	}

	private File createLocalPath(ContentAccessRepositoryIndexProcessor processor, LoadableArtifactDTO art) {
		return new File(baseFolder, processor.getName() + "/" + art.getUri());
	}

	private String getFileName(String path) {
		// strip the gz
		if (path.endsWith(".gz")) {
			path = path.substring(0, path.length() -3);
		}
		int idx = path.lastIndexOf("/");
		if (idx >= 0) {
			return path.substring(idx + 1);
		} else {
			return path;
		}
	}
}
