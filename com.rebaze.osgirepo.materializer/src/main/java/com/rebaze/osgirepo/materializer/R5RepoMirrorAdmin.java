package com.rebaze.osgirepo.materializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.osgirepo.api.LoadableArtifactDTO;
import com.rebaze.osgirepo.api.MirrorAdmin;
import com.rebaze.osgirepo.api.StreamDefinitionDTO;
import com.rebaze.osgirepo.api.StreamSourceDTO;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeSession;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import aQute.bnd.deployer.repository.api.Decision;
import aQute.bnd.deployer.repository.api.IRepositoryContentProvider;
import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class R5RepoMirrorAdmin implements MirrorAdmin {

	private final Logger LOG = LoggerFactory.getLogger(R5RepoMirrorAdmin.class);
	private OkHttpClient client = new OkHttpClient();
	private TreeSession treeSession;

	private final IRepositoryContentProvider[] providers;
	// we create r5 indexes only:
	private final R5RepoContentProvider r5provider = new R5RepoContentProvider();

	private final File baseFolder;

	public R5RepoMirrorAdmin(File base, IRepositoryContentProvider... contentProviders) {
		baseFolder = base;
		providers = contentProviders;
		this.treeSession = new TreeSession();
		treeSession.setDigestAlgorithm("SHA-256");
	}
	
	@Override
	public URI mirror(StreamDefinitionDTO def) throws Exception {
		List<StreamSourceDTO> indexes = new ArrayList<>();
		for (StreamSourceDTO src : def.sources) {
			if (src.active) {
				StreamSourceDTO dest = new StreamSourceDTO();
				dest.name = src.name;
				dest.active = src.active;
				dest.url = mirror(src).toASCIIString();
				indexes.add(dest);
			}
		}
		// retrieve artifacts from selected indexes:
		List<ContentAccessRepositoryIndexProcessor> processed = new ArrayList<>();
		for (StreamSourceDTO index : indexes) {
			ContentAccessRepositoryIndexProcessor processor = new ContentAccessRepositoryIndexProcessor(index.name);
			processed.add(processor);
			try (InputStream input = new URL(index.url).openStream()) {
				r5provider.parseIndex(input, new URI(index.name), processor, null);
			}
		}

		Set<File> resources = new HashSet<>();
		for (ContentAccessRepositoryIndexProcessor processor : processed) {
			for (LoadableArtifactDTO art : processor.getArtifacts()) {
				// we know its a local artifact
				resources.add(createLocalPath(processor, art));
			}
		}

		// then create composite index:
		File f = new File(baseFolder, "index.xml");
		try (FileOutputStream fout = new FileOutputStream(f)) {
			r5provider.generateIndex(resources, fout, def.name, baseFolder.toURI(), true, null, null);
		}
		return f.toURI();
	}

	@Override
	public URI mirror(StreamSourceDTO src) throws Exception {
		// calculate from index uri:
		URI baseUri = new URI(src.url.substring(0, src.url.lastIndexOf("/") + 1));
		return mirror(src.name, new URI(src.url), baseUri).toURI();
	}

	private File mirror(String name, URI index, URI baseUri) throws Exception {
		try (InputStream input = index.toURL().openStream()) {
			IRepositoryContentProvider provider = selectProviderForProvidedIndex(index);
			if (provider != null) {
				ContentAccessRepositoryIndexProcessor processor = new ContentAccessRepositoryIndexProcessor(name);
				provider.parseIndex(input, baseUri, processor, null);
				List<File> indexable = new ArrayList<>();
				for (LoadableArtifactDTO artifact : processor.getArtifacts()) {
					indexable.add(download(name, artifact));
				}
				// Index:
				return index(new File(baseFolder, name + "/" + getFileName(index.getPath())), indexable);
			} else {
				throw new RuntimeException("Unsupported repository type! " + index.toASCIIString());
			}
		}

	}

	private IRepositoryContentProvider selectProviderForProvidedIndex(URI index)
			throws IOException, MalformedURLException {
		IRepositoryContentProvider provider = null;

		for (IRepositoryContentProvider p : this.providers) {
			if (p.checkStream(index.getPath(), index.toURL().openStream()).getDecision() == Decision.accept) {
				provider = p;
				break;
			}
		}
		return provider;
	}

	private File index(File indexFileName, List<File> indexable) throws Exception, IOException, FileNotFoundException {
		indexFileName.getParentFile().mkdirs();
		try (OutputStream out = new FileOutputStream(indexFileName)) {
			String repoName = "Mirror " + baseFolder.getName();

			r5provider.generateIndex(new HashSet<File>(indexable), out, repoName, indexFileName.getParentFile().toURI(),
					true, null, null);
			System.out.println("Created index (" + indexFileName.getAbsolutePath() + ") for " + indexable.size()
					+ " files in repo: " + repoName);
		}

		return indexFileName;
	}

	private String getFileName(String path) {
		int idx = path.lastIndexOf("/");
		if (idx >= 0) {
			return path.substring(idx + 1);
		} else {
			return path;
		}
	}
	
	

	private File createLocalPath(ContentAccessRepositoryIndexProcessor processor, LoadableArtifactDTO art) {
		return new File(baseFolder, processor.getName() + "/" + art.getUri());
	}

	protected File download(String repoName, LoadableArtifactDTO artifact) throws Exception {
		File target = new File(baseFolder, repoName + "/" + artifact.getUri().getPath());
		if (!alreadyAvailable(target, artifact.getHash())) {
			System.out.println("Downloading: " + target.getAbsolutePath());
			target.getParentFile().mkdirs();
			if ("file".equals(artifact.getUri().getScheme())) {
				download(new File(artifact.getUri()), target);
			} else {
				download(artifact.getUri(), target);
			}
		} else {
			System.out.println("Already available: " + target.getAbsolutePath());
		}
		return target;

	}

	private boolean alreadyAvailable(File target, String hash) {
		if (target.exists()) {
			Tree tree = treeSession.createStreamTreeBuilder().add(target).seal();
			if (!hash.equals(tree.fingerprint())) {
				System.err.println("Bad checksum: " + hash + "(we have " + tree.fingerprint() + "): Redownload.");
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	protected void download(URI uri, File target) throws Exception {
		Request request = new Request.Builder().url(uri.toURL()).build();
		Response response = client.newCall(request).execute();
		ResponseBody body = response.body();

		// LOG.info("Loading " + body.contentLength() + " bytes of type " +
		// body.contentType() + "..");
		try (Source a = Okio.source(body.byteStream()); BufferedSink b = Okio.buffer(Okio.sink(target))) {
			b.writeAll(a);
		}

	}

	protected void download(File uri, File target) throws Exception {
		try (Source a = Okio.source(uri); BufferedSink b = Okio.buffer(Okio.sink(target))) {
			b.writeAll(a);
		}
	}
}
