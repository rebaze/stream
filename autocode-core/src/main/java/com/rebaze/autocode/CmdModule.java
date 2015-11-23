package com.rebaze.autocode;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import okio.Okio;
import okio.Source;

import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by tonit on 23/11/15.
 */
public class CmdModule extends AbstractModule
{
    public static final String NAME_LOCATIONS = "autocode-locations.json";
    public static final String NAME_TOOLCHAIN = "autocode-universe.json";
    public static final String NAME_TREE = "autocode-tree.json";
    private final File m_baseConfig;

    public CmdModule(File baseConfigFolder)
    {
        m_baseConfig = baseConfigFolder;
    }

    @Override protected void configure()
    {

    }

    @Provides @Named( "sites" )
    public Source defaultSites() throws FileNotFoundException
    {
        return Okio.source( getResource( "/" + NAME_LOCATIONS ) );
    }

    @Provides @Named( "universe" )
    public Source defaultConfiguration() throws FileNotFoundException
    {
        return Okio.source( getResource( "/" + NAME_TOOLCHAIN ) );
    }

    @Provides @Named( "tree" )
    public Source defaultTree() throws FileNotFoundException
    {
        return Okio.source( getResource( "/" + NAME_TREE ) );
    }

    private InputStream getResource( String name ) throws FileNotFoundException
    {
        return new FileInputStream( new File(m_baseConfig,name) );
    }
}

