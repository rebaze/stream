package com.rebaze.autocode.internal.transports;

import com.rebaze.trees.core.Tree;

import java.io.File;

/**
 * A thing that can make a given object available offline.
 */
public interface ResourceTransporter
{
    boolean accept(String protocol);

    File transport( Tree tree );
}
