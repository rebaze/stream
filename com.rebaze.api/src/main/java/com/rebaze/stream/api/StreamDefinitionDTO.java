package com.rebaze.stream.api;

import org.osgi.dto.DTO;

public class StreamDefinitionDTO extends DTO {
	
	public String name;
	public String version;
	public StreamSourceDTO[] sources;
	public String compositeURI;
	
}
