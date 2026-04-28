package scene;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import lighting.AmbientLight;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

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
public class XmlSceneLoader {

    /**
     * Logger for tracking loading errors and file issues
     */
    private static final Logger logger = Logger.getLogger(XmlSceneLoader.class.getName());

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private XmlSceneLoader() {
    }

    /**
     * Parses an XML file and populates the provided Scene object.
     * <p>
     * Expected XML structure:
     * &lt;scene background-color="r g b"&gt;
     * &lt;ambient-light color="r g b" /&gt;
     * &lt;geometries&gt; ... &lt;/geometries&gt;
     * &lt;/scene&gt;
     * </p>
     *
     * @param filePath the path to the XML source file
     * @param scene    the Scene object to be populated
     * @throws RuntimeException if the file is missing, malformed, or parsing fails
     */
    public static void loadScene(String filePath, Scene scene) {
        try {
            File xmlFile = new File(filePath);
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(xmlFile);

            // Eliminates empty text nodes and combines split text nodes
            doc.getDocumentElement().normalize();

            // 1. Root element: Parse Background Color
            String bgColorStr = doc.getDocumentElement().getAttribute("background-color");
            if (!bgColorStr.isEmpty()) {
                Double3 bg = parseDouble3(bgColorStr);
                scene.background = new Color(bg._d1(), bg._d2(), bg._d3());
            }

            // 2. Parse Ambient Light
            NodeList ambientList = doc.getElementsByTagName("ambient-light");
            if (ambientList.getLength() > 0) {
                Element ambient = (Element) ambientList.item(0);
                Double3 color = parseDouble3(ambient.getAttribute("color"));
                scene.ambientLight = new AmbientLight(new Color(color._d1(), color._d2(), color._d3()));
            }

            // 3. Parse Geometries
            Node geometriesNode = doc.getElementsByTagName("geometries").item(0);
            if (geometriesNode != null) {
                NodeList list = geometriesNode.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    // Only process actual XML elements, skipping whitespace/comments
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        parseGeometry((Element) node, scene);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load scene from: " + filePath, e);
            throw new RuntimeException("Error loading XML", e);
        }
    }

    /**
     * Identifies the geometry type based on the XML tag name and adds it to the scene.
     * * @param el    the XML element representing a shape
     *
     * @param scene the scene to add the geometry to
     */
    private static void parseGeometry(Element el, Scene scene) {
        String type = el.getTagName();
        switch (type) {
            case "sphere" -> {
                Point center = parsePoint(el.getAttribute("center"));
                double radius = Double.parseDouble(el.getAttribute("radius"));
                scene.geometries.add(new Sphere(center, radius));
            }
            case "triangle" -> {
                Point p0 = parsePoint(el.getAttribute("p0"));
                Point p1 = parsePoint(el.getAttribute("p1"));
                Point p2 = parsePoint(el.getAttribute("p2"));
                scene.geometries.add(new Triangle(p0, p1, p2));
            }
            case "plane" -> {
                Point p0 = parsePoint(el.getAttribute("p0"));
                // Plane can be defined by a point and a normal OR by three points
                if (el.hasAttribute("normal")) {
                    Vector normal = parseVector(el.getAttribute("normal"));
                    scene.geometries.add(new Plane(p0, normal));
                } else {
                    Point p1 = parsePoint(el.getAttribute("p1"));
                    Point p2 = parsePoint(el.getAttribute("p2"));
                    scene.geometries.add(new Plane(p0, p1, p2));
                }
            }
            case "tube" -> {
                Point origin = parsePoint(el.getAttribute("origin"));
                Vector axisDir = parseVector(el.getAttribute("AxisDirection"));
                double radius = Double.parseDouble(el.getAttribute("radius"));
                scene.geometries.add(new Tube(radius, new Ray(origin, axisDir)));
            }
            case "cylinder" -> {
                Point origin = parsePoint(el.getAttribute("origin"));
                Vector axisDir = parseVector(el.getAttribute("AxisDirection"));
                double radius = Double.parseDouble(el.getAttribute("radius"));
                double height = Double.parseDouble(el.getAttribute("height"));
                scene.geometries.add(new Cylinder(radius, new Ray(origin, axisDir), height));
            }
            case "polygon" -> {
                // Polygons require a 'count' attribute to know how many points to look for (p0, p1...)
                int numVertices = Integer.parseInt(el.getAttribute("count"));
                Point[] vertices = new Point[numVertices];
                for (int k = 0; k < numVertices; ++k) {
                    vertices[k] = parsePoint(el.getAttribute("p" + k));
                }
                scene.geometries.add(new Polygon(vertices));
            }
        }
    }

    /**
     * Converts a coordinate string ("x y z") into a Point object.
     *
     * @param str coordinate string
     * @return a new Point
     */
    private static Point parsePoint(String str) {
        return new Point(parseDouble3(str));
    }

    /**
     * Converts a direction string ("x y z") into a Vector object.
     *
     * @param str vector components string
     * @return a new Vector
     */
    private static Vector parseVector(String str) {
        return new Vector(parseDouble3(str));
    }

    /**
     * Internal helper to split a string by whitespace and parse three doubles.
     * * @param str string containing three numbers separated by spaces
     *
     * @return a Double3 container
     * @throws NumberFormatException if the string does not contain valid doubles
     */
    private static Double3 parseDouble3(String str) {
        String[] parts = str.trim().split("\\s+");
        return new Double3(
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2])
        );
    }
}