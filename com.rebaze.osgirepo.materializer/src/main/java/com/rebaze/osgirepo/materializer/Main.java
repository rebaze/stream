package com.rebaze.osgirepo.materializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeBuilder;
import com.rebaze.tree.api.TreeSession;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import aQute.bnd.deployer.repository.api.Decision;
import aQute.bnd.deployer.repository.api.IRepositoryContentProvider;
import aQute.bnd.deployer.repository.api.IRepositoryIndexProcessor;
import aQute.bnd.deployer.repository.api.Referral;
import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class Main {

	private final Logger LOG = LoggerFactory.getLogger(Main.class);

	private final IRepositoryContentProvider[] providers;
	private final File baseFolder;
	private final IRepositoryIndexProcessor processor;

	private Set<File> files;

	public Main(File base, IRepositoryContentProvider... contentProviders) {
		baseFolder = base;
		providers = contentProviders;
		files = new HashSet<File>();
		processor = new CloningRepositoryIndexProcessor(base, files);
	}

	private File materialize(URI index) throws Exception {
		// calculate from index uri:
		String uri = index.toASCIIString();
		URI baseUri = new URI(uri.substring(0, uri.lastIndexOf("/") + 1));
		System.out.println("Calculated baseuri= " + baseUri);
		return materialize(index, baseUri);

	}

	private File materialize(URI index, URI baseUri) throws Exception {

		InputStream input = index.toURL().openStream();
		IRepositoryContentProvider provider = null;
		for (IRepositoryContentProvider p : this.providers) {
			if (p.checkStream(index.getPath(), index.toURL().openStream()).getDecision() == Decision.accept) {
				provider = p;
				break;
			}
		}
		if (provider != null) {
			// Download!
			provider.parseIndex(input, baseUri, processor, null);
			File indexFileName = new File(baseFolder, getFileName(index.getPath()));
			indexFileName.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(indexFileName);
			String repoName = "Mirror " + baseFolder.getName();
			System.out.println("Generating index (" + indexFileName.getName() + ") for " + files.size()
					+ " files in repo: " + repoName);
			provider.generateIndex(files, out, repoName, indexFileName.getParentFile().toURI(), true, null, null);
			out.close();
			return indexFileName;
		} else {
			throw new RuntimeException("Unsupported repository type! " + index.toASCIIString());
		}

	}

	private String getFileName(String path) {
		int idx = path.lastIndexOf("/");
		if (idx >= 0) {
			return path.substring(idx + 1);
		} else {
			return path;
		}
	}

	public static class CloningRepositoryIndexProcessor implements IRepositoryIndexProcessor {

		private OkHttpClient client = new OkHttpClient();
		private final File baseFolder;
		private final Set<File> files;
		private TreeSession treeSession;

		CloningRepositoryIndexProcessor(File base, Set<File> files) {
			this.baseFolder = base;
			this.files = files;
			this.treeSession = new TreeSession();
			treeSession.setDigestAlgorithm("SHA-256");
		}

		@Override
		public void processResource(Resource resource) {
			System.out.println("processResource: " + resource);
			List<Capability> cap = resource.getCapabilities("osgi.content");

			for (Capability c : cap) {
				URI url = (URI) c.getAttributes().get("url");
				String hash = (String) c.getAttributes().get("osgi.content");
				File f;
				try {
					f = download(url,hash);
					if (f != null) {
						files.add(f);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		}

		@Override
		public void processReferral(URI parentUri, Referral referral, int maxDepth, int currentDepth) {
			System.out.println("processReferral: " + referral.getUrl());

		}

		protected File download(URI uri, String hash) throws Exception {
			File target = new File(baseFolder, uri.getPath());
			if (!alreadyAvailable(target,hash)) {

				target.getParentFile().mkdirs();

				if ("file".equals(uri.getScheme())) {
					download(new File(uri), target);
				} else {
					download(uri, target);

				}
				System.out.println("Cloned: " + target.getAbsolutePath());
			}else {
				System.out.println("Already available: " + target.getAbsolutePath());

			}
			return target;

		}

		private boolean alreadyAvailable(File target, String hash) {
			// TODO use hash!
			if (target.exists()) {
				Tree tree = treeSession.createStreamTreeBuilder().add(target).seal();
				if (!hash.equals(tree.fingerprint())) {
					System.err.println("Bad checksum: " + hash +  "(we have " + tree.fingerprint() + "): Redownload.");
					return false;
				}else {
					return true;
				}
			}else {
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

	public static void main(String[] args) throws Exception {
		mirror(new File("target/repo/bnd_latest"), new URI(
				"https://bndtools.ci.cloudbees.com/job/bnd.master/lastSuccessfulBuild/artifact/dist/bundles/index.xml"));
		mirror(new File("target/repo/amdatu_release"), new URI("http://repository.amdatu.org/release/index.xml"));
		mirror(new File("target/repo/maven_deps"),
				new File("/Users/tonit/devel/rebaze/autocode/com.rebaze.maven.dependencies/target/index.xml")
						.getAbsoluteFile().getCanonicalFile().toURI());
	}

	private static File mirror(File target, URI index) throws Exception {
		File localIndex = new Main(target, new R5RepoContentProvider()).materialize(index);
		System.out.println("Mirrored " + index.toASCIIString() + " to " + localIndex.getAbsolutePath());
		return localIndex;
	}

}
