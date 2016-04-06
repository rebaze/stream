package com.rebaze.repository.cli;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebaze.distribution.Distribution;
import com.rebaze.distribution.DistributionBuilder;
import com.rebaze.index.api.IndexAdmin;
import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.osgirepo.materializer.MirrorConfig;
import com.rebaze.repository.osgi.R5RepoIndexAdmin;
import com.rebaze.repository.osgi.R5RepoMirrorAdmin;
import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.stream.api.StreamSourceResourcesDTO;
import com.rebaze.stream.app.StreamPacker;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeBuilder;
import com.rebaze.tree.api.TreeSession;
import com.rebaze.trees.core.internal.DefaultTreeSessionFactory;
import com.rebaze.trees.core.internal.TreeConsoleFormatter;

import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;
import aQute.lib.collections.SortedList;
import okio.Okio;

public class R5RepoMirrorAdminCli {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage     : R5RepoMirrorAdminCli <definition-url> <target_folder>");
			System.err.println("Example   : java -jar stream.r5mirror.cli.jar basic-stream.json target/repo");
			System.exit(-1);
		} else {
			String path = args[0];
			String dest = args[1];
			URI uri = null;
			if (path.contains("//:")) {
				uri = new URI(path);
			} else {
				uri = new File(path).toURI();

			}
			File baseFolder = new File(dest);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (InputStream stream = uri.toURL().openStream()) {
				StreamDefinitionDTO def = gson.fromJson(
						new InputStreamReader(Okio.buffer(Okio.source(stream)).inputStream()),
						StreamDefinitionDTO.class);
				def.localPath = baseFolder.getAbsolutePath();
				TreeSession session = new DefaultTreeSessionFactory().create(def.hashAlgorithm);	
				/**
				MirrorConfig config =  new MirrorConfig();
				
				R5RepoMirrorAdmin mirrorAdmin = new R5RepoMirrorAdmin(session,  new R5RepoContentProvider());
				IndexAdmin indexer = new R5RepoIndexAdmin(def);
				DistributionBuilder dist = new StreamPacker();

				System.out.println("Mirroring data: " + def.toString());
				List<ResourceDTO> remoteResources = mirrorAdmin.fetchResources();
				Tree tree = session.reduce(dist.createTree(remoteResources)); 	
				new TreeConsoleFormatter().prettyPrint(tree);
				
				// create packs:
				List<ResourceDTO> localResources = mirrorAdmin.download(remoteResources);
				
				System.out.println("Indexing local data.." + localResources.size());
				List<URI> indexes = indexer.index(localResources);

				indexer.compositeIndex(indexes);
				**/
				
				// Put that into a descriptor format, name it STREAM
				
				// 
				//mirrored.stream().map(x -> indexer.index(x));
				// then create the compound index:
				//mirrored.stream().map(s -> indexer.index(x)).collect();
				//indexer.index()
			}

		}
	}
	

}
