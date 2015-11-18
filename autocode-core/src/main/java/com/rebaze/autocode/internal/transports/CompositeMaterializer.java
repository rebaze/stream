package com.rebaze.autocode.internal.transports;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.trees.core.Tree;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.Set;

/**
 *
 */
@Singleton
@Named("composite")
public class CompositeMaterializer implements ResourceMaterializer
{
    @Inject
    private Set<ResourceMaterializer> materializers;

    @Override public File get( Tree input )
    {
        File result = null;
        // make sure to skip itself!
        for (ResourceMaterializer mat : materializers) {
            if (mat == this) continue;

            try
            {
                result = mat.get( input );
                if (result != null) {
                    break;
                }
            }catch (AutocodeException e) {
                // do nothing.
            }

        }
        if (result == null) {
            throw new AutocodeException( "CompositeMaterializer cannot load " + input + " using materializers: " + materializers.size() );
        }else
        {
            return result;
        }
    }
}
