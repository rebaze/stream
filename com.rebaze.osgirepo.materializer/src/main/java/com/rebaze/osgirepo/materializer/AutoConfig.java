package com.rebaze.osgirepo.materializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebaze.stream.api.StreamDefinitionDTO;

import okio.Okio;

@Component
public class AutoConfig {
	
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	private ServiceRegistration<StreamDefinitionDTO> reg;
	
	@Activate
	public void config(BundleContext ctx) throws IOException {
		// See if we find a appropriate file to load:
		File f = new File("basic-stream.json");
		if (f.exists()) {
			try (InputStream stream = new FileInputStream(f)) {
				StreamDefinitionDTO definition = gson.fromJson(
						new InputStreamReader(Okio.buffer(Okio.source(stream)).inputStream()),
						StreamDefinitionDTO.class);
				definition.localPath = new File("target/repo").getAbsolutePath();
				 reg = ctx.registerService(StreamDefinitionDTO.class, definition, null);
				System.out.println("Configuration ("+reg.toString()+") successfully created: " + definition);
			}
		}else {
			throw new RuntimeException("Descriptor " + f.getAbsolutePath() + " not available!");
		}
	}
	
	@Deactivate
	public void deactivate(BundleContext ctx) throws IOException {
		if (reg != null) {
			reg.unregister();
		}
	}
	
}
