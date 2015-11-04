package com.rebaze.autocode.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.rebaze.autocode.config.WorkspaceConfiguration;
import com.rebaze.autocode.core.AutocodeArtifactResolver;
import com.rebaze.autocode.config.JSonConfigBuilder;
import com.rebaze.autocode.core.SubjectHandlerFactory;
import com.rebaze.autocode.core.SubjectRegistry;
import com.rebaze.autocode.maven.MavenSubjectHandlerFactory;
import com.rebaze.autocode.registry.DefaultSubjectRegistry;
import com.rebaze.autocode.transports.HttpResolver;
import com.rebaze.autocode.transports.LocalResolver;
import com.rebaze.commons.tree.util.DefaultTreeSessionFactory;
import com.rebaze.commons.tree.util.TreeSession;

import javax.inject.Singleton;

/**
 * Created by tonit on 29/10/15.
 */
public class DefaultModule extends AbstractModule
{

    @Override protected void configure()
    {
        bind( WorkspaceConfiguration.class ).to( JSonConfigBuilder.class );
        bind( SubjectRegistry.class ).to( DefaultSubjectRegistry.class );

        Multibinder<AutocodeArtifactResolver> resolvers = Multibinder.newSetBinder(binder(), AutocodeArtifactResolver.class);
        resolvers.addBinding().to(LocalResolver.class);
        resolvers.addBinding().to(HttpResolver.class);

        Multibinder<SubjectHandlerFactory> handlers = Multibinder.newSetBinder(binder(), SubjectHandlerFactory.class);
        handlers.addBinding().to(MavenSubjectHandlerFactory.class);
    }

    @Provides @Singleton
    public TreeSession treeSession() {
        return new DefaultTreeSessionFactory().create();
    }

}
