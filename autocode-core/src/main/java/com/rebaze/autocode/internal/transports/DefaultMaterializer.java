package com.rebaze.autocode.internal.transports;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.trees.core.Tree;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;

/**
 *
 */
public class DefaultMaterializer implements ResourceMaterializer
{
    private final Set<ResourceTransporter> transporters;

    @Inject
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
