package com.rebaze.tree.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface StreamTreeBuilder extends TreeBuilder {

	StreamTreeBuilder add(InputStream is) throws IOException;

	StreamTreeBuilder selector(Selector selector);

	StreamTreeBuilder branch(Selector selector);

	StreamTreeBuilder branch(Tree subtree);

	StreamTreeBuilder add(File f);

}