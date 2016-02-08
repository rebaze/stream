package com.rebaze.autocode.internal;

import com.rebaze.trees.core.TreeSession;
import com.rebaze.trees.core.util.DefaultTreeSessionFactory;


/**
 * Created by tonit on 29/10/15.
 */
public class DefaultModule 
{

    protected void configure()
    {
    	/**
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
        **/
    }

    // TODO: expose session via DS
    public TreeSession treeSession() {
        return new DefaultTreeSessionFactory().create();
    }

}
