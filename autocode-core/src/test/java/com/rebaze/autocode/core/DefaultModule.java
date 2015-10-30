package com.rebaze.autocode.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

import java.io.IOException;

/**
 * Created by tonit on 29/10/15.
 */
public class DefaultModule extends AbstractModule
{

    @Override protected void configure()
    {
        bind( WorkspaceConfiguration.class ).to( JSonConfigBuilder.class );
        bind( SubjectRegistry.class ).to( DefaultSubjectRegistry.class );

        Multibinder<AutocodeArtifactResolver> uriBinder = Multibinder.newSetBinder(binder(), AutocodeArtifactResolver.class);
        uriBinder.addBinding().to(LocalResolver.class);
        uriBinder.addBinding().to(HttpResolver.class);
    }

    @Provides
    public Autocode clearAutocode( AutocodeBuilder builder ) throws IOException
    {
        return builder.build();
    }
}
