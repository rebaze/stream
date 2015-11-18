package com.rebaze.autocode.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.name.Named;
import okio.Okio;
import okio.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by tonit on 27/10/15.
 */
@Singleton
public class JSonConfigBuilder implements WorkspaceConfiguration
{
    private final static Logger LOG = LoggerFactory.getLogger(JSonConfigBuilder.class);

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Configuration configuration;

    private final ArtifactLookupSites sites;

    private ResourceTreeConfiguration resourceTree;


    @Inject
    JSonConfigBuilder( @Named( "universe" ) Source rawConfiguration, @Named( "sites" ) Source rawSites, @Named( "tree" ) Source rawTree) throws IOException
    {
        configuration = build( rawConfiguration, Configuration.class );
        sites = build( rawSites, ArtifactLookupSites.class );
        resourceTree = build(rawTree,ResourceTreeConfiguration.class);
        LOG.info("[WorkspaceConfiguration] Built config={}, sites={}, tree={}",configuration,sites,resourceTree);
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
