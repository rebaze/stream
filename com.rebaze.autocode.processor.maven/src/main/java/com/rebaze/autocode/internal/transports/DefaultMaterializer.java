package com.rebaze.autocode.internal.transports;

import java.io.File;
import java.util.Set;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.trees.core.Tree;

/**
 *
 */
public class DefaultMaterializer implements ResourceMaterializer
{
    private final Set<ResourceTransporter> transporters;

    public DefaultMaterializer(Set<ResourceTransporter> transporters) {
        this.transporters = transporters;
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
        if (f == null) {
            throw new AutocodeException( "Request " + input + " not available." );
        }
        return f;
    }
}
