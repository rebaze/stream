package com.rebaze.workspace.simple;

import java.io.File;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeSession;
import com.rebaze.tree.api.TreeSessionFactory;
import com.rebaze.workspace.WorkspaceAdmin;

@Component
public class SimpleWorkspaceAdmin implements WorkspaceAdmin {

	@Reference
	private TreeSessionFactory treeFactory;
	
	@Reference
	private StreamDefinitionDTO definition;
	
	@Override
	public File getPathFor(ResourceDTO artifact) {
		return new File(definition.localPath, artifact.getOrigin().name + "/" + artifact.getUri().getPath());
	}

	@Override
	public boolean existsInWorkspace(ResourceDTO res) {
		File target = getPathFor(res);
		if (target.exists()) {
			// TODO: Cache this:
			Tree tree = treeFactory.create(res.getHashType().name()).createStreamTreeBuilder().add(target).seal();
			if (res.getHash() == null || !res.getHash().equals(tree.fingerprint())) {
				System.err.println("Bad checksum: " + res.getHash() + "(we have " + tree.fingerprint() + "): Redownload.");
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
}
