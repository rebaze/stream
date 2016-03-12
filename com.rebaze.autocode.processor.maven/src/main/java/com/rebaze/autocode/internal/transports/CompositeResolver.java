package com.rebaze.autocode.internal.transports;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.autocode.api.core.AutocodeException;
import com.rebaze.autocode.api.transport.ResourceResolver;
import com.rebaze.autocode.internal.maven.GAV;
import com.rebaze.trees.core.Tree;

/**
 *
 */
@Component(name="compositeResolver")
public class CompositeResolver implements ResourceResolver<GAV>
{
    private final static Logger LOG = LoggerFactory.getLogger( CompositeResolver.class );

	@Reference(bind="bind",unbind="unbind",target="(!(component.name=compositeResolver))")
    private List<ResourceResolver<GAV>> resolvers;

	public CompositeResolver() {
		resolvers = new ArrayList<>();
	}
	
	@Activate
    public void activate() {
    	LOG.info("installed CompositeResolver: " + resolvers.size());
    }
	
	@Deactivate
    public void deactivate() {
    	LOG.info("uninstalled CompositeResolver: " + resolvers.size());
    }
	
	public void bind(ResourceResolver<GAV> resolver) {
		LOG.info("binding resolver: " + resolver);
		//resolvers.add(resolver);
	}
	
	public void unbind(ResourceResolver<GAV> resolver) {
		resolvers.remove(resolver);
	}
	
    @Override public Tree resolve( GAV query )
    {
        Tree result = null;
        // make sure to skip itself!
        for (ResourceResolver<GAV> resolver : resolvers) {
            if (resolver == this) {
            	LOG.warn("Dude, composite should not end up here!");
            	continue;
            }

            try
            {
                result = resolver.resolve( query );
                if (result != null) {
                    LOG.info( "+ Query " + query + " resolved by " + resolver.toString() );
                    break;
                }else {
                    LOG.info( "- Query " + query + " cannot be resolved by " + resolver.toString() );
                }
            }catch (AutocodeException e) {
                // do nothing.
            }

        }
        if (result == null) {
            throw new AutocodeException( "CompositeResolver cannot resolve " + query + " using resolvers: " + resolvers.size() );
        }else
        {
            return result;
        }
    }
    
    @Override public String toString()
    {
        return "CompositeResolver{" +
            "size=" + resolvers.size() +
            '}';
    }
}
