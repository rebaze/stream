package com.rebaze.workspace.simple;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.mirror.api.ResourceDTO.HashType;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeSession;
import com.rebaze.tree.api.TreeSessionFactory;
import com.rebaze.workspace.api.DataSink;
import com.rebaze.workspace.api.DataSource;
import com.rebaze.workspace.api.ResourceLink;
import com.rebaze.workspace.api.ResourceLinkAvailableConsumer;
import com.rebaze.workspace.api.WorkspaceAdmin;

@Component (immediate = true)
public class SimpleWorkspaceAdmin implements WorkspaceAdmin, ResourceLinkAvailableConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleWorkspaceAdmin.class);

	@Reference
	private TreeSessionFactory treeFactory;
	
	@Reference 
	private TreeSession treeSession;
		
	// a redundant store for all incoming links:
	private final Map<ResourceLink,DataSource> store = new HashMap<>();
	
	@Reference
	StreamDefinitionDTO definition;

	private File wsLocation;
	
	
	@Activate
	public void activation() {
		wsLocation = new File(definition.localPath + "/workspace");
		LOG.info("Initialized simple worspace at: " + wsLocation);
		indexExisting(wsLocation);	
	}
	
	private void indexExisting(File folder) {
		for (File f : folder.listFiles()) {
			if (f.isFile()) {
				resourceLinkAvailable(linkFromPath(f), new LocalDataSource(f) );
			}else {
				indexExisting(f);
			}
		}
		
	}

	private ResourceLink linkFromPath(File f) {
		Tree tree = treeFactory.create("SHA-256").createStreamTreeBuilder().add( f ).seal();
		return new ResourceLink(HashType.SHA256, tree.fingerprint());
	}

	// only when hash is not available
	public File getPathFor(ResourceDTO artifact) {
		
		try {
			String resultPath = artifact.getUri().getPath() ;
			String p = new URI(artifact.getOrigin().url).getPath();
			String a = artifact.getUri().getPath();
			if (p.lastIndexOf('.') > p.lastIndexOf('/')) {
				p = p.substring(0, p.lastIndexOf("/"));
			}
			if (a.startsWith(p)) {
				resultPath = a.substring(p.length());
			}
			
			return new File(definition.localPath, artifact.getOrigin().name + "/" + resultPath);

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public DataSource resolve(ResourceDTO artifact) {
		return resolve( new ResourceLink(artifact.getHashType(),artifact.getHash()));
	}
	
	public boolean existsInWorkspaceLegacy(ResourceDTO res) {
		
		File target = getPathFor(res);
		if (target.exists()) {
			// TODO: Cache this:
			Tree tree = treeFactory.create(res.getHashType().value()).createStreamTreeBuilder().add(target).seal();
			if (res.getHash() == null || !res.getHash().equals(tree.fingerprint())) {
				LOG.warn("Bad checksum: " + res.getHash() + "(we have " + tree.fingerprint() + "): Redownload.");
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public DataSink sink(ResourceDTO artifact) {
		return new LocalFileDataSink(wsLocation, artifact, treeSession,this);
	}

	@Override
	public void resourceLinkAvailable(ResourceLink link, DataSource src) {
		// create secondary links:
		try {
			Tree tree = treeFactory.create(HashType.MD5.name()).createStreamTreeBuilder().add(src.uri().toURL().openStream()).seal();
			ResourceLink linkMD5 = new ResourceLink(HashType.MD5,tree.fingerprint());
			System.out.println("ADDED " + linkMD5);
			System.out.println("+ (primary: " + src.uri().toASCIIString() + ") ADDED " + link);
			store.put(linkMD5,src);
			store.put(link,src);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public DataSource resolve(ResourceLink link) {
		DataSource res = store.get(link);
		System.out.println("RESOLVE " + link + " --> " + res);

		return res;
	}
	
}
