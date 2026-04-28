package scene;

import geometries.api.Geometry;
import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import java.util.List;
import java.util.Map;
import lighting.AmbientLight;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Abstract base class for scene loading operations.
 * <p>
 * This class implements the Template Method Pattern. It defines the high-level
 * algorithm for populating a {@link Scene} (background, light, and geometries) from a file.
 * while delegating format-specific data extraction (XML or JSON) to subclasses.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public abstract class SceneLoader {

    /**
     * The scene object being populated by this loader
     */
    private final Scene scene;

    /**
     * Initializes a new loader and creates a new Scene with the given name.
     *
     * @param sceneName the identifier for the scene
     */
    public SceneLoader(String sceneName) {
        this.scene = new Scene(sceneName);
    }

    /**
     * The Template Method. Orchestrates the loading process.
     * <p>
     * It executes the algorithm steps in order:
     * 1. Setting the background color.
     * 2. Initializing ambient lighting.
     * 3. Constructing all geometric shapes.
     * </p>
     *
     * @return the fully populated {@link Scene}
     */
    public Scene loadScene() {
        // 1. Process Background Color
        String bgColor = getBackgroundColor();
        if (bgColor != null) {
            scene.background = parseColor(bgColor);
        }

        // 2. Process Ambient Light
        String ambientColor = getAmbientLight();
        if (ambientColor != null) {
            scene.ambientLight = new AmbientLight(parseColor(ambientColor));
        }

        // 3. Process Geometries
        // Subclasses provide raw string data in maps, this class builds the objects.
        List<Map<String, String>> geometryData = getGeometries();
        for (var data : geometryData) {
            scene.geometries.add(buildGeometry(data));
        }

        return scene;
    }

    /**
     * Centralized Factory Method for creating geometric objects.
     * <p>
     * This method handles the logic of converting raw string attributes into
     * concrete geometry implementations.
     * </p>
     *
     * @param data a map containing the attributes for the geometry
     * @return the constructed {@link Geometry} object
     * @throws IllegalArgumentException if the geometry type is unsupported
     */
    private Geometry buildGeometry(Map<String, String> data) {
        String type = data.get("type");
        switch (type) {
            case "sphere" -> {
                Point center = parsePoint(data.get("center"));
                double radius = Double.parseDouble(data.get("radius"));
                return new Sphere(center, radius);
            }
            case "triangle" -> {
                Point p0 = parsePoint(data.get("p0"));
                Point p1 = parsePoint(data.get("p1"));
                Point p2 = parsePoint(data.get("p2"));
                return new Triangle(p0, p1, p2);
            }
            case "plane" -> {
                Point p0 = parsePoint(data.get("p0"));
                // Supports Plane(point, normal) or Plane(p0, p1, p2)
                if (data.containsKey("normal")) {
                    return new Plane(p0, parseVector(data.get("normal")));
                } else {
                    Point p1 = parsePoint(data.get("p1"));
                    Point p2 = parsePoint(data.get("p2"));
                    return new Plane(p0, p1, p2);
                }
            }
            case "tube" -> {
                double radius = Double.parseDouble(data.get("radius"));
                Ray axis = new Ray(parsePoint(data.get("origin")), parseVector(data.get("AxisDirection")));
                return new Tube(radius, axis);
            }
            case "cylinder" -> {
                double radius = Double.parseDouble(data.get("radius"));
                Ray axis = new Ray(parsePoint(data.get("origin")), parseVector(data.get("AxisDirection")));
                double height = Double.parseDouble(data.get("height"));
                return new Cylinder(radius, axis, height);
            }
            case "polygon" -> {
                // Dynamically parses vertices labeled p0, p1, p2... based on the count
                int numVertices = Integer.parseInt(data.get("number of vertices"));
                Point[] vertices = new Point[numVertices];
                for (int k = 0; k < numVertices; ++k)
                    vertices[k] = parsePoint(data.get("p" + k));
                return new Polygon(vertices);
            }
            default -> throw new IllegalArgumentException("Unknown geometry type: " + type);
        }
    }

    // --- Abstract Hooks: To be implemented by format-specific subclasses ---

    /**
     * Returns the scene background color from the source file as a String
     *
     * @return the background color string from the source file
     */
    protected abstract String getBackgroundColor();

    /**
     * Returns the ambient light color from the source file as a String
     *
     * @return the ambient light color string from the source file
     */
    protected abstract String getAmbientLight();

    /**
     * Returns a list of maps representing the string-based attributes for a single geometry
     *
     * @return a list of maps, where each map contains string-based attributes for one geometry
     */
    protected abstract List<Map<String, String>> getGeometries();

    // --- Shared Internal Helpers ---

    /**
     * Converts a coordinate string "x y z" into a {@link Point}
     *
     * @param str the point coordinates in string format
     * @return the constructed point
     */
    protected Point parsePoint(String str) {
        return new Point(parseDouble3(str));
    }

    /**
     * Converts a direction string "x y z" into a {@link Vector}
     *
     * @param str the Vector coordinates in string format
     * @return the constructed vector
     */
    protected Vector parseVector(String str) {
        return new Vector(parseDouble3(str));
    }

    /**
     * Converts a color string "r g b" into a {@link Color} object
     *
     * @param str the color components in string format
     * @return the constructed color object
     */
    protected Color parseColor(String str) {
        Double3 d = parseDouble3(str);
        return new Color(d._d1(), d._d2(), d._d3());
    }

    /**
     * Splits a string by whitespace and converts it to a {@link Double3} primitive
     *
     * @param str the Double3 components in string format
     * @return the constructed Double3 object
     */
    private Double3 parseDouble3(String str) {
        String[] p = str.trim().split("\\s+");
        return new Double3(Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2]));
    }
}