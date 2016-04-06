package com.rebaze.workspace.simple;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeSessionFactory;
import com.rebaze.workspace.WorkspaceAdmin;

@Component
public class SimpleWorkspaceAdmin implements WorkspaceAdmin {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleWorkspaceAdmin.class);

	@Reference
	private TreeSessionFactory treeFactory;
	
	@Reference
	StreamDefinitionDTO definition;
	
	@Override
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
	public boolean existsInWorkspace(ResourceDTO res) {
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
	
}
