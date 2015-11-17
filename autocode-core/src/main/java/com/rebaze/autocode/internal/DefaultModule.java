package com.rebaze.autocode.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.rebaze.autocode.api.core.SubjectHandlerFactory;
import com.rebaze.autocode.api.core.SubjectRegistry;
import com.rebaze.autocode.config.JSonConfigBuilder;
import com.rebaze.autocode.config.WorkspaceConfiguration;
import com.rebaze.autocode.internal.maven.MavenSubjectHandlerFactory;
import com.rebaze.autocode.internal.transports.HttpResourceTransporter;
import com.rebaze.autocode.internal.transports.LocalResourceTransporter;
import com.rebaze.autocode.internal.transports.ResourceTransporter;
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

        Multibinder<ResourceTransporter> resolvers = Multibinder.newSetBinder(binder(), ResourceTransporter.class);
        resolvers.addBinding().to(LocalResourceTransporter.class);
        resolvers.addBinding().to(HttpResourceTransporter.class);

        Multibinder<SubjectHandlerFactory> handlers = Multibinder.newSetBinder(binder(), SubjectHandlerFactory.class);
        handlers.addBinding().to(MavenSubjectHandlerFactory.class);
    }

    @Provides @Singleton
    public TreeSession treeSession() {
        return new DefaultTreeSessionFactory().create();
    }

}
