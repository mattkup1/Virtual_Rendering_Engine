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
 * Utility class for loading scene data from XML files.
 * <p>
 * This class uses the DOM (Document Object Model) parser to read XML files and
 * populate a {@link Scene} object with background data, ambient lighting,
 * and various geometric shapes.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class XmlSceneLoader extends SceneLoader {

    private final Document doc;

    public XmlSceneLoader(String sceneName, String jsonName) {

        super(sceneName);

        try {
            File xmlFile = new File(jsonName);
            this.doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(xmlFile);

        } catch (Exception e) {
            throw new RuntimeException("Error loading XML", e);
        }
        // Eliminates empty text nodes and combines split text nodes
        doc.getDocumentElement().normalize();
    }

    @Override
    protected String getBackgroundColor() {
        return doc.getDocumentElement().getAttribute("background-color");
    }

    @Override
    protected String getAmbientLight() {
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

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) node;
                    Map<String, String> map = new HashMap<>();

                    // 1. Store the tag name as the "type" (sphere, triangle, etc.)
                    map.put("type", el.getTagName());

                    // 2. Iterate through all attributes and put them in the map
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