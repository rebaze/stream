package com.rebaze.workspace.api;

import java.io.IOException;
import java.io.OutputStream;

public interface DataSink {
	
	OutputStream openStream();
	
	ResourceLink finish() throws IOException;
		
}
