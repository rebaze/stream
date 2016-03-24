package com.rebaze.osgirepo.materializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.stream.api.StreamSourceDTO;
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
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

@Component(immediate = true, name = "R5RepoMirrorAdmin")
public class R5RepoMirrorAdmin implements MirrorAdmin {

	private static final Logger LOG = LoggerFactory.getLogger(R5RepoMirrorAdmin.class);

	@Reference
	private StreamDefinitionDTO definition;

	@Reference
	private TreeSession treeSession;
	
	private final OkHttpClient client = new OkHttpClient();


	// @Reference(cardinality=ReferenceCardinality.OPTIONAL)
	private IRepositoryContentProvider[] providers = new IRepositoryContentProvider[] { new R5RepoContentProvider() };

	@Activate
	private void activate(ComponentContext context) {
		LOG.info("#Mirror! Activating " + context.getProperties().get("component.name"));

	}

	@Deactivate
	private void deactivate(ComponentContext context) {

		LOG.info("# Mirror! Deactivating " + context.getProperties().get("component.name"));
	}

	// @Activate
	void activate(MirrorConfig config) throws Exception {
		if (1 == 1)
			throw new RuntimeException("Activating a Mirror!!!");
		URI uri = null;
		if (config.definitionUri().contains("//:")) {
			uri = new URI(config.definitionUri());
		} else {
			uri = new File(config.definitionUri()).toURI();

		}
		try (InputStream stream = uri.toURL().openStream()) {

			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			definition = gson.fromJson(new InputStreamReader(Okio.buffer(Okio.source(stream)).inputStream()),
					StreamDefinitionDTO.class);
		}
	}

	// testing and standalone constructor. Inject all references statically.
	/**
	 * public R5RepoMirrorAdmin(TreeSession session, MirrorConfig config,
	 * IRepositoryContentProvider... contentProviders) throws Exception {
	 * providers = contentProviders; this.treeSession = session;
	 * activate(config); }
	 **/

	@Override
	public List<ResourceDTO> fetchResources() {
		List<ResourceDTO> resources = new ArrayList<>();
		try {
		// Mirror must return a set of "mirrored" resources per StreamSource
		for (StreamSourceDTO src : definition.sources) {
			if (src.active) {
				resources.addAll(fetchIndex(src));
			}
		}
		} catch (Exception e) {
			LOG.warn("Problem fetching resources.",e);
		}
		return resources;
	}

	private List<ResourceDTO> fetchIndex(StreamSourceDTO origin) throws Exception {
		URI baseUri = new URI(origin.url.substring(0, origin.url.lastIndexOf("/") + 1));
		URI index = new URI(origin.url);

		// try (InputStream input = openStream(index)) {
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
	public List<ResourceDTO> download(List<ResourceDTO> remoteList) {
		
		List<ResourceDTO> ret = new ArrayList<>(remoteList.size());
		try {
		for (ResourceDTO loadable : remoteList) {
			File target = createLocalName(loadable); // the expected file name

			ResourceDTO local = download(loadable, target);
			ret.add(local);
		}
		} catch (Exception e) {
			LOG.warn("Problem fetching resources.",e);
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

	protected ResourceDTO download(ResourceDTO artifact, File target) throws Exception {
		if (!alreadyAvailable(target, artifact.getHash())) {
			LOG.info("Downloading yes!: " + target.getAbsolutePath());
			try {
				target.getParentFile().mkdirs();

				if ("file".equals(artifact.getUri().getScheme())) {
					download(new File(artifact.getUri()), target);
				} else {
					download(artifact.getUri(), target);
				}

			} catch (Exception e) {
				LOG.error("Unable to download " + artifact.getUri(), e);
				throw new RuntimeException("Unable to download",e);
			}
		} else {
			System.out.println("Already available: " + target.getAbsolutePath());
		}
		return new ResourceDTO(artifact.getOrigin(), target.toURI(), artifact.getHash());

	}

	private File createLocalName(ResourceDTO artifact) {
		return new File(definition.localPath, artifact.getOrigin().name + "/" + artifact.getUri().getPath());
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
		try {
		Response response = client.newCall(request).execute();
		ResponseBody body = response.body();

		// LOG.info("Loading " + body.contentLength() + " bytes of type " +
		// body.contentType() + "..");
		try (Source a = Okio.source(body.byteStream()); BufferedSink b = Okio.buffer(Okio.sink(target))) {
			b.writeAll(a);
		}
		}catch(Throwable e) {
			e.printStackTrace();
		}

	}

	protected void download(File uri, File target) throws Exception {
		try (Source a = Okio.source(uri); BufferedSink b = Okio.buffer(Okio.sink(target))) {
			b.writeAll(a);
		}
	}

}
