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

public abstract class SceneLoader {

    private final Scene scene;

    public SceneLoader(String sceneName) {
        this.scene = new Scene(sceneName);
    }

    // Keep this static if you want to call SceneLoader.loadScene(...)
    // But it needs to create an instance of the specific loader!
    public Scene loadScene() {
        // 1. Background
        String bgColor = getBackgroundColor();
        if (bgColor != null) scene.background = parseColor(bgColor);

        // 2. Ambient Light (Fixed name to match your subclass)
        String ambientColor = getAmbientLight();
        if (ambientColor != null) {
            scene.ambientLight = new AmbientLight(parseColor(ambientColor));
        }

        // 3. Geometries
        List<Map<String, String>> geometryData = getGeometries();
        for (var data : geometryData) {
            scene.geometries.add(buildGeometry(data));
        }

        return scene;
    }

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
                if (data.containsKey("normal")) {
                    return new Plane(p0, parseVector(data.get("normal")));
                } else {
                    return new Plane(p0, parsePoint(data.get("p1")), parsePoint(data.get("p2")));
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
                int numVertices = Integer.parseInt(data.get("number of vertices"));
                Point[] vertices = new Point[numVertices];
                for (int k = 0; k < numVertices; ++k)
                    vertices[k] = parsePoint(data.get("p" + k));
                return new Polygon(vertices);
            }
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    // --- MUST BE PROTECTED ABSTRACT (No static here!) ---
    protected abstract String getBackgroundColor();

    protected abstract String getAmbientLight();

    protected abstract List<Map<String, String>> getGeometries();

    // Helpers can stay static or be instanced (safer)
    protected Point parsePoint(String str) {
        return new Point(parseDouble3(str));
    }

    protected Vector parseVector(String str) {
        return new Vector(parseDouble3(str));
    }

    protected Color parseColor(String str) {
        Double3 d = parseDouble3(str);
        return new Color(d._d1(), d._d2(), d._d3());
    }

    private Double3 parseDouble3(String str) {
        String[] p = str.trim().split("\\s+");
        return new Double3(Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2]));
    }
}