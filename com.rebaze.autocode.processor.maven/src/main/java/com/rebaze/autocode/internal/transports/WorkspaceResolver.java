package com.rebaze.autocode.internal.transports;

import static com.rebaze.trees.core.Selector.selector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.internal.maven.GAV;
import com.rebaze.trees.core.Tree;
import com.rebaze.trees.core.TreeSession;

/**
 * Can resolve things based on a given Treescape.
 */
@Component
public class WorkspaceResolver implements ResourceResolver<GAV>, ResourceMaterializer
{
    private TreeSession treesession;

    private final Map<GAV, Tree> map = new HashMap<>();

    public WorkspaceResolver( TreeSession session )
    {
        treesession = session;

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
            throw new AutocodeException( "Given tree is not from WorkspaceResolver: " + input + ". Make sure you derive it from WorkspaceResolver.resolve()." );
        }
    }

    @Override public Tree resolve( GAV query )
    {
        // Look up at the index.
        return map.get( query );
    }
}
