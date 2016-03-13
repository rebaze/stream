package com.rebaze.autocode.internal.transports;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.transport.ResourceMaterializer;
import com.rebaze.tree.api.Tree;

/**
 *
 */
@Component(name="compositeMaterializer")
public class CompositeMaterializer implements ResourceMaterializer
{
	
    private final static Logger LOG = LoggerFactory.getLogger( CompositeMaterializer.class );

	@Reference(bind="bind",unbind="unbind",target="(!(component.name=compositeMaterializer))")
    private List<ResourceMaterializer> materializers;

	public CompositeMaterializer() {
		materializers = new ArrayList<>();
	}
	
	@Activate
    public void activate() {
    	LOG.info("installed CompositeMaterializer: " + materializers.size());
    }
	
	@Deactivate
    public void deactivate() {
    	LOG.info("uninstalled CompositeMaterializer: " + materializers.size());
    }
	
	public void bind(ResourceMaterializer mat) {
		LOG.info("binding materializer: " + mat);
	}
	
	public void unbind(ResourceMaterializer mat) {
		//materializers.remove(mat);
	}
	
    @Override public File get( Tree input )
    {
        File result = null;
        // make sure to skip itself!
        for (ResourceMaterializer mat : materializers) {
            if (mat == this) {
            	LOG.warn("Dude, composites should not end up here!");
            	continue;
            }

            try
            {
                result = mat.get( input );
                if (result != null) {
                    LOG.info( "+ Tree " + input + " materialized by " + mat.toString() );
                    break;
                }else {
                    LOG.info( "- Tree " + input + " not materialized by " + mat.toString() );
                }
            }catch (AutocodeException e) {
                LOG.error("Error while trying to materialize " + input + " with " + mat,e);
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
