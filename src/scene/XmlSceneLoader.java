package scene;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Concrete implementation of {@link SceneLoader} for XML source files.
 * <p>
 * This class uses the DOM (Document Object Model) parser to navigate XML structures.
 * It extracts scene attributes and geometric data by mapping XML elements and
 * attributes into the generic format required by the base loader.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class XmlSceneLoader extends SceneLoader {

    /**
     * The parsed XML document tree
     */
    private final Document doc;

    /**
     * Constructs an XML scene loader and parses the file into a DOM Document.
     * * @param sceneName the name of the scene to be created
     *
     * @param xmlPath the path to the XML source file
     * @throws RuntimeException if the XML file cannot be read or parsed
     */
    public XmlSceneLoader(String sceneName, String xmlPath) {
        super(sceneName);
        try {
            File xmlFile = new File(xmlPath);
            this.doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(xmlFile);

            // Eliminates empty text nodes and combines split text nodes for cleaner traversal
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            throw new RuntimeException("Error loading XML from: " + xmlPath, e);
        }
    }

    @Override
    protected String getBackgroundColor() {
        // Retrieves the background-color attribute from the root element
        String color = doc.getDocumentElement().getAttribute("background-color");
        return color.isEmpty() ? null : color;
    }

    @Override
    protected String getAmbientLight() {
        // Look for the <ambient-light> tag and extract its color attribute
        NodeList ambientList = doc.getElementsByTagName("ambient-light");
        if (ambientList.getLength() > 0) {
            Element ambient = (Element) ambientList.item(0);
            return ambient.getAttribute("color");
        }
        return null;
    }

    @Override
    protected List<Map<String, String>> getGeometries() {
        List<Map<String, String>> list = new ArrayList<>();
        Node geometriesNode = doc.getElementsByTagName("geometries").item(0);

        if (geometriesNode != null) {
            NodeList children = geometriesNode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);

                // Process only actual XML elements (tags), ignoring comments or whitespace
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) node;
                    Map<String, String> map = new HashMap<>();

                    // 1. Map the tag name (e.g., "sphere") to the "type" key
                    map.put("type", el.getTagName());

                    // 2. Extract all attributes (radius, center, p0, etc.) into the map
                    var attributes = el.getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node attr = attributes.item(j);
                        map.put(attr.getNodeName(), attr.getNodeValue());
                    }

                    list.add(map);
                }
            }
        }
        return list;
    }
}