package com.rebaze.autocode.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.rebaze.autocode.internal.maven.GAV;
import com.rebaze.autocode.api.core.SubjectHandlerFactory;
import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.api.core.Workspace;
import com.rebaze.autocode.config.JSonConfigBuilder;
import com.rebaze.autocode.config.WorkspaceConfiguration;
import com.rebaze.autocode.internal.maven.MavenSubjectHandlerFactory;
import com.rebaze.autocode.internal.transports.*;
import com.rebaze.trees.core.util.DefaultTreeSessionFactory;
import com.rebaze.trees.core.util.TreeSession;

import javax.inject.Singleton;

/**
 * Created by tonit on 29/10/15.
 */
public class DefaultModule extends AbstractModule
{

    @Override protected void configure()
    {
        bind( WorkspaceConfiguration.class ).to( JSonConfigBuilder.class );
        bind( Workspace.class ).to( DefaultWorkspace.class );
        bind( ResourceMaterializer.class).to( DefaultMaterializer.class);
        bind( new TypeLiteral<ResourceResolver<GAV>>(){}).to( StaticGAVResolver.class);

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
