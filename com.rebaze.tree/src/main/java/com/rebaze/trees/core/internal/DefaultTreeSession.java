/*
 * Copyright (c) 2012-2013 rebaze GmbH
 * All rights reserved. 
 * 
 * This library and the accompanying materials are made available under the terms of the Apache License Version 2.0,
 * which accompanies this distribution and is available at http://www.apache.org/licenses/LICENSE-2.0.
 *
 */
package com.rebaze.trees.core.internal;

import com.rebaze.tree.api.Selector;
import com.rebaze.tree.api.StreamTreeBuilder;
import com.rebaze.tree.api.Tag;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeBuilder;
import com.rebaze.tree.api.TreeException;
import com.rebaze.tree.api.TreeSession;
import com.rebaze.trees.ext.operators.IntersectTreeCombiner;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Set of tools for this API.
 *
 * @author Toni Menzel <toni.menzel@rebaze.com>
 */
@Component(scope=ServiceScope.SINGLETON)
public class DefaultTreeSession implements TreeSession {
	public static final String DEFAULT_HASH_ALOGO = "SHA-256";
	public static final String CHARSET_NAME = "UTF-8";
	// private String m_messageDigestAlgorithm = DEFAULT_HASH_ALOGO;
	private final MessageDigest m_digest;

	public DefaultTreeSession() {
		this(DEFAULT_HASH_ALOGO);
	}

	public DefaultTreeSession(String digestAlg) {
		m_digest = createMessageDigest(digestAlg);
	}

	/**
	 * counts the total number of nodes of this tree.
	 *
	 * @param tree
	 *            input
	 * @return number of sub trees.
	 */
	public static long nodes(Tree tree) {
		int total = 1;
		for (Tree sub : tree.branches()) {
			total += nodes(sub);
		}
		return total;
	}

	/**
	 * counts the total number of leafs of this tree.
	 *
	 * @param tree
	 *            input
	 * @return number of leafs.
	 */
	public static long leafs(Tree tree) {
		int total = 0;
		for (Tree sub : tree.branches()) {
			if (sub.branches().length == 0) {
				total++;
			} else {
				total += leafs(sub);
			}
		}
		return total;
	}

	public static boolean isRaw(Tree tree) {
		if (tree.branches().length == 0)
			return true;

		if (tree.branches().length == 1 && tree.fingerprint().equals(tree.branches()[0].fingerprint())) {
			return isRaw(tree.branches()[0]);
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rebaze.tree.api.TreeSession#createTreeBuilder()
	 */
	@Override
	public TreeBuilder createTreeBuilder() {
		return new InMemoryTreeBuilderImpl(this, m_digest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rebaze.tree.api.TreeSession#createStreamTreeBuilder()
	 */
	@Override
	public StreamTreeBuilder createStreamTreeBuilder() {
		return new DefaultStreamTreeBuilder(createTreeBuilder());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rebaze.tree.api.TreeSession#createStreamTreeBuilder(com.rebaze.tree.
	 * api.TreeBuilder)
	 */
	@Override
	public StreamTreeBuilder createStreamTreeBuilder(TreeBuilder delegate) {
		return new DefaultStreamTreeBuilder(delegate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rebaze.tree.api.TreeSession#createTree(com.rebaze.tree.api.Selector,
	 * java.lang.String, com.rebaze.tree.api.Tree[], com.rebaze.tree.api.Tag)
	 */
	@Override
	public Tree createTree(Selector selector, String hashValue, Tree[] subs, Tag tag) {
		return new InMemoryTreeImpl(selector, hashValue, subs, tag);
	}

	private MessageDigest createMessageDigest(String digest) {
		try {
			return MessageDigest.getInstance(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new TreeException("Problem loading digest with algorthm: digest",e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rebaze.tree.api.TreeSession#createStaticTreeBuilder(com.rebaze.tree.
	 * api.Tree)
	 */
	@Override
	public TreeBuilder createStaticTreeBuilder(Tree tree) {
		return new StaticTreeBuilder(tree, this);
	}

	public static TreeIndex wrapAsIndex(Tree tree) {
		if (tree instanceof TreeIndex) {
			return (TreeIndex) tree;
		} else {
			return new TreeIndex(tree);
		}
	}

	public static byte[] asByteArray(String s) {
		try {
			return s.getBytes(CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Is not " + CHARSET_NAME + " ??");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rebaze.tree.api.TreeSession#find(com.rebaze.tree.api.Tree,
	 * com.rebaze.tree.api.Tree)
	 */
	@Override
	public Tree find(Tree base, Tree subtree) {
		// TreeBuilder builder = createTreeBuilder();
		// basically copy the whole input tree but erase all leafs
		// that do not mach the subtree.

		return new IntersectTreeCombiner(this).combine(base, subtree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rebaze.tree.api.TreeSession#createTree(com.rebaze.tree.api.Selector,
	 * java.lang.String)
	 */
	@Override
	public Tree createTree(Selector selector, String hashValue) {
		return createTree(selector, hashValue, new Tree[0], Tag.tag());
	}

	@Override
	public Tree reduce(Tree tree) {
		if (tree.branches().length == 1) {
			return reduce(tree.branches()[0]);
		}
		return tree;
	}

}
