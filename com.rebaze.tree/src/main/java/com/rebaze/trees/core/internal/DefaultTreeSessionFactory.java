package com.rebaze.trees.core.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import com.rebaze.tree.api.HashAlgorithm;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeSession;
import com.rebaze.tree.api.TreeSessionFactory;

/**
 * Standard impl without any DI fw.
 */
@Component(scope=ServiceScope.SINGLETON)
public class DefaultTreeSessionFactory implements TreeSessionFactory {
	
	@Override
	public TreeSession getTreeSession(HashAlgorithm digestAlo) {
		return new DefaultTreeSession(digestAlo);
	}

	public static boolean isWrapper(Tree tree) {
		return (tree.branches().length == 1 && tree.fingerprint().equals(tree.branches()[0].fingerprint()));
	}
}
