package com.rebaze.autocode.config;

import java.io.IOException;
import java.io.InputStreamReader;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okio.Okio;
import okio.Source;

/**
 * Created by tonit on 27/10/15.
 */
@Component
public class JSonConfigBuilder implements WorkspaceConfiguration
{
    private final static Logger LOG = LoggerFactory.getLogger(JSonConfigBuilder.class);

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Configuration configuration;

    private final ArtifactLookupSites sites;

    private ResourceTreeConfiguration resourceTree;

    // TODO: field reference!
    JSonConfigBuilder( Source rawConfiguration, Source rawSites, Source rawTree) throws IOException
    {
        configuration = build( rawConfiguration, Configuration.class );
        sites = build( rawSites, ArtifactLookupSites.class );
        resourceTree = build(rawTree,ResourceTreeConfiguration.class);
        LOG.info("[WorkspaceConfiguration] Built config="+configuration+", sites="+sites+", tree="+resourceTree+"");
    }

    @Override
    public Configuration getConfiguration()
    {
        return configuration;
    }

    @Override
    public ArtifactLookupSites getSites()
    {
        return sites;
    }

    @Override
    public ResourceTreeConfiguration getResourceTreeConfiguration()
    {
        return resourceTree;
    }

    private <T> T build( Source raw, Class<T> clazz ) throws IOException
    {
        return gson.fromJson( new InputStreamReader( Okio.buffer(raw).inputStream() ), clazz );
    }

    @Override public String toString()
    {
        return "JSonConfigBuilder{" +
            "configuration=" + configuration +
            ", sites=" + sites +
            ", resourceTree=" + resourceTree +
            '}';
    }
}
