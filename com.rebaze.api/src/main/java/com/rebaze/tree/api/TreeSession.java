package com.rebaze.tree.api;

public interface TreeSession {

	TreeBuilder createTreeBuilder();

	StreamTreeBuilder createStreamTreeBuilder();

	StreamTreeBuilder createStreamTreeBuilder(TreeBuilder delegate);

	Tree createTree(Selector selector, String hashValue, Tree[] subs, Tag tag);

	TreeBuilder createStaticTreeBuilder(Tree tree);

	Tree find(Tree base, Tree subtree);

	Tree createTree(Selector selector, String hashValue);

	Tree reduce(Tree tree);

}