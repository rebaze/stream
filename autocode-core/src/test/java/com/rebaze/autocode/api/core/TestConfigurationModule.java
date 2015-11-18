package com.rebaze.autocode.api.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import okio.Okio;
import okio.Source;

import javax.inject.Named;

/**
 * Just binds external sources and possibly local dependency systems.
 */
public class TestConfigurationModule extends AbstractModule
{
    @Override protected void configure()
    {
        //bind(Source.class).toInstance( Okio.source( getClass().getResourceAsStream( "/autocode-locations.json" ) ) );

    }

    @Provides @Named("sites")
    public Source defaultSites() {
        return Okio.source( getClass().getResourceAsStream( "/autocode-locations.json" ) );
    }

    @Provides @Named ("universe")
    public Source defaultConfiguration() {
        return Okio.source(getClass().getResourceAsStream( "/autocode-universe.json" ));
    }

    @Provides @Named ("tree")
    public Source defaultTree() {
        return Okio.source(getClass().getResourceAsStream( "/autocode-tree.json" ));
    }
}
