package com.rebaze.stream.api;

import org.osgi.dto.DTO;

public class StreamDefinitionDTO extends DTO {
	
	public String name;
	public String version;
	public String localPath;
	public String hashAlgorithm = "SHA-256";
	public StreamSourceDTO[] sources;
	public String compositeURI;
	
}
