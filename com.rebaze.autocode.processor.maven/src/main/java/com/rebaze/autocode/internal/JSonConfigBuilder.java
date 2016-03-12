package com.rebaze.autocode.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebaze.autocode.config.ArtifactLookupSites;
import com.rebaze.autocode.config.Configuration;
import com.rebaze.autocode.config.ResourceTreeConfiguration;
import com.rebaze.autocode.config.WorkspaceConfiguration;

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

    // TODO: Configuration types for: rawConfiguration, Source rawSites, Source rawTree
    
    public JSonConfigBuilder( ) throws IOException
    {
    	// getClass().getResourceAsStream( "/autocode-universe.json" )
    	File base = new File("/Users/tonit/devel/rebaze/autocode/com.rebaze.autocode.test/src/test/resources");
    	Source rawConfiguration = Okio.source( new FileInputStream(new File(base,"autocode-universe.json")) );
    	Source rawSites = Okio.source( new FileInputStream(new File(base,"autocode-locations.json" )));
    	Source rawTree = Okio.source( new FileInputStream(new File(base,"autocode-tree.json" )));

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
