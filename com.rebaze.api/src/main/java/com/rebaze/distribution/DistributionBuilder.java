package com.rebaze.distribution;

import java.util.List;

import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.tree.api.Tree;

public interface DistributionBuilder {
	
	void pack(Distribution dist);
	
	void unpack(Distribution dist);

	Tree createTree(List<ResourceDTO> resources);
}
