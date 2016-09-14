package com.rebaze.stream.app;

import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.index.api.IndexAdmin;
import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.workspace.api.WorkspaceAdmin;

@Component (immediate=true)
public class ReadyStateService
{
    private static final Logger LOG = LoggerFactory.getLogger( ReadyStateService.class );

    @Reference (cardinality=ReferenceCardinality.AT_LEAST_ONE)
    private List<MirrorAdmin> childs; 
    
    @Reference (cardinality=ReferenceCardinality.AT_LEAST_ONE)
    private List<IndexAdmin> indexers; 
    
    @Reference (cardinality=ReferenceCardinality.AT_LEAST_ONE)
    private List<WorkspaceAdmin> ws; 
    
    @Activate
    public void activate()
    {
        report();        
    }

    private void report()
    {
        System.out.println( "------------------------------------------\\\n Stream is ready! \\\n------------------------------------------" );
        childs.forEach( System.out::println );
        indexers.forEach( System.out::println );
        ws.forEach( System.out::println );
    }
    
    //@Reference(target="(objectClass=*)", )
    private void addAny(List<Object> any) {
        report();
    }
}
