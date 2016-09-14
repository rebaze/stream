package com.rebaze.connect.neo4j;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactoryProvider;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public class OSGiAwareSessionFactory implements SessionFactoryProvider {

	@Override
	public MetaData metaData() {
		return null;
	}

	@Override
	public Session openSession() {
		Driver driver = new BoltDriver(driverConfiguration);
		return new Neo4jSession(metaData(),driver);
	}

}
