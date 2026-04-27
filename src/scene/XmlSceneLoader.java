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

public class XmlSceneLoader {

    private static final Logger logger = Logger.getLogger(XmlSceneLoader.class.getName());

    // Prevent instantiation since it's a static utility class
    private XmlSceneLoader() {
    }

    /**
     * Loads a scene from an XML file into the provided Scene object.
     *
     * @param filePath Path to the XML file
     * @param scene    The Scene object to populate
     */
    public static void loadScene(String filePath, Scene scene) {
        try {
            File xmlFile = new File(filePath);
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(xmlFile);
            doc.getDocumentElement().normalize();

            // 1. Root: Background Color
            String bgColorStr = doc.getDocumentElement().getAttribute("background-color");
            if (!bgColorStr.isEmpty()) {
                Double3 bg = parseDouble3(bgColorStr);
                scene.background = new Color(bg._d1(), bg._d2(), bg._d3());
            }

            // 2. Ambient Light
            NodeList ambientList = doc.getElementsByTagName("ambient-light");
            if (ambientList.getLength() > 0) {
                Element ambient = (Element) ambientList.item(0);
                Double3 color = parseDouble3(ambient.getAttribute("color"));
                scene.ambientLight = new AmbientLight(new Color(color._d1(), color._d2(), color._d3()));
            }

            // 3. Geometries
            Node geometriesNode = doc.getElementsByTagName("geometries").item(0);
            if (geometriesNode != null) {
                NodeList list = geometriesNode.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        parseGeometry((Element) node, scene);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load scene from: " + filePath, e);
            throw new RuntimeException("Error loading XML", e); // Re-throwing so the app knows it failed
        }
    }

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
                // Check attributes to decide which constructor to use
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
                Vector AxisDirection = parseVector(el.getAttribute("AxisDirection"));
                double radius = Double.parseDouble(el.getAttribute("radius"));
                scene.geometries.add(new Tube(radius, new Ray(origin, AxisDirection)));
            }
            case "cylinder" -> {
                Point origin = parsePoint(el.getAttribute("origin"));
                Vector AxisDirection = parseVector(el.getAttribute("AxisDirection"));
                double radius = Double.parseDouble(el.getAttribute("radius"));
                double height = Double.parseDouble(el.getAttribute("height"));
                scene.geometries.add(new Cylinder(radius, new Ray(origin, AxisDirection), height));
            }
            case "polygon" -> {
                // Use a simpler attribute name like "count"
                int numVertices = Integer.parseInt(el.getAttribute("count"));
                Point[] vertices = new Point[numVertices];
                for (int k = 0; k < numVertices; ++k) {
                    vertices[k] = parsePoint(el.getAttribute("p" + k));
                }
                scene.geometries.add(new Polygon(vertices));
            }

        }
    }

    private static Point parsePoint(String str) {
        return new Point(parseDouble3(str));
    }

    private static Vector parseVector(String str) {
        return new Vector(parseDouble3(str));
    }

    private static Double3 parseDouble3(String str) {
        String[] parts = str.trim().split("\\s+");
        return new Double3(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }
}