package com.rebaze.transport.http;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.transport.api.TransportAgent;
import com.rebaze.transport.api.TransportMonitor;
import com.rebaze.tree.api.Tree;
import com.rebaze.workspace.WorkspaceAdmin;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * This loads remote resources into a local workspace.
 * 
 * @author tonit
 *
 */
@Component
public class HttpTransporter implements TransportAgent {
	
	private static final Logger LOG = LoggerFactory.getLogger(HttpTransporter.class);

	@Reference
	private WorkspaceAdmin workspaceAdmin;
	
	private final OkHttpClient client = new OkHttpClient();

	@Override
	public List<ResourceDTO> transport(TransportMonitor monitor, List<ResourceDTO> resources) {
		List<ResourceDTO> ret = new ArrayList<>(resources.size());
		try {
			for (ResourceDTO loadable : resources) {
				File target = workspaceAdmin.getPathFor(loadable); // the expected file
															// name
				ResourceDTO local = download(loadable, target, monitor);
				if (local != null) {
					ret.add(local);
				}
			}
		} catch (Exception e) {
			LOG.warn("Problem fetching resources.", e);
		}
		return ret;
	}
	
	protected ResourceDTO download(ResourceDTO artifact, File target, TransportMonitor monitor) throws Exception {
		if (!workspaceAdmin.existsInWorkspace(artifact)) {
			try {
				target.getParentFile().mkdirs();
				monitor.transporting(artifact);
				if ("file".equals(artifact.getUri().getScheme())) {
					download(new File(artifact.getUri()), target);
				} else {
					download(artifact.getUri(), target);
				}

			} catch (Exception e) {
				LOG.error("Unable to download " + artifact.getUri(), e);
				throw new RuntimeException("Unable to download", e);
			}
			return new ResourceDTO(artifact.getOrigin(), target.toURI(), artifact.getHash(),artifact.getHashType());
		}else {
			return null;
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
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	protected void download(File uri, File target) throws Exception {
		try (Source a = Okio.source(uri); BufferedSink b = Okio.buffer(Okio.sink(target))) {
			b.writeAll(a);
		}
	}


}
