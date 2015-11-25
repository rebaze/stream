package com.rebaze.autocode.api.core;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import okio.Okio;
import okio.Source;

/**
 * Just binds external sources and possibly local dependency systems.
 */
public class TestConfigurationModule extends AbstractModule
{
    @Override protected void configure()
    {
        bind(Source.class).annotatedWith( Names.named("sites") ).toInstance( Okio.source( getClass().getResourceAsStream( "/autocode-locations.json" ) ) );
        bind(Source.class).annotatedWith( Names.named("universe") ).toInstance( Okio.source( getClass().getResourceAsStream( "/autocode-universe.json" ) ) );
        bind(Source.class).annotatedWith( Names.named("tree") ).toInstance( Okio.source( getClass().getResourceAsStream( "/autocode-tree.json" ) ) );
    }
}
