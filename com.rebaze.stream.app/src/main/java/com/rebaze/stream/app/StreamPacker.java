package com.rebaze.stream.app;

import static com.rebaze.tree.api.Selector.selector;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.distribution.DistributionBuilder;
import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.tree.api.Tag;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeBuilder;
import com.rebaze.tree.api.TreeSession;
import com.rebaze.workspace.api.WorkspaceAdmin;

import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;
import osgi.enroute.scheduler.api.Scheduler;

@Designate( ocd = StreamPacker.Config.class, factory = true )
@Component( name = "rebaze.packer.provider", service= {DistributionBuilder.class}, configurationPolicy = ConfigurationPolicy.OPTIONAL, scope=ServiceScope.SINGLETON, immediate = true )
public class StreamPacker implements DistributionBuilder
{
    private final Logger LOG = LoggerFactory.getLogger( StreamPacker.class );

    private final R5RepoContentProvider r5provider = new R5RepoContentProvider();

    
    public enum DistributionFormat
    {
        OSGI_R5, ECLIPSE_P2, MAVEN_M2;
    }

    @ObjectClassDefinition( name = "Packer" )
    @interface Config
    {
        String destination() default "/tmp/foo";

        DistributionFormat format() default DistributionFormat.OSGI_R5;
        
        String tickRemoteSyncPattern() default "*/30 * * * * * *";

    }

    @Reference
    private TreeSession treeSession;
    
    @Reference
    private WorkspaceAdmin workspace;
    

    @Reference
    private Scheduler scheduler;

    private Closeable schedulerSession;
    
    private transient CompletableFuture<File> pipe;


    public StreamPacker()
    {
      
    }
    
    @Activate
    private void activate( StreamPacker.Config config, ComponentContext context )
    {
        LOG.info( "# Activating Stream: " + context.getProperties().get( "component.name" ) );
        try
        {
          //  this.schedulerSession = scheduler.schedule( () -> pack(), config.tickRemoteSyncPattern() );
        }
        catch ( Exception e )
        {
           throw new RuntimeException("Scheduler.");
        }

    }

    @Override
    public void pack( )
    {
        LOG.info( "PING PACKGER" );
        step1();
      /**
        synchronized (this)
        {
            if ( pipe == null || pipe.isDone() )
            {
                pipe = CompletableFuture.supplyAsync( () -> step1() );
            }
        }
        **/
    }

    private File step1()
    {
        Set<File> elements = workspace.list().stream().map( s -> new File(s.uri()) ).collect( Collectors.toSet() );
        File base = workspace.getLocation();
        File indexFileName = new File(base,"index.xml");
        try (FileOutputStream out = new FileOutputStream(indexFileName) ) {
            r5provider.generateIndex(elements, out, "Stream", indexFileName.getParentFile().toURI(), true, null, null);
            LOG.info("Created index (" + indexFileName.getAbsolutePath() + ") for " + elements.size());
        }
        catch ( Exception e )
        {
            LOG.error( "Problem creating index..",e);
        }
        return indexFileName;
    }
    
    @Deactivate
    private void deactivate( ComponentContext context ) throws IOException
    {
        LOG.info( "# Deactivating " + context.getProperties().get( "component.name" ) );
        if (schedulerSession != null) {
            schedulerSession.close();
        }
    }

    public Tree createTree( String prefix, List<ResourceDTO> resources )
    {
        List<TreePath> virtual = new ArrayList<>( resources.size() );
        for ( ResourceDTO thing : resources )
        {
            virtual.add( TreePath.build( prefix, thing ) );
        }
        return indexPaths( virtual ).seal();
    }

    private TreeBuilder indexPaths( List<TreePath> virtual )
    {
        TreeBuilder root = treeSession.createTreeBuilder();
        new VirtualTree( treeSession, root, virtual );
        return root;
    }

    private static class TreePath
    {
        final private ResourceDTO resource;
        final private String[] parts;

        public static TreePath build( String prefix, ResourceDTO resource )
        {
            String base = resource.getUri().getPath();
            if ( prefix != null )
            {
                base = base.substring( prefix.length() + 1 );
            }
            return new TreePath( base.split( "/" ), resource );
        }

        public TreePath( String[] parts, ResourceDTO resource )
        {
            this.resource = resource;
            this.parts = parts;
        }

        public String getHead()
        {
            return ( parts.length > 1 ) ? parts[0] : null;
        }

        public String[] getTail()
        {
            if ( parts.length == 1 )
            {
                return new String[] { parts[0] };
            }
            return Arrays.copyOfRange( parts, 1, parts.length );
        }

        public ResourceDTO getResource()
        {
            return resource;
        }
    }

    private static class VirtualTree
    {
        private static final String RESOURCE = "RESOURCE";
        private List<TreePath> parts;
        private Collection<VirtualTree> children;
        private List<ResourceDTO> leafs;
        private TreeSession session;

        VirtualTree( TreeSession session, TreeBuilder treeBuilder, List<TreePath> path )
        {
            this.session = session;
            this.parts = path;
            children = buildChildren( treeBuilder );
            leafs = buildLeafs( treeBuilder );
        }

        private List<ResourceDTO> buildLeafs( TreeBuilder treeBuilder )
        {
            List<ResourceDTO> res = new ArrayList<>();
            for ( TreePath tree : parts )
            {
                String head = tree.getHead();
                if ( head == null )
                {
                    treeBuilder.branch( session.createTree( selector( tree.getTail()[0] ), tree.resource.getHash(),
                            new Tree[0], Tag.tag( RESOURCE ) ) );
                    res.add( tree.getResource() );
                }
            }
            return res;
        }

        private Collection<VirtualTree> buildChildren( TreeBuilder treeBuilder )
        {
            Map<String, List<TreePath>> map = new HashMap<>();
            for ( TreePath tree : parts )
            {
                String head = tree.getHead();
                if ( head != null )
                {
                    List<TreePath> current = map.get( head );
                    if ( current == null )
                    {
                        current = new ArrayList<TreePath>();
                        map.put( head, current );
                    }
                    current.add( new TreePath( tree.getTail(), tree.resource ) );
                }
            }
            List<VirtualTree> list = new ArrayList<>( map.size() );
            for ( String head : map.keySet() )
            {
                List<TreePath> paths = map.get( head );
                list.add( new VirtualTree( session, treeBuilder.branch( selector( head ) ), paths ) );
            }
            return list;
        }

        public Collection<VirtualTree> getChildren()
        {
            return children;
        }

        public List<ResourceDTO> getResources()
        {
            return leafs;
        }

    }

}
