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
import com.rebaze.trees.core.TreeSession;
import com.rebaze.trees.core.util.DefaultTreeSessionFactory;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created by tonit on 29/10/15.
 */
public class DefaultModule extends AbstractModule
{

    @Override protected void configure()
    {
        bind( WorkspaceConfiguration.class ).to( JSonConfigBuilder.class );

        Multibinder<ResourceMaterializer> materializers = Multibinder.newSetBinder(binder(), ResourceMaterializer.class);
        materializers.addBinding().to(DefaultMaterializer.class);
        materializers.addBinding().to(WorkspaceResolver.class);
        bind( ResourceMaterializer.class).to( CompositeMaterializer.class);

        Multibinder<ResourceResolver<GAV>> resolvers = Multibinder.newSetBinder(binder(), new TypeLiteral<ResourceResolver<GAV>>(){});
        resolvers.addBinding().to(StaticGAVResolver.class);
        resolvers.addBinding().to(WorkspaceResolver.class);
        bind( new TypeLiteral<ResourceResolver<GAV>>(){}).to( CompositeResolver.class);

        Multibinder<ResourceTransporter> transporters = Multibinder.newSetBinder(binder(), ResourceTransporter.class);
        transporters.addBinding().to(LocalResourceTransporter.class);
        transporters.addBinding().to(HttpResourceTransporter.class);
        // TODO add composite for transporters as well.

        Multibinder<SubjectHandlerFactory> handlers = Multibinder.newSetBinder(binder(), SubjectHandlerFactory.class);
        handlers.addBinding().to(MavenSubjectHandlerFactory.class);

        bind( Workspace.class ).to( DefaultWorkspace.class );
    }

    @Provides @Singleton
    public TreeSession treeSession() {
        return new DefaultTreeSessionFactory().create();
    }

}
