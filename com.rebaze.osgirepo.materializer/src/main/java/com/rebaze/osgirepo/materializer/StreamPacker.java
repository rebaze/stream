package com.rebaze.osgirepo.materializer;

import static com.rebaze.tree.api.Selector.selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Reference;

import com.rebaze.distribution.Distribution;
import com.rebaze.distribution.DistributionBuilder;
import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.tree.api.Tag;
import com.rebaze.tree.api.Tree;
import com.rebaze.tree.api.TreeBuilder;
import com.rebaze.tree.api.TreeSession;

public class StreamPacker implements DistributionBuilder {
	
	@Reference
	private TreeSession treeSession;
	
	public StreamPacker() {
		
	}

	@Override
	public void pack(Distribution dist) {
		
	}

	@Override
	public void unpack(Distribution dist) {
		
	}
	
	public Tree createTree(String prefix, List<ResourceDTO> resources) {
		List<TreePath> virtual = new ArrayList<>(resources.size());
		for (ResourceDTO thing : resources) {
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
		private static final String RESOURCE = "RESOURCE";
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
							new Tree[0], Tag.tag(RESOURCE)));
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

	@Override
	public Tree createTree(List<ResourceDTO> resources) {
		return createTree(null, resources);
	}
	
	
}
