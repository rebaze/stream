package com.rebaze.transport.http;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.transport.api.TransportAgent;
import com.rebaze.transport.api.TransportMonitor;
import com.rebaze.tree.api.Tree;
import com.rebaze.workspace.api.DataSink;
import com.rebaze.workspace.api.DataSource;
import com.rebaze.workspace.api.ResourceLink;
import com.rebaze.workspace.api.WorkspaceAdmin;
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
	
	@Reference 
	private EventAdmin eventAdmin;
	
	
	private final OkHttpClient client = new OkHttpClient();

	@Override
	public List<ResourceLink> transport(TransportMonitor monitor, List<ResourceDTO> resources) {
		List<ResourceLink> sources = new ArrayList<>();		
		try {
			for (ResourceDTO loadable : resources) {
				ResourceLink res = download(loadable, monitor);
				if (res != null) {
					sources.add( res );
				}
			}
		} catch (Exception e) {
			LOG.warn("Problem fetching resources.", e);
		}
		return sources;
	}
	
	protected ResourceLink download(ResourceDTO artifact, TransportMonitor monitor) throws Exception {
		DataSource resolved = workspaceAdmin.resolve(artifact);
		if (resolved == null) {
			try {
				DataSink target = workspaceAdmin.sink(artifact);
				monitor.transporting(artifact);
				if ("file".equals(artifact.getUri().getScheme())) {
					download(new File(artifact.getUri()), target);
				} else {
					download(artifact.getUri(), target);
				}
				
				//return new ResourceDTO(artifact.getOrigin(), target.getLink().toUri(), artifact.getHash(),artifact.getHashType());
				// now that we get a handle, we might 
				return target.finish();
			} catch (Exception e) {
				LOG.error("Unable to download " + artifact, e);
				throw new RuntimeException("Unable to download", e);
			}
		}else {
			return null;
		}
	}

	protected void download(URI uri, DataSink target) throws Exception {
		Request request = new Request.Builder().url(uri.toURL()).build();
		try {
			Response response = client.newCall(request).execute();
			ResponseBody body = response.body();
			// LOG.info("Loading " + body.contentLength() + " bytes of type " +
			// body.contentType() + "..");
			try (Source a = Okio.source(body.byteStream()); BufferedSink b = Okio.buffer(Okio.sink(target.openStream()))) {
				b.writeAll(a);
			}
			
		} catch (Throwable e) {
			throw new RuntimeException("Cannot download resource " +  uri,e);
		}


	}

	protected void download(File uri, DataSink target) throws Exception {
		try (Source a = Okio.source(uri); BufferedSink b = Okio.buffer(Okio.sink(target.openStream()))) {
			b.writeAll(a);
		}
	
		
	}


}
