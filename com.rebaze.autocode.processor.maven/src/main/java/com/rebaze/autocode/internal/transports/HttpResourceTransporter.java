package com.rebaze.autocode.internal.transports;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.ops4j.store.Handle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.autocode.config.WorkspaceConfiguration;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

/**
 *
 */
@Component(service=ResourceTransporter.class)
public class HttpResourceTransporter extends AbstractResourceTransporter {
	
	private final static Logger LOG = LoggerFactory.getLogger(HttpResourceTransporter.class);
	private OkHttpClient client = new OkHttpClient();
	
	@Reference(bind="setConfiguration") 
	WorkspaceConfiguration configuration;
	
	protected File download(URL url) throws IOException {
		Request request = new Request.Builder().url(url).build();
		Response response = client.newCall(request).execute();
		ResponseBody body = response.body();
		LOG.info("Loading " + body.contentLength() + " bytes of type " + body.contentType() + "..");
		Handle handle = getStore().store(body.byteStream());
		LOG.info("DONE: " + handle.getIdentification());
		return new File(getStore().getLocation(handle).getPath());
	}

	@Override
	public boolean accept(String protocol) {
		return protocol.startsWith("http");
	}
}
