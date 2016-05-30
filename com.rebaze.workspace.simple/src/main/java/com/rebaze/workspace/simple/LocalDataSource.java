package com.rebaze.workspace.simple;

import java.io.File;
import java.net.URI;

import com.rebaze.workspace.api.DataSource;

public class LocalDataSource implements DataSource {

	private final URI uri;
	
	LocalDataSource(File f) {
		uri = f.toURI();
	}
	
	@Override
	public URI uri() {
		return uri;
	}

}
