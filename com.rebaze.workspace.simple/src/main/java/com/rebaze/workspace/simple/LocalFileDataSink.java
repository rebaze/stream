package com.rebaze.workspace.simple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.tree.api.HashAlgorithm;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeSession;
import com.rebaze.workspace.api.DataSink;
import com.rebaze.workspace.api.ResourceLink;
import com.rebaze.workspace.api.ResourceLinkAvailableConsumer;

public class LocalFileDataSink implements DataSink {

	private static final Logger LOG = LoggerFactory.getLogger(LocalFileDataSink.class);

	private FileOutputStream out;
	
	private Tree tree;

	private final TreeSession session;
	
	private File f;

	private final File cache;

	private ResourceLinkAvailableConsumer consumer;

	private ResourceDTO artifact;
		
	public LocalFileDataSink(File tmpFolder, ResourceDTO artifact, TreeSession session, ResourceLinkAvailableConsumer consumer) {
		this.session = session;
		this.cache = tmpFolder;
		this.consumer = consumer;
		this.artifact = artifact;
		this.cache.mkdirs();
	}
	
	@Override
	public ResourceLink finish() throws IOException {
		if (out != null) {
			out.close();
			out = null;
		}
		if (f != null) {
			
			tree = session.createStreamTreeBuilder().add(f).seal();
			// now copy f to its final location:
			File folder = new File(cache,tree.fingerprint());
			folder.mkdirs();
			String name = artifact.getUri().getPath();
			name = name.substring(name.lastIndexOf("/"));
			File fStorageName = new File(folder, name );
			// copy:
			if (!fStorageName.exists()) {
				Files.copy(f.toPath(),fStorageName.toPath());
				LOG.info("Copied a resource to its final location: " + fStorageName.getAbsolutePath());
			}else {
				LOG.warn("copied a file that already exists in store: " + fStorageName.getAbsolutePath());
			}
			// now we copy over the tmp file to the store:
			ResourceLink link = new ResourceLink(tree);//
			if (consumer != null) {
				consumer.resourceLinkAvailable( link, new LocalDataSource(fStorageName));
			}
			return link;
		}else {
			throw new IOException("No open stream.");
		}
	}

	@Override
	public OutputStream openStream() {
		try {
			f = File.createTempFile("cache", "", cache);
			f.deleteOnExit();
			return new FileOutputStream(f);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open tmp file in " + cache,e);
		}
	}

}
