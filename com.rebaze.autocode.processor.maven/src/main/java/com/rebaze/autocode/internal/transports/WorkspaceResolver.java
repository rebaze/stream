package com.rebaze.autocode.internal.transports;

import static com.rebaze.tree.api.Selector.selector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.internal.DefaultWorkspace;
import com.rebaze.autocode.internal.maven.GAV;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeSession;

/**
 * Can resolve things based on a given Treescape.
 */
@Component(name="workspace")
public class WorkspaceResolver implements ResourceResolver<GAV>, ResourceMaterializer
{
    private final static Logger LOG = LoggerFactory.getLogger( DefaultWorkspace.class );

	@Reference(bind="bindSession")
    private TreeSession treesession;

    private final Map<GAV, Tree> map = new HashMap<>();
    
    @Activate
    public void activate() {
    	LOG.info("installed WorkspaceResolver");
    }
    
    void bindSession(TreeSession session) {
    	// TODO: Do this.
        // that tree makes up everything this resolver knows about.
        // Index tree using GAV->Tree->File
        // for now we hard wire certain "known" artifacts:
        addToIndex( GAV.fromString( "com.rebaze.autocode:autocode-maven-extension" ), new File( "/Users/tonit/devel/rebaze/autocode/autocode-maven-extension/target/autocode-maven-extension-0.1.0-SNAPSHOT.jar" ) );
    }

    // TODO: This is a shortcut until we have implemented the surface tree.
    private void addToIndex( GAV gav, File file )
    {
        map.put( gav, treesession.createStreamTreeBuilder().selector( selector( file.getAbsolutePath() ) ).add( file ).seal() );
    }

    @Override public File get( Tree input )
    {
        if ( map.containsValue( input ) )
        {
            return new File( input.selector().name() );
        }
        else
        {
           // LOG.info( "Given tree is not from WorkspaceResolver: " + input + ". Make sure you derive it from WorkspaceResolver.resolve()." );
        	return null;
        }
    }

    @Override public Tree resolve( GAV query )
    {
    	Tree res = map.get( query );
    	//LOG.info("Resolving " + query + " from " + map.size() + " = " + res);;
        return res;
    }
    
    @Override public String toString()
    {
        return "WorkspaceResolver{" +
            "items=" + map.size() +
            '}';
    }
}
