package com.rebaze.osgirepo.materializer;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebaze.osgirepo.api.StreamDefinitionDTO;

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
				System.out.println("Def: " + def.toString());
				R5RepoMirrorAdmin tool = new R5RepoMirrorAdmin(new File(dest), new R5RepoContentProvider());
				tool.mirror(def);
			}

		}
	}
}
