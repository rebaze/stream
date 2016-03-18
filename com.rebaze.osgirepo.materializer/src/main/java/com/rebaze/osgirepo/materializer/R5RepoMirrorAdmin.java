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
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.stream.api.StreamSourceDTO;
import com.rebaze.stream.api.StreamSourceResourcesDTO;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeSession;
import com.rebaze.trees.core.internal.DefaultTreeSessionFactory;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import aQute.bnd.deployer.repository.api.Decision;
import aQute.bnd.deployer.repository.api.IRepositoryContentProvider;
import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class R5RepoMirrorAdmin implements MirrorAdmin {

	private final Logger LOG = LoggerFactory.getLogger(R5RepoMirrorAdmin.class);
	private OkHttpClient client = new OkHttpClient();

	private TreeSession treeSession;
	private final IRepositoryContentProvider[] providers;

	private final File baseFolder;
	private StreamDefinitionDTO definition;

	public R5RepoMirrorAdmin(TreeSession session, File base, StreamDefinitionDTO def, IRepositoryContentProvider... contentProviders) {
		baseFolder = base;
		providers = contentProviders;
		this.definition = def;
		this.treeSession = session;
	}
	
	@Override
	public List<ResourceDTO> fetchResources() throws Exception {
		// Mirror must return a set of "mirrored" resources per StreamSource
		List<ResourceDTO> resources = new ArrayList<>();
		for (StreamSourceDTO src : definition.sources) {
			if (src.active) {				
				resources.addAll(fetchIndex(src));
			}
		}
		return resources; 
	}



	private List<ResourceDTO> fetchIndex(StreamSourceDTO origin) throws Exception {
		URI baseUri = new URI(origin.url.substring(0, origin.url.lastIndexOf("/") + 1));
		URI index = new URI(origin.url);

		//try (InputStream input = openStream(index)) {
		try (BufferedSource s = Okio.buffer(Okio.source(openStream(index)))) {
			IRepositoryContentProvider provider = selectProviderForProvidedIndex(index);
			if (provider != null) {
				ContentAccessRepositoryIndexProcessor processor = new ContentAccessRepositoryIndexProcessor(origin);
				provider.parseIndex(s.inputStream(), baseUri, processor, null);
				return processor.getArtifacts();	
			} else {
				throw new RuntimeException("Unsupported repository type! " + index.toASCIIString());
			}
		}

	}
	
	@Override
	public List<ResourceDTO> download(List<ResourceDTO> remoteList) throws Exception {
		List<ResourceDTO> ret = new ArrayList<>(remoteList.size());
		for (ResourceDTO loadable : remoteList) {
			File target = createLocalName(loadable); // the expected file name
			
			ResourceDTO local = download(loadable,target);
			ret.add(local);
		}
		return ret;
	}

	private InputStream openStream(URI index) throws IOException {
		if (index.getPath().endsWith(".gz")) {
			return new GZIPInputStream(index.toURL().openStream());
		} else {
			return index.toURL().openStream();

		}
	}

	private IRepositoryContentProvider selectProviderForProvidedIndex(URI index)
			throws IOException, MalformedURLException {
		IRepositoryContentProvider provider = null;

		for (IRepositoryContentProvider p : this.providers) {
			if (p.checkStream(index.getPath(), openStream(index)).getDecision() == Decision.accept) {
				provider = p;
				break;
			}
		}
		return provider;
	}
	
	protected ResourceDTO download( ResourceDTO artifact, File target) throws Exception {
		if (!alreadyAvailable(target, artifact.getHash())) {
			System.out.println("Downloading: " + target.getAbsolutePath());
			target.getParentFile().mkdirs();
			try {
				if ("file".equals(artifact.getUri().getScheme())) {
					download(new File(artifact.getUri()), target);
				} else {
					download(artifact.getUri(), target);
				}
				
			}catch(Exception e) {
				System.err.println("Unable to download " + artifact.getUri() + " Exception: " + e.getMessage());
				return null;
			}
		} else {
			System.out.println("Already available: " + target.getAbsolutePath());
		}
		return new ResourceDTO(artifact.getOrigin(),target.toURI(),artifact.getHash());

	}

	private File createLocalName(ResourceDTO artifact) {
		return new File(baseFolder, artifact.getOrigin().name + "/" + artifact.getUri().getPath());
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
