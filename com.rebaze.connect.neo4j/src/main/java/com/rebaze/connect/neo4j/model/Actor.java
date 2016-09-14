package com.rebaze.connect.neo4j.model;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Actor is a person who acts in a project.
 * @author tonit
 *
 */
@NodeEntity
public class Actor {

    @GraphId
    private Long id;
    private String name;

    public Actor() {
    }

    public Actor(String name) {
        this.name = name;
    }


}