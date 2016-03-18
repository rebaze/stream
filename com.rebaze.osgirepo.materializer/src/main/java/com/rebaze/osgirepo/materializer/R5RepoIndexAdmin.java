package com.rebaze.osgirepo.materializer;

import static com.rebaze.tree.api.Selector.selector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.index.api.IndexAdmin;
import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamSourceDTO;
import com.rebaze.stream.api.StreamSourceResourcesDTO;
import com.rebaze.tree.api.Tag;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeBuilder;
import com.rebaze.tree.api.TreeSession;

import aQute.bnd.deployer.repository.api.IRepositoryContentProvider;
import aQute.bnd.deployer.repository.providers.R5RepoContentProvider;
import aQute.lib.collections.SortedList;
import okio.BufferedSource;
import okio.Okio;

public class R5RepoIndexAdmin implements IndexAdmin {
	private static final Logger LOG = LoggerFactory.getLogger(R5RepoIndexAdmin.class);

	private final R5RepoContentProvider r5provider = new R5RepoContentProvider();

	private TreeSession treeSession;

	private final File baseFolder;

	public R5RepoIndexAdmin(TreeSession session, File base) {
		this.baseFolder = base;
		this.treeSession = session;
	}

	@Override
	public List<URI> index(List<ResourceDTO> streamResources) {
		List<URI> indexes = new ArrayList<>();
		// indexes per origin repo:
		Map<StreamSourceDTO, List<URI>> map = mapByOrigin(streamResources);
		for (StreamSourceDTO origin : map.keySet()) {
			URI singleIndexFile = index(new File(baseFolder, origin.name + "/" + getFileName(origin.url)),
					map.get(origin));
			indexes.add(singleIndexFile);
		}

		return indexes;

		// then we use that index file to create a compound index:
		/**
		 * for (ContentAccessRepositoryIndexProcessor processor : processed) {
		 * for (LoadableArtifactDTO art : processor.getArtifacts()) { // we know
		 * its a local artifact resources.add(createLocalPath(processor, art));
		 * } }
		 * 
		 * // then create composite index: File f = new File(baseFolder,
		 * "index.xml"); System.out.println("Creating composite index " +
		 * f.getAbsolutePath() + " for " + resources.size() + " resources in " +
		 * definition.sources.length + " source streams.");
		 * 
		 * try (FileOutputStream fout = new FileOutputStream(f)) {
		 * r5provider.generateIndex(asLocal(resources), fout, definition.name,
		 * baseFolder.toURI(), true, null, null); }
		 **/
	}

	private Map<StreamSourceDTO, List<URI>> mapByOrigin(List<ResourceDTO> streamResources) {
		Map<StreamSourceDTO, List<URI>> map = new HashMap<>();
		for (ResourceDTO resource : streamResources) {
			List<URI> repoResource = map.get(resource.getOrigin());
			if (repoResource == null) {
				repoResource = new ArrayList<>();
				map.put(resource.getOrigin(), repoResource);
			}
			repoResource.add(resource.getUri());
		}
		return map;
	}

	@Override
	public URI index(StreamSourceResourcesDTO index) {
		return index(new File(baseFolder, index.name + "/" + getFileName(index.url)), index.resources);
	}

	private URI index(File indexFileName, List<URI> indexable) {
		indexFileName.getParentFile().mkdirs();
		try (OutputStream out = new FileOutputStream(indexFileName)) {
			String repoName = "Mirror " + baseFolder.getName();

			r5provider.generateIndex(asLocal(indexable), out, repoName, indexFileName.getParentFile().toURI(), true,
					null, null);
			System.out.println("Created index (" + indexFileName.getAbsolutePath() + ") for " + indexable.size()
					+ " files in repo: " + repoName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return indexFileName.toURI();
	}

	private Set<File> asLocal(Collection<URI> indexable) {
		Set<File> ret = new HashSet<>();
		for (URI uri : indexable) {
			ret.add(new File(uri));
		}
		return ret;
	}

	@Override
	public Tree createTree(String prefix, List<ResourceDTO> resources) {
		List<ResourceDTO> sorted = new SortedList<>(resources);
		List<TreePath> virtual = new ArrayList<>(sorted.size());
		for (ResourceDTO thing : sorted) {
			virtual.add(TreePath.build(prefix, thing));
		}
		return indexPaths(virtual).seal();
	}

	private TreeBuilder indexPaths(List<TreePath> virtual) {
		TreeBuilder root = treeSession.createTreeBuilder();
		new VirtualTree(treeSession, root, virtual);
		return root;
	}

	private static class TreePath {
		final private ResourceDTO resource;
		final private String[] parts;

		public static TreePath build(String prefix, ResourceDTO resource) {
			String base = resource.getUri().getPath();
			if (prefix != null) {
				base = base.substring(prefix.length() + 1);
			}
			return new TreePath(base.split("/"), resource);
		}

		public TreePath(String[] parts, ResourceDTO resource) {
			this.resource = resource;
			this.parts = parts;
		}

		public String getHead() {
			return (parts.length > 1) ? parts[0] : null;
		}

		public String[] getTail() {
			if (parts.length == 1) {
				return new String[] { parts[0] };
			}
			return Arrays.copyOfRange(parts, 1, parts.length);
		}

		public ResourceDTO getResource() {
			return resource;
		}
	}

	private static class VirtualTree {
		private List<TreePath> parts;
		private Collection<VirtualTree> children;
		private List<ResourceDTO> leafs;
		private TreeSession session;

		VirtualTree(TreeSession session, TreeBuilder treeBuilder, List<TreePath> path) {
			this.session = session;
			this.parts = path;
			children = buildChildren(treeBuilder);
			leafs = buildLeafs(treeBuilder);
		}

		private List<ResourceDTO> buildLeafs(TreeBuilder treeBuilder) {
			List<ResourceDTO> res = new ArrayList<>();
			for (TreePath tree : parts) {
				String head = tree.getHead();
				if (head == null) {
					treeBuilder.branch(session.createTree(selector(tree.getTail()[0]), tree.resource.getHash(),
							new Tree[0], Tag.tag("RESOURCE")));
					res.add(tree.getResource());
				}
			}
			return res;
		}

		private Collection<VirtualTree> buildChildren(TreeBuilder treeBuilder) {
			Map<String, List<TreePath>> map = new HashMap<>();
			for (TreePath tree : parts) {
				String head = tree.getHead();
				if (head != null) {
					List<TreePath> current = map.get(head);
					if (current == null) {
						current = new ArrayList<TreePath>();
						map.put(head, current);
					}
					current.add(new TreePath(tree.getTail(), tree.resource));
				}
			}
			List<VirtualTree> list = new ArrayList<>(map.size());
			for (String head : map.keySet()) {
				List<TreePath> paths = map.get(head);
				list.add(new VirtualTree(session, treeBuilder.branch(selector(head)), paths));
			}
			return list;
		}

		public Collection<VirtualTree> getChildren() {
			return children;
		}

		public List<ResourceDTO> getResources() {
			return leafs;
		}

	}

	private String getFileName(String path) {
		// strip the gz
		if (path.endsWith(".gz")) {
			path = path.substring(0, path.length() - 3);
		}
		int idx = path.lastIndexOf("/");
		if (idx >= 0) {
			return path.substring(idx + 1);
		} else {
			return path;
		}
	}

	@Override
	public Tree createTree(List<ResourceDTO> resources) {
		return createTree(null, resources);
	}

	@Override
	public URI compositeIndex(List<URI> indexes) throws Exception {
		List<URI> streamResources = new ArrayList<>();
		StreamSourceDTO origin = new StreamSourceDTO();
		origin.name = "composite";
		ContentAccessRepositoryIndexProcessor processor = new ContentAccessRepositoryIndexProcessor(origin);

		for (URI index : indexes) {
			URI base = new File(index).getParentFile().toURI();
			// read that index and get their resources.
			try (BufferedSource s = Okio.buffer(Okio.source(index.toURL().openStream()))) {
				r5provider.parseIndex(s.inputStream(), base , processor, null);
				for (ResourceDTO res : processor.getArtifacts()) {
					streamResources.add(res.getUri());
				}
			}
		}
		// then create a single composite
		URI composite = index(new File(baseFolder, "index.xml"), streamResources);
		System.out.println("Created compositeIndex (" + composite.getPath() + ") for " + streamResources.size() + " resources from " + indexes.size() + " indexes.");
		return composite;
	}
}
