package com.rebaze.osgirepo.materializer;

import java.io.File;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebaze.osgirepo.api.StreamDefinitionDTO;

import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;
import okio.Okio;

public class R5RepoMirrorAdminCli {
	public static void main(String[] args) throws Exception {
		File f = new File("basic-stream.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		StreamDefinitionDTO def = gson.fromJson(new InputStreamReader(Okio.buffer(Okio.source(f)).inputStream()),
				StreamDefinitionDTO.class);
		System.out.println("Def: " + def.toString());
		R5RepoMirrorAdmin tool = new R5RepoMirrorAdmin(new File("target/repo"), new R5RepoContentProvider());
		tool.mirror(def);
	}
}
