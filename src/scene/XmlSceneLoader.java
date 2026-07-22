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
     *
     * @param sceneName the name of the scene to be created
     * @param xmlPath   the path to the XML source file
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
        // Prefer ambient light nested under <lights>, then fall back to a top-level tag
        Node lightsNode = doc.getElementsByTagName("lights").item(0);
        if (lightsNode != null) {
            NodeList children = lightsNode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE
                        && "ambient-light".equals(node.getNodeName())) {
                    String color = ((Element) node).getAttribute("color");
                    return color.isEmpty() ? null : color;
                }
            }
        }

        NodeList ambientList = doc.getElementsByTagName("ambient-light");
        if (ambientList.getLength() > 0) {
            String color = ((Element) ambientList.item(0)).getAttribute("color");
            return color.isEmpty() ? null : color;
        }

        return null;
    }

    @Override
    protected List<Map<String, String>> getLights() {
        List<Map<String, String>> list = new ArrayList<>();
        Node lightsNode = doc.getElementsByTagName("lights").item(0);

        if (lightsNode == null) {
            return list;
        }

        NodeList children = lightsNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element el = (Element) node;
            String type = el.getTagName();
            if ("ambient-light".equals(type)) {
                continue;
            }

            Map<String, String> map = new HashMap<>();
            map.put("type", type);

            var attributes = el.getAttributes();
            for (int j = 0; j < attributes.getLength(); j++) {
                Node attr = attributes.item(j);
                map.put(attr.getNodeName(), attr.getNodeValue());
            }

            list.add(map);
        }

        return list;
    }

    @Override
    protected Map<String, String> getCamera() {
        Node cameraNode = doc.getElementsByTagName("camera").item(0);
        if (cameraNode == null) return null;

        Map<String, String> map = new HashMap<>();
        var attributes = cameraNode.getAttributes();
        for (int j = 0; j < attributes.getLength(); j++) {
            Node attr = attributes.item(j);
            map.put(attr.getNodeName(), attr.getNodeValue());
        }
        return map;
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

                    // Map the tag name (e.g., "sphere") to the "type" key
                    map.put("type", el.getTagName());

                    // Extract all attributes (radius, center, p0, etc.) into the map
                    var attributes = el.getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node attr = attributes.item(j);
                        map.put(attr.getNodeName(), attr.getNodeValue());
                    }

                    // Extract nested <material .../> into namespaced keys (e.g., material.kA)
                    NodeList materialNodes = el.getElementsByTagName("material");
                    if (materialNodes.getLength() > 0) {
                        Element materialEl = (Element) materialNodes.item(0);
                        var materialAttributes = materialEl.getAttributes();
                        for (int j = 0; j < materialAttributes.getLength(); j++) {
                            Node attr = materialAttributes.item(j);
                            map.put("material." + attr.getNodeName(), attr.getNodeValue());
                        }
                    }

                    list.add(map);
                }
            }
        }
        return list;
    }
}