package com.rebaze.autocode.internal.transports;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.tree.api.Tree;

/**
 *
 */
@Component
public class DefaultMaterializer implements ResourceMaterializer
{
	
    private final static Logger LOG = LoggerFactory.getLogger( DefaultMaterializer.class );

	@Reference
    private List<ResourceTransporter> transporters;

    public DefaultMaterializer() {
    	transporters = new ArrayList<>();
    }

    @Override public File get( Tree input )
    {
        File f = null;
        for ( ResourceTransporter transporter : transporters )
        {
            f = transporter.transport( input );
            if ( f != null )
            {
                break;
            }
        }
      
        return f;
    }
    
    @Override public String toString()
    {
        return "DefaultMaterializer{" +
            "transporters=" + transporters.size() +
            '}';
    }
    
}
