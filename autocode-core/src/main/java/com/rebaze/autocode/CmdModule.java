package com.rebaze.autocode;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.rebaze.autocode.api.core.AutocodeException;
import okio.Okio;
import okio.Source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by tonit on 23/11/15.
 */
public class CmdModule extends AbstractModule
{
    public static final String NAME_LOCATIONS = "/autocode-locations.json";
    public static final String NAME_TOOLCHAIN = "/autocode-universe.json";
    public static final String NAME_TREE = "/autocode-tree.json";
    private final File m_baseConfig;

    public CmdModule(File baseConfigFolder)
    {
        m_baseConfig = baseConfigFolder;
    }

    @Override protected void configure()simpl
    {
        try
        {
            bind(Source.class).annotatedWith( Names.named("sites") ).toInstance( Okio.source( getResource( NAME_LOCATIONS ) ) );
            bind(Source.class).annotatedWith( Names.named("tree") ).toInstance( Okio.source( getResource( NAME_TREE ) ) );
            bind(Source.class).annotatedWith( Names.named("universe") ).toInstance( Okio.source( getResource( NAME_TOOLCHAIN ) ) );
        }
        catch ( FileNotFoundException e )
        {
            throw new AutocodeException( "Problem configuration this autocode session." );
        }
    }

    private InputStream getResource( String name ) throws FileNotFoundException
    {
        return new FileInputStream( new File(m_baseConfig,name) );
    }
}

