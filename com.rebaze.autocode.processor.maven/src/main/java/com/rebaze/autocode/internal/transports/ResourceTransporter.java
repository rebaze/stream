package com.rebaze.autocode.internal.transports;

import java.io.File;

import com.rebaze.tree.api.Tree;

/**
 * A thing that can make a given object available offline.
 */
public interface ResourceTransporter
{
    boolean accept(String protocol);

    File transport( Tree tree );
}
