package com.rebaze.osgirepo.materializer;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Mirror Configuration")
public @interface MirrorConfig {
	String name();
	String definitionUri();
}
