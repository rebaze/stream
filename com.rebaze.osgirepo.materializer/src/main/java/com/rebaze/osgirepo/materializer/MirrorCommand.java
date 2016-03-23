package com.rebaze.osgirepo.materializer;
import java.net.URI;
import java.util.List;

import org.apache.felix.service.command.Descriptor;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
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
			List<ResourceDTO> res = mirrorAdmin.fetchResources();
			List<ResourceDTO> local =  mirrorAdmin.download(res);
			List<URI> indexes = indexAdmin.index(local);
			indexAdmin.compositeIndex(indexes);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
