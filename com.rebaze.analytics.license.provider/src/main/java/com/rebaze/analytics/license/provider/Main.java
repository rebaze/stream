package com.rebaze.analytics.license.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.osgi.dto.DTO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rebaze.analytics.license.Asf2License;
import com.rebaze.analytics.license.BSD2License;
import com.rebaze.analytics.license.BSD3License;
import com.rebaze.analytics.license.UnknownLicense;
import com.rebaze.analytics.license.provider.Main.LicensedItem;

public class Main {


	private Map<String, LicensedItem> map = new HashMap<>();

	class LicensedItem extends DTO {
		public String name;
		public String bsn;
		public String osgiVersion;
		public String checksum;
		public License license;
		public String groupId;
		public String artifactId;
		public String version;
		public String classifier;
	}

	public static void main(String... args) throws Exception {
		Properties p = new Properties();
		Arrays.asList(args).stream().filter(s -> s.startsWith("-")).findAny().ifPresent(s -> {
			try {
				p.load(new FileInputStream(new File(s)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		String cmd = Arrays.asList(args).stream().filter(s -> s.startsWith("+")).findAny().orElse(null);

		
		Main main = new Main();

		for (String param : args) {
			if (!param.startsWith("+") && !param.startsWith("-")) {
				walk(p, main, new File(param));
			}
		}
		
		// command based:
		if ("+bom".equals(cmd)) {
			main.getItems().forEach(item -> { System.out.println(item.groupId + ":" + item.artifactId + ":" + item.version); });
		} else {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			System.out.println(gson.toJson(main.getItems()));	
		}
	}

	private Collection<LicensedItem> getItems() {
		return map.values();
	}

	private static void walk(Properties p, Main main, File f) {
		if (f.exists() && f.isFile() && f.getName().endsWith(".jar")) {
			try {
				main.analyze(f.toURI(), p);
			} catch (Exception e) {

			}
		} else {
			if (f.exists() && f.isDirectory()) {
				Arrays.asList(f.listFiles()).forEach(sub -> walk(p, main, sub));
			}
		}
	}

	private void analyze(URI input, Properties p) throws Exception {
		addToIndex(checksum(input.toURL().openStream()),input.getPath());
		try (JarInputStream fis = new JarInputStream(input.toURL().openStream())) {
			// embedded
			//buildIndex(input.toASCIIString(), fis, p);
		}
		LicensedItem item = map.get(input.getPath());
		analyzeLicenses(item, new JarInputStream(input.toURL().openStream()), p);
		try (JarInputStream fis = new JarInputStream(input.toURL().openStream())) {
			//analyzeLicenses(input.toASCIIString(), fis, p);
		}
	}

	private void buildIndex(String name, JarInputStream fis, Properties p) throws Exception {
		JarEntry entry = null;

		while ((entry = fis.getNextJarEntry()) != null) {
			if (entry.getName().endsWith(".jar")) {
				String checksum = checksum( fis);
				addToIndex(checksum, entry.getName());
			}
		}

	}

	private void addToIndex(String checksum, String name) throws Exception {
		LicensedItem item = new LicensedItem();
		item.checksum = checksum;
		item.name =name;
		 map.put(name, item);
	}

	public String checksum( final InputStream is) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-1");

		byte[] bytes = new byte[1024];
		int numRead = 0;
		long total = 0L;
		while ((numRead = is.read(bytes)) >= 0) {
			total += numRead;
			digest.update(Arrays.copyOf(bytes, numRead));
		}
		// System.out.println("Read: " + total + " entry: " + entry.getSize() +
		// " (name: "+entry.getName()+")");
		return convertToHex(digest.digest());
	}

	private void analyzeLicenses(LicensedItem item, JarInputStream fis, Properties p) throws IOException, URISyntaxException {

		JarEntry entry = null;

		while ((entry = fis.getNextJarEntry()) != null) {
			// System.out.println(name + ":" + entry.getName());
			if (entry.getName().endsWith(".jar")) {
				JarInputStream sub = new JarInputStream(fis);

				analyzeLicenses(map.get(entry.getName()), sub, p);

			} else if (entry.getName().equals("META-INF/MANIFEST.MF")) {
				// JarInputStream sub = new JarInputStream(fis);
				// analyze(name + "/" + entry.getName(),sub,p);

			} else {
				if (entry.getName().endsWith("/pom.xml")) {
			
					// read gav from pom:
					
					MavenXpp3Reader reader = new MavenXpp3Reader();
					try {
						Model pom = reader.read(fis);
						item.groupId = pom.getGroupId();
						item.artifactId = pom.getArtifactId();
						item.version = pom.getVersion();
						fixGroupId(item,entry.getName());

						
					} catch (XmlPullParserException e) {
						//e.printStackTrace();
					}
				}
				// JarInputStream sub = new JarInputStream(fis);
				// analyze(name + "/" + entry.getName(),sub,p);

			}
		}

		Manifest mf = fis.getManifest();

		if (mf != null) {
			String bsn = mf.getMainAttributes().getValue("Bundle-SymbolicName");
			String license = mf.getMainAttributes().getValue("Bundle-License");
			// System.out.println("+ " + name + " --> " + bsn + " --> " +
			// license);
			item.bsn = bsn;
			item.osgiVersion = mf.getMainAttributes().getValue("Bundle-Version");
			// fix version with osgi version:
			item.version = fixVersion(item.version,item.osgiVersion);

			if (license != null && item != null) {
				if (license.contains("http://www.apache.org/licenses/LICENSE-2.0") || license.contains("pache2")
						|| license.contains("pache-2")) {
					item.license = new Asf2License(license);
				} else if (license.contains("BSD-3")) {
					item.license = new BSD3License(license);
				} else if (license.contains("BSD-2")) {
					item.license = new BSD2License(license);
				} else {
					item.license = new UnknownLicense(license);
				}
			}

			if (item != null) {
				// find from props:
				String custom = p.getProperty(item.checksum);
				if (custom != null) {
					item.license = mapLicense(item, custom);
				}
			}
			// System.out.println("- " + name);
		}
	}

	private void fixGroupId(LicensedItem item, String name) {
		// parse parts from name:
		String[] split = name.split("/");
		if (item.groupId == null) {
			item.groupId = split[2];			
		}
		if (item.artifactId == null) {
			item.artifactId = split[3];			
		}
	}

	private String fixVersion(String version, String osgiVersion) {
		if (version == null) {
			if (osgiVersion != null) {
				if (osgiVersion.endsWith(".SNAPSHOT")) {
					return osgiVersion.replaceAll("\\.SNAPSHOT", "-SNAPSHOT");
				}else {
					return osgiVersion;
				}
			}
		}
		return version;
	}

	private License mapLicense(LicensedItem item, String custom) {
		if ("Apache2".equals(custom))
			return new Asf2License("Custom License Mapping");
		if ("BSD2".equals(custom))
			return new BSD2License("Custom License Mapping");
		if ("BSD3".equals(custom))
			return new BSD3License("Custom License Mapping");

		UnknownLicense unknown = new UnknownLicense("Custom License");
		unknown.name = custom;
		return unknown;
	}

	public static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}
}
