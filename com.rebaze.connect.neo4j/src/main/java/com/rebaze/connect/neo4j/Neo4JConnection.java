package com.rebaze.connect.neo4j;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class Neo4JConnection {

	private final static Logger LOG = LoggerFactory.getLogger(Neo4JConnection.class);

	@Activate
	public void activate() {
		// connect and write something to neo..
		/**
		 * Driver driver = GraphDatabase.driver("bolt://localhost",
		 * AuthTokens.basic("neo4j", "admin")); try (Session session =
		 * driver.session()) { session.run("CREATE (a:Person {name:'OSGi',
		 * title:'King'})"); }
		 **/
		
		Session s = new 
		
		ClassLoader old = Thread.currentThread().getContextClassLoader();
		try {
			//Thread.currentThread().setContextClassLoader(null);

			Components.setDriver(new BoltDriver());

			Configuration configuration = new Configuration();
			
			configuration.driverConfiguration()

					.setDriverClassName("org.neo4j.ogm.drivers.bolt.driver.BoltDriver")
					.setURI("bolt://neo4j:admin@localhost").setEncryptionLevel("NONE");

			//Components.configure(configuration);
			SessionFactory sf = new SessionFactory("com.rebaze.connect.neo4j.model");
			
			Session session = sf.openSession();
			

			LOG.info("DONE.");
			
		} catch (Exception e) {
			LOG.error("Problem!",e);
		}finally {
			Thread.currentThread().setContextClassLoader(old);
		}

	}
}
