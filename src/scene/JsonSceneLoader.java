package scene;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import lighting.AmbientLight;
import org.json.JSONArray;
import org.json.JSONObject;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

public class JsonSceneLoader {
    private static final Logger logger = Logger.getLogger(JsonSceneLoader.class.getName());

    private JsonSceneLoader() {
    }

    public static void loadScene(String filePath, Scene scene) {
        try {
            // Read the file content into a String
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject json = new JSONObject(content);

            // 1. Background Color
            if (json.has("background-color")) {
                scene.background = parseColor(json.getString("background-color"));
            }

            // 2. Ambient Light
            if (json.has("ambient-light")) {
                JSONObject ambient = json.getJSONObject("ambient-light");
                Color color = parseColor(ambient.getString("color"));
                scene.ambientLight = new AmbientLight(color);
            }

            // 3. Geometries
            if (json.has("geometries")) {
                JSONArray geometries = json.getJSONArray("geometries");
                for (int i = 0; i < geometries.length(); i++) {
                    parseGeometry(geometries.getJSONObject(i), scene);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load JSON scene: " + filePath, e);
            throw new RuntimeException(e);
        }
    }

    private static void parseGeometry(JSONObject el, Scene scene) {
        String type = el.getString("type");
        switch (type) {
            case "sphere" -> {
                Point center = parsePoint(el.getString("center"));
                double radius = el.getDouble("radius");
                scene.geometries.add(new Sphere(center, radius));
            }
            case "triangle" -> {
                Point p0 = parsePoint(el.getString("p0"));
                Point p1 = parsePoint(el.getString("p1"));
                Point p2 = parsePoint(el.getString("p2"));
                scene.geometries.add(new Triangle(p0, p1, p2));
            }
            case "plane" -> {
                Point p0 = parsePoint(el.getString("p0"));
                if (el.has("normal")) {
                    Vector normal = parseVector(el.getString("normal"));
                    scene.geometries.add(new Plane(p0, normal));
                } else {
                    Point p1 = parsePoint(el.getString("p1"));
                    Point p2 = parsePoint(el.getString("p2"));
                    scene.geometries.add(new Plane(p0, p1, p2));
                }
            }
            case "tube" -> {
                Point origin = parsePoint(el.getString("origin"));
                Vector AxisDirection = parseVector(el.getString("AxisDirection"));
                double radius = Double.parseDouble(el.getString("radius"));
                scene.geometries.add(new Tube(radius, new Ray(origin, AxisDirection)));
            }
            case "cylinder" -> {
                Point origin = parsePoint(el.getString("origin"));
                Vector AxisDirection = parseVector(el.getString("AxisDirection"));
                double radius = Double.parseDouble(el.getString("radius"));
                double height = Double.parseDouble(el.getString("height"));
                scene.geometries.add(new Cylinder(radius, new Ray(origin, AxisDirection), height));
            }
            case "polygon" -> {
                // Use a simpler attribute name like "count"
                int numVertices = Integer.parseInt(el.getString("count"));
                Point[] vertices = new Point[numVertices];
                for (int k = 0; k < numVertices; ++k) {
                    vertices[k] = parsePoint(el.getString("p" + k));
                }
                scene.geometries.add(new Polygon(vertices));
            }
        }
    }

    // Helpers
    private static Color parseColor(String str) {
        Double3 d = parseDouble3(str);
        return new Color(d._d1(), d._d2(), d._d3());
    }

    private static Point parsePoint(String str) {
        return new Point(parseDouble3(str));
    }

    private static Vector parseVector(String str) {
        return new Vector(parseDouble3(str));
    }

    private static Double3 parseDouble3(String str) {
        String[] parts = str.trim().split("\\s+");
        return new Double3(
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2])
        );
    }
}