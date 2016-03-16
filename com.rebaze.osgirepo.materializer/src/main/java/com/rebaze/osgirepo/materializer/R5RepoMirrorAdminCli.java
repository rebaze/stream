package com.rebaze.osgirepo.materializer;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebaze.index.api.IndexAdmin;
import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.stream.api.StreamDefinitionDTO;
import com.rebaze.stream.api.StreamSourceResourcesDTO;

import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;
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
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (InputStream stream = uri.toURL().openStream()) {
				StreamDefinitionDTO def = gson.fromJson(
						new InputStreamReader(Okio.buffer(Okio.source(stream)).inputStream()),
						StreamDefinitionDTO.class);
				
				MirrorAdmin mirrorAdmin = new R5RepoMirrorAdmin(new File(dest), def, new R5RepoContentProvider());
				IndexAdmin indexer = new R5RepoIndexAdmin(new File(dest), def);

				System.out.println("Mirroring data: " + def.toString());
				List<StreamSourceResourcesDTO> mirrored = mirrorAdmin.mirror();
				
				System.out.println("Indexing local data..");
				indexer.index(mirrored);

				
			}

		}
	}
}
