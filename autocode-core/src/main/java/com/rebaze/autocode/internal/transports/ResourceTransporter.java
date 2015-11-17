package com.rebaze.autocode.internal.transports;

import com.rebaze.commons.tree.Tree;

import java.io.File;
import java.io.IOException;

/**
 * A thing that can make a given object available offline.
 */
public interface ResourceTransporter
{
    boolean accept(String protocol);

    File transport( Tree tree );
}
