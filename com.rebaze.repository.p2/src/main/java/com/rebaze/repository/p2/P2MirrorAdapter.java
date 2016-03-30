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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rebaze.mirror.api.MirrorAdmin;
import com.rebaze.mirror.api.ResourceDTO;
import com.rebaze.stream.api.StreamDefinitionDTO;
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

	public static final String NAME = "P2";

	private static final String INDEX_NAME_COMPRESSED = "artifacts.jar";
	private static final String INDEX_NAME_PRETTY = "artifacts.xml";

	private static final String TAG_REPOSITORY = "repository";
	private static final String TAG_MAPPINGS = "mappings";
	private static final String TAG_RULE = "rule";

	private static final String TAG_ARTIFACTS = "artifacts";
	private static final String TAG_ARTIFACT = "artifact";

	private static final String ATTR_TYPE = "type";

	private static final Logger LOG = LoggerFactory.getLogger(P2MirrorAdapter.class);

	public static final String TYPE = "org.eclipse.p2";

	private static final String ATTR_CLASSIFIER = "classifier";

	private static final String ATTR_ID = "id";

	private static final String ATTR_VERSION = "version";

	private static final String ATTR_FILTER = "filter";

	private static final String ATTR_OUTPUT = "output";

	@Reference
	private StreamDefinitionDTO definition;
	
	public P2MirrorAdapter() {
		// TODO Auto-generated constructor stub
	}
	
	public P2MirrorAdapter(StreamDefinitionDTO def) {
		this.definition = def;
	}

	@Override
	public List<ResourceDTO> fetchResources() {
		List<ResourceDTO> resources = new ArrayList<>();
		try {
			// Mirror must return a set of "mirrored" resources per StreamSource
			for (StreamSourceDTO src : definition.sources) {
				if (TYPE.equals(src.type) && src.active) {
					resources.addAll(fetchIndex(src));
				}

			}
		} catch (Exception e) {
			LOG.warn("Problem fetching resources.", e);
		}
		return resources;
	}

	private Collection<? extends ResourceDTO> fetchIndex(StreamSourceDTO src) throws Exception {
		List<ResourceDTO> ret = new ArrayList<>();
		String archive = src.url + "/" + INDEX_NAME_COMPRESSED;
		try (InputStream inp = extractSingle(archive, INDEX_NAME_PRETTY).inputStream()) {
			readP2artifactsXml(ret,src, inp);
		}
		return ret;
	}

	private static enum ParserState {
		beforeRoot, inRoot, inMappings, inArtifacts
	}

	public void readP2artifactsXml(List<ResourceDTO> result, StreamSourceDTO src, InputStream stream) throws IOException {
		XMLStreamReader reader = null;
		try {
			Map<String,String> typeToPath = new HashMap<>();
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();

			inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
			inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

			reader = inputFactory.createXMLStreamReader(stream);
			ParserState state = ParserState.beforeRoot;
			
			// Only valid within inArtifact:
			Properties properties = new Properties();
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
						}
						break;
					case inMappings:
						if (TAG_RULE.equals(localName)) {
							String filter = reader.getAttributeValue(null, ATTR_FILTER);
							String output = reader.getAttributeValue(null, ATTR_OUTPUT);
							typeToPath.put(filter, output);
						}
						break;
					}
					break;
				case END_ELEMENT:
					localName = reader.getLocalName();
					if (state == ParserState.inArtifacts && TAG_ARTIFACT.equals(localName)) {
						//state = ParserState.inArtifacts;
						// here we need to feed the result list:
						String path = resolvePath(src, typeToPath,classifier,id,version);
						result.add( resource( src, path, (String)properties.get("download.md5")));
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
	}

	private ResourceDTO resource( StreamSourceDTO src, String uri, String hash) {
		try {
			ResourceDTO res =  new ResourceDTO(src, new URI(uri), hash);
			//LOG.info("New Resource " + res);
			return res;
			
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String resolvePath(StreamSourceDTO src, Map<String, String> typeToPath, String classifier, String id, String version) {
		// test filters using osgi filter expr and math a path. Then resolve the path using inputs:
		String mask = "";
		for (String key : typeToPath.keySet()) {
			// use osgi filter resolver later:
			if (key.contains(classifier)) {
				mask = typeToPath.get(key);
			}
		}
		
		return resolveMask(src, mask,classifier,id,version);
	}

	private String resolveMask(StreamSourceDTO src, String mask, String classifier, String id, String version) {
		// just replace the variables in mask:
		// replace repourl too.
		mask = simpleReplace(mask,"repoUrl",src.url);
		mask = simpleReplace(mask,"id",id);
		mask = simpleReplace(mask,"version",version);
		mask = simpleReplace(mask,"classifier",classifier);
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