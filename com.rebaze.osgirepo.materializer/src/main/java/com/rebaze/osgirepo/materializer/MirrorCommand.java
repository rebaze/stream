package com.rebaze.osgirepo.materializer;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.felix.service.command.Descriptor;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.index.api.IndexAdmin;
import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.mirror.api.ResourceDTO;


@Component(immediate=true, service = MirrorCommand.class, property = 
	{
			"osgi.command.scope=stream",
			"osgi.command.function=build"
	})
public class MirrorCommand {

	private final Logger LOG = LoggerFactory.getLogger(MirrorCommand.class);

	@Reference 
	IndexAdmin indexAdmin; 
	
	@Reference 
	MirrorAdmin mirrorAdmin; 
	
	@Activate
	private void activate(ComponentContext context ) {
		LOG.info("# Activating " + context.getProperties().get("component.name"));
	}
	
	@Deactivate
	private void deactivate(ComponentContext context ) {
		LOG.info("# Deactivating " + context.getProperties().get("component.name"));
	}
	
//	private StreamDefinitionDTO definition;
	
	@Descriptor("build")
	public void build() {
		
		System.out.println("MirrorCommand called!");
		try {
			final CompletableFuture<List<ResourceDTO>> future = 
				    CompletableFuture.supplyAsync(() -> mirrorAdmin.fetchResources());
			// chain up the next steps:
			future
			.thenApply(resource -> mirrorAdmin.download(resource))
			.thenApply(local -> indexAdmin.index(local))
			.thenApply(indexes -> indexAdmin.compositeIndex(indexes));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
