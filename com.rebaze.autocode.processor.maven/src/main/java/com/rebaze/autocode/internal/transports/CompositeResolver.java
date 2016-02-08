package com.rebaze.autocode.internal.transports;

import java.util.Set;

import org.osgi.service.component.annotations.Component;

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
    private Set<ResourceResolver<GAV>> resolvers;

    @Override public Tree resolve( GAV query )
    {
        Tree result = null;
        // make sure to skip itself!
        for (ResourceResolver resolver : resolvers) {
            if (resolver == this) continue;

            try
            {
                result = resolver.resolve( query );
                if (result != null) {
                    break;
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
}
