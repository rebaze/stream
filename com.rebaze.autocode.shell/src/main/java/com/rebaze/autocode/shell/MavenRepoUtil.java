package com.rebaze.autocode.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.google.common.io.Files;

/**
 * Ability: Collects and resolves dependencies. Uploads bills to nexus.
 */
@Component(immediate = true, service=MavenRepoUtil.class)
public class MavenRepoUtil {

	private RepositorySystem system;

	public MavenRepoUtil() {

	}

	@Activate
	public void activate() throws Exception {
		// RepositoryAccess access = new Booter.RepositoryAccess(args[0],
		// args[1], args[2], args[3]);

		// Setup
		system = Booter.newRepositorySystem();

	}

	private static boolean forceRedeploy(Artifact artifact) {
		return false;
	}

	private static DeployRequest prepareDeployRequest(RemoteRepository targetRepo, Artifact resolvedArtifact) {
		DeployRequest deployRequest = new DeployRequest();
		deployRequest.addArtifact(resolvedArtifact);
		deployRequest.setRepository(targetRepo);
		return deployRequest;
	}

	private static File clear(String s) {
		File f = Files.createTempDir();
		System.out.println("Temporary local cache: " + f.getAbsolutePath());
		return f;
	}

	private Artifact resolveOnTarget(RepositorySystemSession session, RemoteRepository dzRepository, Artifact artifact) {
		ArtifactRequest request = new ArtifactRequest();
		request.addRepository(dzRepository);
		request.setArtifact(artifact);
		try {
			ArtifactResult result = system.resolveArtifact(session, request);
			artifact = result.getArtifact();
			return artifact;
		} catch (Exception e) {
			return null;
		}
	}

	private Set<Artifact> readBill(File bill) throws IOException {
		Set<Artifact> set = new HashSet<Artifact>();
		BufferedReader reader = new BufferedReader(new FileReader(bill));
		String line = null;
		while ((line = (reader.readLine())) != null) {
			if (!line.startsWith("#")) {
				Artifact a = new DefaultArtifact(line);
				if (a != null) {
					set.add(a);
				}
			}
		}

		reader.close();
		return set;
	}

	public void resolve(File bill) throws IOException, DeploymentException {
		RepositorySystemSession session = Booter.newRepositorySystemSession(system,"localrepo");

		File folder = clear(".localRepo2");

		RemoteRepository unsafe = new RemoteRepository.Builder("local", "default",
				new File("/tmp/unsafe").toURI().toString()).build(); //

		File deltaFolder = new File(bill.getParentFile(), "deltaRepository");
		RemoteRepository localRepo = new RemoteRepository.Builder("local", "default", deltaFolder.toURI().toString())
				.build();
		Authentication auth = new AuthenticationBuilder().addUsername("admin").addPassword("admin123").build();
		RemoteRepository dzRepository = new RemoteRepository.Builder("nexus", "default",
				"http://localhost:8081/nexus/content/repositories/thirdparty/").setAuthentication(auth).build(); //

		// parse bill

		Set<Artifact> artifacts = readBill(bill);

		Set<Artifact> freshArtifacts = new HashSet<Artifact>();
		Set<Artifact> unresolvedArtifacts = new HashSet<Artifact>();

		for (Artifact artifact : artifacts) {
			if (forceRedeploy(artifact) || resolveOnTarget(session, dzRepository, artifact) == null) {
				Artifact resolvedArtifact = resolveOnTarget(session, Booter.newCentralRepository(), artifact);
				// resolve on local unsafe:
				// Artifact resolvedArtifact = resolveOnTarget(unsafe,
				// artifact);

				if (resolvedArtifact != null) {
					system.deploy(session, prepareDeployRequest(dzRepository, resolvedArtifact));

					system.deploy(session, prepareDeployRequest(localRepo, resolvedArtifact));
					freshArtifacts.add(artifact);
				} else {
					unresolvedArtifacts.add(artifact);
				}
			}
		}

		for (Artifact a : freshArtifacts) {
			System.out.println("Uploaded: " + a);
		}
		for (Artifact a : unresolvedArtifacts) {
			System.out.println("Still missing: " + a);
		}
		System.out.println("Summary: repo=" + folder.getAbsolutePath() + " deltaRepo=" + deltaFolder.getAbsolutePath()
				+ " bill=" + artifacts.size() + " new=" + freshArtifacts.size() + " unresolved="
				+ unresolvedArtifacts.size());

	}

}
