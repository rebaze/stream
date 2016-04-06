package com.rebaze.workspace.simple;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.rebaze.stream.api.StreamSourceDTO;

import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;

@Component
public class WorkspaceEndpoint implements REST {
	
	@Reference
	private List<StreamSourceDTO> source;
	
	public List<StreamSourceDTO> getSources(RESTRequest req) {
		
		return source;
	}
}
