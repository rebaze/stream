package com.rebaze.stream.app;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.mirror.api.ResourceDTO;

@Component(property="type=composite")
public class CompositeMirrorAdmin implements MirrorAdmin {

	@Reference (target="(!(type=composite))")
	private List<MirrorAdmin> childs; 
	
	private static final Logger LOG = LoggerFactory.getLogger(CompositeMirrorAdmin.class);
	
	@Override
	public List<ResourceDTO> fetchResources() {
		// all resources:
		List<ResourceDTO> res = new ArrayList<>();
		System.out.println( "Running " + childs.size() + " mirrors." );
		for (MirrorAdmin adm : childs) {
		    LOG.info("Running " + adm);
		   res.addAll(adm.fetchResources());
		}
		return res;
	}
	
	@Override
	public String toString() {
	    return "[CompositeMirrorAdmin]";
	}


}
