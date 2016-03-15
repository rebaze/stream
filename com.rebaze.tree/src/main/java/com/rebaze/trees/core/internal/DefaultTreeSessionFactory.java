package com.rebaze.trees.core.internal;

import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeSession;
import com.rebaze.tree.api.TreeSessionFactory;

/**
 * Standard impl without any DI fw.
 */
public class DefaultTreeSessionFactory implements TreeSessionFactory {
	@Override
	public TreeSession create() {
		return new DefaultTreeSession(DefaultTreeSession.DEFAULT_HASH_ALOGO);
	}

	@Override
	public TreeSession create(String digestAlo) {
		return new DefaultTreeSession(digestAlo);
	}

	public static boolean isWrapper(Tree tree) {
		return (tree.branches().length == 1 && tree.fingerprint().equals(tree.branches()[0].fingerprint()));
	}
}
