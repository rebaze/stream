/*
 * Copyright (c) 2015 rebaze GmbH
 * All rights reserved.
 *
 * This library and the accompanying materials are made available under the terms of the Apache License Version 2.0,
 * which accompanies this distribution and is available at http://www.apache.org/licenses/LICENSE-2.0.
 *
 */
package com.rebaze.trees.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.tree.api.Selector;
import com.rebaze.tree.api.StreamTreeBuilder;
import com.rebaze.tree.api.Tag;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeBuilder;
import com.rebaze.tree.api.TreeException;

public class DefaultStreamTreeBuilder implements StreamTreeBuilder
{

    private static final Logger LOG = LoggerFactory.getLogger( DefaultStreamTreeBuilder.class );
    private long m_dataAmountRead = 0L;
    final private TreeBuilder m_delegate;

    public DefaultStreamTreeBuilder( final TreeBuilder delegate )
    {
        m_delegate = delegate;
    }

    /* (non-Javadoc)
	 * @see com.rebaze.trees.core.internal.StreamTreeBuilder#add(java.io.InputStream)
	 */
    @Override
	public StreamTreeBuilder add( final InputStream is )
        throws IOException
    {
        byte[] bytes = new byte[1024];
        int numRead = 0;
        while ( ( numRead = is.read( bytes ) ) >= 0 )
        {
            m_delegate.add( Arrays.copyOf( bytes, numRead ) );
            m_dataAmountRead += numRead;
        }
        return this;
    }

    @Override
    public StreamTreeBuilder add( final File f )
    {
        try
        {
            InputStream is = new FileInputStream( f );
            try
            {
                add( is );
            }
            finally
            {
                try
                {
                    is.close();
                }
                catch ( IOException e )
                {
                    LOG.warn( "Problem closing file " + f.getAbsolutePath(), e );
                }
            }
        }
        catch ( IOException ioE )
        {
            throw new TreeException( "Problem reading file " + f.getAbsolutePath() + " contents.", ioE );
        }
        return this;
    }

    public long getDataRead()
    {
        return m_dataAmountRead;
    }

    public void reset()
    {
        m_dataAmountRead = 0L;
    }

    @Override
    public DefaultStreamTreeBuilder add( byte[] bytes )
    {
        m_delegate.add( bytes );
        return this;
    }

    /* (non-Javadoc)
	 * @see com.rebaze.trees.core.internal.StreamTreeBuilder#selector(com.rebaze.tree.api.Selector)
	 */
	@Override
    public DefaultStreamTreeBuilder selector( Selector selector )
    {
        m_delegate.selector( selector );
        return this;
    }

    /* (non-Javadoc)
	 * @see com.rebaze.trees.core.internal.StreamTreeBuilder#branch(com.rebaze.tree.api.Selector)
	 */
    @Override
    public DefaultStreamTreeBuilder branch( Selector selector )
    {
        LOG.warn( "Branching from StreamTreeBuilder is pretty unusually as it means you add raw data to an intermediate tree. " );
        return new DefaultStreamTreeBuilder( m_delegate.branch( selector ) );
    }

    /* (non-Javadoc)
	 * @see com.rebaze.trees.core.internal.StreamTreeBuilder#branch(com.rebaze.tree.api.Tree)
	 */
	@Override
    public DefaultStreamTreeBuilder branch( Tree subtree )
    {
        LOG.warn( "Branching from StreamTreeBuilder is pretty unusually as it means you add raw data to an intermediate tree. " );
        return new DefaultStreamTreeBuilder( m_delegate.branch( subtree ) );
    }

    @Override
    public DefaultStreamTreeBuilder tag( Tag tag )
    {
        m_delegate.tag( tag );
        return this;
    }

    @Override
    public Tree seal()
    {
        return m_delegate.seal();
    }
}
