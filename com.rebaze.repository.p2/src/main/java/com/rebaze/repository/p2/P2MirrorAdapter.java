package com.rebaze.repository.p2;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.mirror.api.ResourceDTO.HashType;
import com.rebaze.stream.api.StreamSourceDTO;

import okio.Buffer;
import okio.Okio;

/**
 * A really simplistic P2 repo reader.
 * Exposes contents as {@link ResourceDTO}s.
 * 
 * Which then can be processed further.
 * 
 * @author Toni Menzel <toni.menzel@rebaze.com>
 *
 */
@Component(property = "type=p2")
public class P2MirrorAdapter implements MirrorAdmin {

	private static final Logger LOG = LoggerFactory.getLogger(P2MirrorAdapter.class);

	public static final String NAME = "P2";

	private static final String INDEX_NAME_COMPRESSED = "artifacts.jar";
	private static final String INDEX_NAME_PRETTY = "artifacts.xml";

	private static final String TAG_REPOSITORY = "repository";
	private static final String TAG_MAPPINGS = "mappings";
	private static final String TAG_RULE = "rule";

	private static final String TAG_ARTIFACTS = "artifacts";
	private static final String TAG_ARTIFACT = "artifact";

	private static final String ATTR_TYPE = "type";
	
	public static final String TYPE = "org.eclipse.p2";

	private static final String ATTR_CLASSIFIER = "classifier";

	private static final String ATTR_ID = "id";

	private static final String ATTR_VERSION = "version";

	private static final String ATTR_FILTER = "filter";

	private static final String ATTR_OUTPUT = "output";

	private static final Object TAG_ARTIFACT_PROPERTY = "property";

	private static final String ATTR_NAME = "name";

	private static final String ATTR_VALUE = "value";

	//@Reference(target="(type="+TYPE+")")
	//private StreamDefinitionDTO definition;
		
	@Reference(target="(type="+TYPE+")")
	private List<StreamSourceDTO> source;
	
	public P2MirrorAdapter() {
	}
	
	public P2MirrorAdapter(StreamSourceDTO... src) {
		this.source = Arrays.asList(src);
	}

	@Override
	public List<ResourceDTO> fetchResources() {
		List<ResourceDTO> resources = new ArrayList<>();
		for (StreamSourceDTO src : source) {
			resources.addAll(fetchIndex(src));
		}
		//return (source.stream().map(s -> fetchIndex(s)).collect(Collectors.toList());;
		
		return resources;
	}

	private List<ResourceDTO> fetchIndex(StreamSourceDTO src) {
		List<ResourceDTO> ret = new ArrayList<>();
		try {
			String archive = src.url + "/" + INDEX_NAME_COMPRESSED;
			try (InputStream inp = extractSingle(archive, INDEX_NAME_PRETTY).inputStream()) {
				readP2artifactsXml(ret,src, inp);
			}
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		return ret;
	}
	

	private static enum ParserState {
		beforeRoot, inRoot, inMappings, inArtifacts
	}

	public void readP2artifactsXml(List<ResourceDTO> result, StreamSourceDTO src, InputStream stream) throws IOException {
		XMLStreamReader reader = null;
		try {
			Map<Filter,String> typeToPath = new HashMap<>();
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();

			inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
			inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

			reader = inputFactory.createXMLStreamReader(stream);
			ParserState state = ParserState.beforeRoot;
			
			// Only valid within inArtifact:
			Map<String,String> properties = new HashMap<>();

			String classifier = null;
			String id = null;
			String version = null;
			
			while (reader.hasNext()) {
				int type = reader.next();
				String localName;

				switch (type) {
				case START_ELEMENT:
					localName = reader.getLocalName();
					switch (state) {
					case beforeRoot:
						state = ParserState.inRoot;
						break;
					case inRoot:
						if (TAG_ARTIFACTS.equals(localName)) {
							state = ParserState.inArtifacts;
						}else if (TAG_MAPPINGS.equals(localName)) {
							state = ParserState.inMappings;
						}
						break;
					case inArtifacts:
						if (TAG_ARTIFACT.equals(localName)) {
							classifier = reader.getAttributeValue(null, ATTR_CLASSIFIER);
							id = reader.getAttributeValue(null, ATTR_ID);
							version = reader.getAttributeValue(null, ATTR_VERSION);
							//LOG.info("ID: " +  id);
						}else if (TAG_ARTIFACT_PROPERTY.equals(localName)) {
							properties.put(reader.getAttributeValue(null, ATTR_NAME), reader.getAttributeValue(null, ATTR_VALUE));
						}
						break;
					case inMappings:
						if (TAG_RULE.equals(localName)) {
							String filter = reader.getAttributeValue(null, ATTR_FILTER);
							String output = reader.getAttributeValue(null, ATTR_OUTPUT);
							typeToPath.put(new Filter(fixupString(filter)), output);
						}
						break;
					}
					break;
				case END_ELEMENT:
					localName = reader.getLocalName();
					if (state == ParserState.inArtifacts && TAG_ARTIFACT.equals(localName)) {
						//state = ParserState.inArtifacts;
						// here we need to feed the result list:
						properties.put("id",id);
						properties.put("classifier",classifier);
						properties.put("version",version);
						properties.put("repoUrl",src.url); 
						String path = resolvePath(src, typeToPath,properties);
						
						ResourceDTO resource = resource( src, path, (String)properties.get("download.md5"), ResourceDTO.HashType.MD5);
						if (filter(resource,properties)) {
							result.add( resource);
							
						}
						properties.clear();
					}
					if (state == ParserState.inMappings && TAG_MAPPINGS.equals(localName))
						state = ParserState.inRoot;
					break;
				default:
					break;
				}

			}
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (XMLStreamException e) {
				}
		}
		// at the end: block!
		//LOG.info("RESULT: " + result.size());
	}

	// kill whitespace..
	private String fixupString(String s) {
		StringBuilder sb = new StringBuilder();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (!Character.isWhitespace(c)) {
				if (Character.isUpperCase(c))
					c = Character.toLowerCase(c);
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private boolean filter(ResourceDTO resource, Map<?,?> props) {
		String given = resource.getOrigin().filter;
		if (given != null) {
			Filter filter = new Filter(given);
			return filter.matchMap(props);
		}else {
			return true;
		}
	}

	private ResourceDTO resource( StreamSourceDTO src, String uri, String hash,HashType hashType) {
		try {
			ResourceDTO res =  new ResourceDTO(src, new URI(uri), hash,hashType);
			return res;
			
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String resolvePath(StreamSourceDTO src, Map<Filter, String> typeToPath, Map<String,String> properties) {
		// test filters using osgi filter expr and math a path. Then resolve the path using inputs:
		String mask = null;
		for (Filter key : typeToPath.keySet()) {
			// use osgi filter resolver later:
			if (key.matchMap(properties)) {
				mask = typeToPath.get(key);
				break;
			}
		}
		return resolveMask(src, mask,properties);
	}

	private String resolveMask(StreamSourceDTO src, String mask, Map<String,String> properties) {
		for (String key : properties.keySet()) {
			mask = simpleReplace(mask,key,properties.get(key));
		}
		return mask;
	}
	
	 String simpleReplace(String pattern, String var, String value) {
		String res =  pattern.replaceAll("\\$\\{"+var+"\\}", value);			
		return res;
	}
	
	private Buffer extractSingle(String url, String name) throws URISyntaxException {
		OutputStream out = new ByteArrayOutputStream(1024);
		Buffer content = Okio.buffer(Okio.sink(out)).buffer();
		try (ArchiveInputStream input = new ArchiveStreamFactory()
				.createArchiveInputStream(new BufferedInputStream(new URL(url).openStream()))) {
			ArchiveEntry entry;
			while ((entry = input.getNextEntry()) != null) {
				if (entry.getName().equals(name)) {
					content.readFrom(input);
				}

			}
		} catch (IOException | ArchiveException e) {
			LOG.error("Problem unpacking archive " + url + ".", e);
		}
		return content;
	}
}
