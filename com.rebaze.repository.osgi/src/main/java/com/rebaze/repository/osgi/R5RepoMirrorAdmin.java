package com.rebaze.repository.osgi;

import java.io.IOException;
import java.io.InputStream;
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

import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamSourceDTO;

import aQute.bnd.deployer.repository.api.Decision;
import aQute.bnd.deployer.repository.api.IRepositoryContentProvider;
import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;
import okio.BufferedSource;
import okio.Okio;

@Component(property = "type=r5")
public class R5RepoMirrorAdmin implements MirrorAdmin {

	private static final Logger LOG = LoggerFactory.getLogger(R5RepoMirrorAdmin.class);

	private static final String TYPE = "org.osgi.r5";
	
	@Reference(target="(&(type="+TYPE+")(active=true))")
	private List<StreamSourceDTO> source;

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
			for (StreamSourceDTO s : source) {
				resources.addAll(fetchIndex(s));
			}
		} catch (Exception e) {
			LOG.error("Problem fetching resources.",e);
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
	
	@Override
	public String toString() {
	    return "[R5RepoMirrorAdmin]";
	}
}
