package com.rebaze.autocode;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import okio.Okio;
import okio.Source;

import javax.inject.Named;

/**
 * Created by tonit on 23/11/15.
 */
public class CmdModule extends AbstractModule
{
    @Override protected void configure()
    {
        //bind(Source.class).toInstance( Okio.source( getClass().getResourceAsStream( "/autocode-locations.json" ) ) );

    }

    @Provides @Named( "sites" )
    public Source defaultSites()
    {
        return Okio.source( getClass().getResourceAsStream( "/autocode-locations.json" ) );
    }

    @Provides @Named( "universe" )
    public Source defaultConfiguration()
    {
        return Okio.source( getClass().getResourceAsStream( "/autocode-universe.json" ) );
    }

    @Provides @Named( "tree" )
    public Source defaultTree()
    {
        return Okio.source( getClass().getResourceAsStream( "/autocode-tree.json" ) );
    }
}

